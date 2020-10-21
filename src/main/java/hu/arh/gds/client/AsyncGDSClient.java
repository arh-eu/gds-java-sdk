/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/22
 */

package hu.arh.gds.client;

import hu.arh.gds.message.data.*;
import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.header.MessageHeaderBase;
import hu.arh.gds.message.util.MessageManager;
import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;
import org.msgpack.value.Value;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;


/**
 * Asynchronous client that can be used safely from multithreaded environments to send messages.
 * The assigned listener ({@link AsyncGDSClient#listener}) will be used for callback on incoming message.
 */
public final class AsyncGDSClient {

    public final static class AsyncGDSClientBuilder {

        private NioEventLoopGroup nioEventLoopGroup;
        private GDSMessageListener listener;
        private Logger logger;
        private boolean shutdownByClose;
        private SslContext sslContext;
        private String URI;
        private String userName;
        private String userPassword;
        private long timeout;

        private AsyncGDSClientBuilder() {
            shutdownByClose = true;
            timeout = 3000L;
        }

        /**
         * @param nioEventLoopGroup the NioEventLoopGroup to be used
         * @return this builder
         */
        public AsyncGDSClientBuilder withNioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
            this.nioEventLoopGroup = nioEventLoopGroup;
            return this;
        }

        /**
         * @param listener the MessageListener used for callbacks
         * @return this builder
         */
        public AsyncGDSClientBuilder withListener(GDSMessageListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * @param logger the Logger instance used for the client
         * @return this builder
         */
        public AsyncGDSClientBuilder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Sets whether the {@link AsyncGDSClientBuilder#nioEventLoopGroup} should be shut down when the client is
         * closed or not. This parameter is only taken into account if the EventLoopGroup is set, otherwise the value
         * will always be used as {@code true} regardless of the one specified here.
         *
         * @param shutdownByClose whether the group should be shut down when client is closed
         * @return this builder
         */

        public AsyncGDSClientBuilder withShutdownByClose(boolean shutdownByClose) {
            this.shutdownByClose = shutdownByClose;
            return this;
        }

        /**
         * @param URI the URI of the GDS
         * @return this builder
         */
        public AsyncGDSClientBuilder withURI(String URI) {
            this.URI = URI;
            return this;
        }

        /**
         * @param userName used for the login message and in the headers
         * @return this builder
         */
        public AsyncGDSClientBuilder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * @param userPassword used for password authentication
         * @return this builder
         */
        public AsyncGDSClientBuilder withUserPassword(String userPassword) {
            this.userPassword = userPassword;
            return this;
        }

        /**
         * @param timeout used for connection timeout
         * @return this builder
         * @throws IllegalArgumentException if the value is zero or negative
         */
        public AsyncGDSClientBuilder withTimeout(long timeout) throws IllegalArgumentException {
            if (timeout < 1) {
                throw new IllegalArgumentException("The timeout has to be positive! Specified: " + timeout);
            }
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets up the SSL context for the WebSocket to use for TLS encryption towards the GDS servers.
         *
         * @param cert   the path for the PKCS12 formatted certificate and key file.
         * @param secret the password that was used to encrypt the given file
         * @throws IllegalStateException if the TLS was already setup or the client is already running.
         * @throws Throwable             on any error while decrypting the given cert-key pair.
         **/
        public AsyncGDSClientBuilder withTLS(String cert, String secret) throws Throwable {
            return withTLS(new FileInputStream(cert), secret.toCharArray());
        }

        /**
         * Sets up the SSL context for the WebSocket to use for TLS encryption towards the GDS servers.
         *
         * @param cert   the Stream containing the data for the PKCS12 formatted certificate and key.
         * @param secret the password that was used to encrypt the given data
         * @throws IllegalStateException if the TLS was already setup or the client is already running.
         * @throws Throwable             on any error while decrypting the given cert-key pair.
         */
        public AsyncGDSClientBuilder withTLS(InputStream cert, String secret) throws Throwable {
            return withTLS(cert, secret.toCharArray());
        }

        /**
         * Sets up the SSL context for the WebSocket to use for TLS encryption towards the GDS servers.
         *
         * @param cert   the Stream containing the data for the PKCS12 formatted certificate and key.
         * @param secret the password that was used to encrypt the given data
         * @throws IllegalStateException if the TLS was already setup or the client is already running.
         * @throws Throwable             on any error while decrypting the given cert-key pair.
         */
        public AsyncGDSClientBuilder withTLS(InputStream cert, char[] secret) throws Throwable {
            this.sslContext = createSSLContext(cert, secret);
            return this;
        }

        public AsyncGDSClient build() {
            return new AsyncGDSClient(URI, userName, userPassword, timeout, logger, listener, sslContext, nioEventLoopGroup, shutdownByClose);
        }
    }

    /**
     * Creates a builder instance that can be used to specify different parameters for the client (e.g. TLS credentials,
     * timeout, listener).
     *
     * @return the builder instance
     */
    public static AsyncGDSClientBuilder getBuilder() {
        return new AsyncGDSClientBuilder();
    }

    private final AtomicReference<ConnectionState> state;
    private final NettyWebSocketClient client;
    private final CountDownLatch countDownLatch;

    private final NioEventLoopGroup eventLoopGroup;
    private final GDSMessageListener listener;
    private final Logger log;
    private final boolean shutdownByClose;
    private final SslContext sslCtx;
    private final URI uri;
    private final String userName;
    private final String userPassword;
    private final long timeout;

    /**
     * Creates a new AsyncGDSClient with the specified parameters.
     * the {@code shutDownByClose} parameter is only taken into account if the EventLoopGroup is set, otherwise the
     * value will always be used as {@code true} regardless of the one specified here.
     *
     * @param uri             The URI of the GDS instance the client will connect to
     * @param userName        The username used for the login message
     * @param userPassword    The password used for the password authentication
     * @param timeout         The timeout (in milliseconds) used to indicate when the connection should be considered as failed
     *                        if not responded within the given time frame
     * @param log             A {@link Logger} instance for error messages. If null, a default will be created with
     *                        only {@link Level#SEVERE} logs.
     * @param listener        The listener which handles the incoming messages (callback on receiving anything). Cannot be null.
     * @param eventLoopGroup  the NioEventLoopGroup to be used by the WebSocket client
     * @param shutdownByClose whether the EventLoopGroup should be shut down when close() is called.
     * @throws IllegalArgumentException if the URI or the username is null or empty (or the URI is invalid)
     *                                  or the timeout is less, than {@code 1}.
     */
    public AsyncGDSClient(String uri, String userName, String userPassword, long timeout, Logger log,
                          GDSMessageListener listener, SslContext sslCtx, NioEventLoopGroup eventLoopGroup, boolean shutdownByClose) {

        Objects.requireNonNull(uri, "The URI for the GDS cannot be null!");
        Objects.requireNonNull(userName, "The username for the GDS cannot be null!");
        Objects.requireNonNull(listener, "The GDSMessageListener for the GDS cannot be null!");

        if (userName.trim().length() < 1) {
            throw new IllegalArgumentException("The username cannot be empty or set to only whitespaces!");
        }

        if (timeout < 1) {
            throw new IllegalArgumentException("The given timeout most be positive! (specified: " + timeout + ")");
        }

        this.listener = listener;

        if (eventLoopGroup == null) {
            this.eventLoopGroup = new NioEventLoopGroup();
            this.shutdownByClose = true;
        } else {
            this.eventLoopGroup = eventLoopGroup;
            this.shutdownByClose = shutdownByClose;
        }

        if (log == null) {
            this.log = createDefaultLogger("AsyncGDSClient");
        } else {
            this.log = log;
        }
        this.sslCtx = sslCtx;

        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        if (this.uri.getScheme() == null || !(this.uri.getScheme().startsWith("ws") || this.uri.getScheme().startsWith("wss"))) {
            throw new IllegalArgumentException("The URI scheme must be either 'ws' or 'wss' to setup the WebSocket connection!" +
                    " Specified: " + this.uri.getScheme());
        }

        this.userName = userName;
        this.userPassword = userPassword;
        this.timeout = timeout;

        this.countDownLatch = new CountDownLatch(1);
        this.state = new AtomicReference<>(ConnectionState.NOT_CONNECTED);
        client = new NettyWebSocketClient();
    }

    public static Logger createDefaultLogger(String loggerName) {
        Logger log = Logger.getLogger(loggerName);
        log.setLevel(Level.SEVERE);
        log.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private String format = "[%1$tF %1$tT] [%2$s] | %3$s::%4$s | %5$s %n";
            //calling 'log.info("InfoMessage");' from the constructor would produce:
            //[2020-10-19 08:15:39] [INFO] | hu.arh.gds.client.AsyncGDSClient::<init> | InfoMessage

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getSourceClassName(),
                        lr.getSourceMethodName(),
                        lr.getMessage()
                );
            }
        });
        log.addHandler(handler);

        return log;
    }

    /**
     * Returns the connection state the client is currently in.
     * For possible values see the {@link ConnectionState} enum.
     *
     * @return the ConnectionState value
     */
    public ConnectionState getState() {
        return state.get();
    }

    /**
     * Returns true if the connection is active and the login by the client was successful to the GDS.     *
     *
     * @return true if the login was successful, false otherwise.
     */
    public boolean isConnected() {
        return state.get() == ConnectionState.LOGGED_IN;
    }


    /**
     * Connects to a GDS instance, with the given {@code userName} and {@code URI} given in the constructor.
     * When the login is successful, the assigned {@link GDSMessageListener#onConnectionSuccess(Channel, MessageHeaderBase, MessageData1ConnectionAck)} will be called.
     */
    public void connect() {
        if (state.compareAndSet(ConnectionState.NOT_CONNECTED, ConnectionState.INITIALIZING)) {
            new Thread(() -> {
                client.connect();
                try {
                    if (!countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                        if (getState() != ConnectionState.FAILED || getState() != ConnectionState.DISCONNECTED) {
                            state.set(ConnectionState.FAILED);
                            listener.onConnectionFailure(client.channel,
                                    Either.fromLeft(new GDSTimeoutException("The GDS did not respond within " + timeout + "ms!")));
                        }
                    }
                } catch (InterruptedException ie) {
                    if (getState() != ConnectionState.FAILED || getState() != ConnectionState.DISCONNECTED) {
                        state.set(ConnectionState.FAILED);
                        listener.onConnectionFailure(client.channel,
                                Either.fromLeft(new RuntimeException(ie)));
                    }
                }
            }).start();
        } else {
            throw new IllegalStateException("Could not initialize connection because the state is not " + ConnectionState.NOT_CONNECTED
                    + " but " + getState() + "! (The client is already in use.)");
        }
    }

    /**
     * closes the connection towards the GDS servers.
     */
    public void close() {
        if (getState() != ConnectionState.FAILED) {
            state.set(ConnectionState.DISCONNECTED);
        }
        client.close();
    }


    /**
     * Decrypts and creates an {@link SslContext} based on the information found in the PKCS12 formatted certificate
     * indicated by the {@code cert}. The password used for decryption should be specified in the {@code password}
     * parameter.
     * If any error happens during the reading, will throw the appropriate exception.
     *
     * @param cert     the {@link InputStream} containing the certificate and the private key.
     * @param password the password used to encrypt the file.
     * @return The created {@link SslContext}.
     * @throws KeyStoreException         if the KeyStore cannot provide an instance for PKCS12 format
     * @throws IOException               if the KeyStore cannot load the certificate from the specified stream
     * @throws CertificateException      if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found
     * @throws UnrecoverableKeyException if the key cannot be recovered (e.g., the given password is wrong).
     */
    public static SslContext createSSLContext(String cert, String password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (cert == null) {
            return null;
        }
        return createSSLContext(new FileInputStream(cert), password.toCharArray());
    }


    /**
     * Decrypts and creates an {@link SslContext} based on the information found in the PKCS12 formatted certificate
     * indicated by the {@code cert}. The password used for decryption should be specified in the {@code password}
     * parameter.
     * If any error happens during the reading, will throw the appropriate exception.
     *
     * @param cert     the {@link InputStream} containing the certificate and the private key.
     * @param password the password used to encrypt the file.
     * @return The created {@link SslContext}.
     * @throws KeyStoreException         if the KeyStore cannot provide an instance for PKCS12 format
     * @throws IOException               if the KeyStore cannot load the certificate from the specified stream
     * @throws CertificateException      if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found
     * @throws UnrecoverableKeyException if the key cannot be recovered (e.g., the given password is wrong).
     */
    public static SslContext createSSLContext(InputStream cert, char[] password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (cert == null) {
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(cert, password);

        Enumeration<String> enumeration = keyStore.aliases();

        if (!enumeration.hasMoreElements()) {
            throw new NoSuchElementException("no aliases");
        }
        String alias = enumeration.nextElement();
        if (enumeration.hasMoreElements()) {
            throw new NoSuchElementException("multiple aliases");
        }
        if (!keyStore.isKeyEntry(alias)) {
            throw new NoSuchElementException(String.format("alias %1$s is not a key entry", alias));
        }
        Key key = keyStore.getKey(alias, password);
        if (!(key instanceof PrivateKey)) {
            throw new NoSuchElementException(String.format("alias %1$s doesn't contain a private key", alias));
        }
        List<X509Certificate> certificates = new ArrayList<>();
        java.security.cert.Certificate[] chain = keyStore.getCertificateChain(alias);
        if (null == chain) {
            throw new NoSuchElementException(String.format("alias %1$s doesn't contain a certificate chain", alias));
        }
        for (Certificate certificate : chain) {
            certificates.add((X509Certificate) certificate);
        }
        if (certificates.isEmpty()) {
            throw new NoSuchElementException(String.format("alias %1$s contains an empty certificate chain", alias));
        }
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(null, null);
        keyStore.setKeyEntry("server", key, new char[0], certificates.toArray(new X509Certificate[0]));
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, new char[0]);


        return SslContextBuilder
                .forClient()
                .keyManager(keyManagerFactory)
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    //<editor-fold desc="Method overloads for sending different types of messages to the GDS System">


    /**
     * Sends an event message.
     *
     * @param operations     The list of strings containing the event operations.
     * @param binaryContents The attachments sent along with the message.
     * @param priorityLevels The priority levels
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEvent2(String operations,
                                    Map<String, byte[]> binaryContents,
                                    List<PriorityLevelHolder> priorityLevels)
            throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageHeaderBase(userName, MessageDataType.EVENT_2),
                MessageManager.createMessageData2Event(operations, binaryContents, priorityLevels));
    }

    /**
     * Sends an event message
     *
     * @param operations     The list of strings containing the event operations.
     * @param binaryContents The attachments sent along with the message.
     * @param priorityLevels The priority levels
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEvent2(List<String> operations,
                                    Map<String, byte[]> binaryContents,
                                    List<PriorityLevelHolder> priorityLevels)
            throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageHeaderBase(userName, MessageDataType.EVENT_2),
                MessageManager.createMessageData2Event(operations, binaryContents, priorityLevels));
    }

    /**
     * Sends an event message
     *
     * @param event the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEvent2(MessageData2Event event)
            throws IOException, ValidationException {
        return sendMessage(event);
    }

    /**
     * Sends an event message
     *
     * @param messageID the message ID to be used in the header
     * @param event     the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEvent2(String messageID, MessageData2Event event)
            throws IOException, ValidationException {
        return sendMessage(messageID, event);
    }

    /**
     * Sends an event message
     *
     * @param header the message header
     * @param event  the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEvent2(MessageHeaderBase header, MessageData2Event event)
            throws IOException, ValidationException {
        return sendMessage(header, event);
    }


    /**
     * Sends an attachment request message
     *
     * @param request the attachment request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequest4(String request) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageHeaderBase(userName, MessageDataType.ATTACHMENT_REQUEST_4),
                MessageManager.createMessageData4AttachmentRequest(request));
    }

    /**
     * Sends an attachment request message
     *
     * @param request the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequest4(MessageData4AttachmentRequest request)
            throws IOException, ValidationException {
        return sendMessage(request);
    }


    /**
     * Sends an attachment request message
     *
     * @param messageID the message ID to be used in the header
     * @param request   the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequest4(String messageID, MessageData4AttachmentRequest request)
            throws IOException, ValidationException {
        return sendMessage(messageID, request);
    }


    /**
     * Sends an attachment request message
     *
     * @param header  the message header
     * @param request the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequest4(MessageHeaderBase header, MessageData4AttachmentRequest request)
            throws IOException, ValidationException {
        return sendMessage(header, request);
    }


    /**
     * Sends an attachment request ACK message
     *
     * @param globalStatus    the ACK status for the message
     * @param data            the data containing the ACK message, if no error happened.
     * @param globalException the String containing any error messages, if something went wrong.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public ChannelFuture createMessageData5AttachmentRequestAck(
            AckStatus globalStatus,
            AttachmentRequestAckDataHolder data,
            String globalException) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageData5AttachmentRequestAck(globalStatus, data, globalException));
    }

    /**
     * Sends an attachment request ACK message
     *
     * @param requestAck the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequestAck5(MessageData5AttachmentRequestAck requestAck)
            throws IOException, ValidationException {
        return sendMessage(requestAck);
    }


    /**
     * Sends an attachment request ACK message
     *
     * @param messageID  the message ID to be used in the header
     * @param requestAck the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequestAck5(String messageID, MessageData5AttachmentRequestAck requestAck)
            throws IOException, ValidationException {
        return sendMessage(messageID, requestAck);
    }


    /**
     * Sends an attachment request ACK message
     *
     * @param header     the message header
     * @param requestAck the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentRequestAck5(MessageHeaderBase header, MessageData5AttachmentRequestAck requestAck)
            throws IOException, ValidationException {
        return sendMessage(header, requestAck);
    }


    /**
     * Sends an attachment response message
     *
     * @param result      the result holder containing the attachment
     * @param eventHolder the event holder of the message
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendAttachmentResponse6(
            AttachmentResultHolder result,
            EventHolder eventHolder
    ) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageData6AttachmentResponse(result, eventHolder));
    }


    /**
     * Sends an attachment response message
     *
     * @param response the response to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentResponse6(MessageData6AttachmentResponse response)
            throws IOException, ValidationException {
        return sendMessage(response);
    }


    /**
     * Sends an attachment response message
     *
     * @param messageID the message ID to be used in the header
     * @param response  the request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentResponse6(String messageID, MessageData6AttachmentResponse response)
            throws IOException, ValidationException {
        return sendMessage(messageID, response);
    }

    /**
     * Sends an attachment response message
     *
     * @param header   the message header
     * @param response the response to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentResponse6(MessageHeaderBase header, MessageData6AttachmentResponse response)
            throws IOException, ValidationException {
        return sendMessage(header, response);
    }


    /**
     * Sends an attachment response ACK message
     *
     * @param globalStatus    the Status code for the request
     * @param data            the data for the attachment response
     * @param globalException the error in string format (if any)
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendAttachmentResponseAck7(AckStatus globalStatus,
                                                    AttachmentResponseAckResultHolder data,
                                                    String globalException) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageData7AttachmentResponseAck(globalStatus, data, globalException));
    }


    /**
     * Sends an attachment response ACK message
     *
     * @param responseAck the response to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentResponseAck7(MessageData7AttachmentResponseAck responseAck)
            throws IOException, ValidationException {
        return sendMessage(responseAck);
    }

    /**
     * Sends an attachment response ACK message
     *
     * @param messageID   the message ID to be used in the header
     * @param responseAck the response to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentResponseAck7(String messageID, MessageData7AttachmentResponseAck responseAck)
            throws IOException, ValidationException {
        return sendMessage(messageID, responseAck);
    }

    /**
     * Sends an attachment response ACK message
     *
     * @param header      the message header
     * @param responseAck the response to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendAttachmentResponseAck7(MessageHeaderBase header, MessageData7AttachmentResponseAck responseAck)
            throws IOException, ValidationException {
        return sendMessage(header, responseAck);
    }

    /**
     * Sends an event document message
     *
     * @param tableName    the table name
     * @param fieldHolders the field holder values
     * @param records      the records
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendEventDocument8(String tableName,
                                            List<FieldHolder> fieldHolders,
                                            List<List<Value>> records) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageData8EventDocument(tableName, fieldHolders, records));
    }


    /**
     * Sends an event document message
     *
     * @param tableName        the table name
     * @param fieldHolders     the field holder values
     * @param records          the records
     * @param returningOptions the returning fields
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendEventDocument8(String tableName,
                                            List<FieldHolder> fieldHolders,
                                            List<List<Value>> records,
                                            Map<Integer, List<String>> returningOptions) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageData8EventDocument(tableName, fieldHolders, records, returningOptions));
    }


    /**
     * Sends an event document message
     *
     * @param eventDocument document the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEventDocument8(MessageData8EventDocument eventDocument)
            throws IOException, ValidationException {
        return sendMessage(eventDocument);
    }

    /**
     * Sends an event document message
     *
     * @param messageID     the message ID to be used in the header
     * @param eventDocument document the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEventDocument8(String messageID, MessageData8EventDocument eventDocument)
            throws IOException, ValidationException {
        return sendMessage(messageID, eventDocument);
    }

    /**
     * Sends an event document message
     *
     * @param header        the message header
     * @param eventDocument document the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEventDocument8(MessageHeaderBase header, MessageData8EventDocument eventDocument)
            throws IOException, ValidationException {
        return sendMessage(header, eventDocument);
    }

    /**
     * Sends an event document ack message
     *
     * @param globalStatus    the Status code for the request
     * @param result          the result of the event document request
     * @param globalException the error in string format (if any)
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendEventDocumentAck9(AckStatus globalStatus,
                                               List<EventDocumentResultHolder> result,
                                               String globalException) throws IOException, ValidationException {
        return sendMessage(MessageManager.createMessageData9EventDocumentAck(globalStatus, result, globalException));
    }

    /**
     * Sends an event document ack message
     *
     * @param eventDocumentAck document the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEventDocumentAck9(MessageData9EventDocumentAck eventDocumentAck)
            throws IOException, ValidationException {
        return sendMessage(eventDocumentAck);
    }

    /**
     * Sends an event document ack message
     *
     * @param messageID        the message ID to be used in the header
     * @param eventDocumentAck document the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEventDocumentAck9(String messageID, MessageData9EventDocumentAck eventDocumentAck)
            throws IOException, ValidationException {
        return sendMessage(messageID, eventDocumentAck);
    }

    /**
     * Sends an event document ack message
     *
     * @param header           the message header
     * @param eventDocumentAck document the event to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendEventDocumentAck9(MessageHeaderBase header, MessageData9EventDocumentAck eventDocumentAck)
            throws IOException, ValidationException {
        return sendMessage(header, eventDocumentAck);
    }


    /**
     * Sends a query request message
     *
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendQueryRequest10(
            String query,
            ConsistencyType consistencyType,
            Long timeout) throws IOException, ValidationException {
        return sendQueryRequest10(MessageManager.createMessageHeaderBase(userName, MessageDataType.QUERY_REQUEST_10),
                MessageManager.createMessageData10QueryRequest(query, consistencyType, timeout));
    }

    /**
     * Sends a query request message
     *
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @param pageSize        the page size used for the query
     * @param queryType       the type of the query (scroll/page)
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendQueryRequest10(
            String query,
            ConsistencyType consistencyType,
            Long timeout,
            Integer pageSize,
            Integer queryType) throws IOException, ValidationException {
        return sendQueryRequest10(MessageManager.createMessageHeaderBase(userName, MessageDataType.QUERY_REQUEST_10),
                MessageManager.createMessageData10QueryRequest(query, consistencyType, timeout, pageSize, queryType));
    }


    /**
     * Sends a query request message
     *
     * @param request the query request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendQueryRequest10(MessageData10QueryRequest request)
            throws IOException, ValidationException {
        return sendMessage(request);
    }

    /**
     * Sends a query request message
     *
     * @param messageID the message ID to be used in the header
     * @param request   the query request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */

    public ChannelFuture sendQueryRequest10(String messageID, MessageData10QueryRequest request)
            throws IOException, ValidationException {
        return sendMessage(messageID, request);
    }

    /**
     * Sends a query request message
     *
     * @param header  the message header
     * @param request the query request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */

    public ChannelFuture sendQueryRequest10(MessageHeaderBase header, MessageData10QueryRequest request)
            throws IOException, ValidationException {
        return sendMessage(header, request);
    }

    /**
     * Sends a next query page request message
     *
     * @param queryContextHolder the ContextHolder containing information about the current query status
     * @param timeout            the timeout used in the GDS for the query
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendNextQueryPage12(
            QueryContextHolder queryContextHolder, Long timeout) throws IOException, ValidationException {
        return sendNextQueryPage12(MessageManager.createMessageHeaderBase(userName, MessageDataType.NEXT_QUERY_PAGE_12),
                MessageManager.createMessageData12NextQueryPage(queryContextHolder, timeout));
    }

    /**
     * Sends a next query page request message
     *
     * @param queryContextHolder the ContextHolder containing information about the current query status
     * @param timeout            the timeout used in the GDS for the query
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public ChannelFuture sendNextQueryPage12(
            QueryContextHolderSerializable queryContextHolder,
            Long timeout) throws IOException, ValidationException {
        return sendNextQueryPage12(MessageManager.createMessageHeaderBase(userName, MessageDataType.NEXT_QUERY_PAGE_12),
                MessageManager.createMessageData12NextQueryPage(queryContextHolder, timeout));
    }


    /**
     * Sends a next query page request message
     *
     * @param request the query request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendNextQueryPage12(MessageData12NextQueryPage request)
            throws IOException, ValidationException {
        return sendMessage(request);
    }

    /**
     * Sends a next query page request message
     *
     * @param messageID the message ID to be used in the header
     * @param request   the query request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */

    public ChannelFuture sendNextQueryPage12(String messageID, MessageData12NextQueryPage request)
            throws IOException, ValidationException {
        return sendMessage(messageID, request);
    }

    /**
     * Sends a next query page request message
     *
     * @param header  the message header
     * @param request the query request to be sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendNextQueryPage12(MessageHeaderBase header, MessageData12NextQueryPage request)
            throws IOException, ValidationException {
        return sendMessage(header, request);
    }

    /**
     * Sends a message towards the GDS, notifying on the {@link AsyncGDSClient#listener} if a reply arrives.
     *
     * @param data the message data sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendMessage(MessageData data) throws ValidationException, IOException {
        return sendMessage(MessageManager.createMessageHeaderBase(userName, data.getTypeHelper().getMessageDataType()), data);
    }

    /**
     * Sends a message towards the GDS, notifying on the {@link AsyncGDSClient#listener} if a reply arrives.
     *
     * @param messageID the message ID to be used in the header
     * @param data      the message data sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendMessage(String messageID, MessageData data) throws ValidationException, IOException {
        MessageHeader header = MessageManager.createMessageHeaderBase(
                userName,
                messageID != null ? messageID : UUID.randomUUID().toString(),
                data.getTypeHelper().getMessageDataType());
        return sendMessage(header, data);
    }

    /**
     * Sends a message towards the GDS, notifying on the {@link AsyncGDSClient#listener} if a reply arrives.
     *
     * @param header the message header
     * @param data   the message data sent to the GDS.
     * @return the {@link ChannelFuture} instance associated with the communication channel
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public ChannelFuture sendMessage(MessageHeader header, MessageData data) throws ValidationException, IOException {
        log.config("Sending message with ID " + header.getTypeHelper().asBaseMessageHeader().getMessageId()
                + " of type " + data.getTypeHelper().getMessageDataType());
        return sendMessage(MessageManager.createMessage(header, data));
    }

    //</editor-fold>

    /**
     * Sends the message without any further validation than checking the connection.
     * <p>
     * This should not be called by client code natively as it bypasses any message format checking
     * (although the GDS will drop it if it's incorrect)
     *
     * @param message the raw message to be sent
     * @return the {@link ChannelFuture} instance associated with the communication channel
     */
    private ChannelFuture sendMessage(byte[] message) {
        ConnectionState state = getState();
        if (state != ConnectionState.LOGGED_IN) {
            throw new IllegalStateException("Could not send message! Expected client state 'LOGGED_IN' but got " + state);
        }
        return client.send(message);
    }

    /**
     * Processes the given message, trying to parse it.
     * If any error occurs, will print it to the log. Otherwise will call the {@link AsyncGDSClient#listener} with the
     * right message type.
     *
     * @param message the binary containing the incoming message
     */
    private void handleIncomingMessage(byte[] message) {
        try {
            MessageHeaderBase header = MessageManager.getMessageHeaderFromBinaryMessage(message).getTypeHelper().asBaseMessageHeader();
            MessageData body = MessageManager.getMessageData(message);

            log.config("Incoming message of type " + header.getDataType() + " with ID: " + header.getMessageId());

            switch (body.getTypeHelper().getMessageDataType()) {
                case CONNECTION_ACK_1: {
                    countDownLatch.countDown();
                    MessageData1ConnectionAck connectionAck = body.getTypeHelper().asConnectionAckMessageData1();
                    if (connectionAck.getGlobalStatus() != AckStatus.OK) {
                        if (!state.compareAndSet(ConnectionState.LOGGING_IN, ConnectionState.FAILED)) {
                            if (getState() != ConnectionState.DISCONNECTED) {
                                throw new IllegalStateException("Expected state is LOGGING_IN but got: " + getState());
                            } else {
                                return;
                            }
                        }
                        close();
                        this.listener.onConnectionFailure(client.channel, Either.fromRight(new Pair<>(header, connectionAck)));

                    } else {
                        if (!state.compareAndSet(ConnectionState.LOGGING_IN, ConnectionState.LOGGED_IN)) {
                            if (getState() != ConnectionState.DISCONNECTED) {
                                throw new IllegalStateException("Expected state is LOGGING_IN but got: " + getState());
                            } else {
                                return;
                            }
                        }
                        this.listener.onConnectionSuccess(client.channel, header, connectionAck);
                    }
                }
                break;
                case EVENT_ACK_3:
                    listener.onEventAck3(header, body.getTypeHelper().asEventAckMessageData3());
                    break;
                case ATTACHMENT_REQUEST_4:
                    listener.onAttachmentRequest4(header, body.getTypeHelper().asAttachmentRequestMessageData4());
                    break;
                case ATTACHMENT_REQUEST_ACK_5:
                    listener.onAttachmentRequestAck5(header, body.getTypeHelper().asAttachmentRequestAckMessageData5());
                    break;
                case ATTACHMENT_RESPONSE_6:
                    listener.onAttachmentResponse6(header, body.getTypeHelper().asAttachmentResponseMessageData6());
                    break;
                case ATTACHMENT_RESPONSE_ACK_7:
                    listener.onAttachmentResponseAck7(header, body.getTypeHelper().asAttachmentResponseAckMessageData7());
                    break;
                case EVENT_DOCUMENT_8:
                    listener.onEventDocument8(header, body.getTypeHelper().asEventDocumentMessageData8());
                    break;
                case EVENT_DOCUMENT_ACK_9:
                    listener.onEventDocumentAck9(header, body.getTypeHelper().asEventDocumentAckMessageData9());
                    break;
                case QUERY_REQUEST_ACK_11:
                    listener.onQueryRequestAck11(header, body.getTypeHelper().asQueryRequestAckMessageData11());
                    break;
                default:
                    log.warning("Received a message from the GDS that should not be sent! Type: " + body.getTypeHelper().getMessageDataType());
                    break;
            }
        } catch (IOException | ReadException | ValidationException e) {
            log.info("The format of the incoming binary message is invalid! " + e.toString());
        }
    }

    /**
     * Inner class which is used to create the WebSocket channel and send messages on it
     */
    private class NettyWebSocketClient {
        NettyWebSocketClient() {
            log.config("NettyWebSocketClient initialized!");
        }

        Channel channel;

        void connect() {
            try {
                new Bootstrap()
                        .group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {

                                ChannelPipeline pipeline = ch.pipeline();
                                if (sslCtx != null) {
                                    pipeline.addFirst(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
                                }
                                pipeline.addLast(
                                        new HttpClientCodec(),
                                        new HttpObjectAggregator(8192),
                                        WebSocketClientCompressionHandler.INSTANCE,
                                        new GDSWebSocketClientHandler(
                                                WebSocketClientHandshakerFactory.newHandshaker(
                                                        uri, WebSocketVersion.V13, null, true,
                                                        new DefaultHttpHeaders(), 10485760)));
                            }
                        })
                        .connect(uri.getHost(), uri.getPort());

                if (!state.compareAndSet(ConnectionState.INITIALIZING, ConnectionState.CONNECTING)) {
                    if (getState() != ConnectionState.DISCONNECTED) {
                        throw new IllegalStateException("Expected state INITIALIZING but got " + getState());
                    } else {
                        return;
                    }
                }

                log.config("Netty channels initialized!");

            } catch (Throwable t) {
                log.severe(t.toString());

                close();
                if (getState() != ConnectionState.FAILED) {
                    state.set(ConnectionState.FAILED);
                    listener.onConnectionFailure(channel, Either.fromLeft(t));
                }
            }
        }

        void close() {
            if (channel != null) {
                channel.writeAndFlush(new CloseWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
                channel = null;
            }
            if (shutdownByClose) {
                eventLoopGroup.shutdownGracefully();
            }
        }


        ChannelFuture send(byte[] message) {
            WebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(message));
            log.config("Sending BinaryWebSocketFrame..");
            log.fine("Message is " + message.length + " bytes");
            return channel.writeAndFlush(frame);
        }
    }

    /**
     * Inner class used to handle the incoming messages and channel changes
     */
    private class GDSWebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        private WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;

        GDSWebSocketClientHandler(WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
            log.config("GDSWebSocketClientHandler initialized!");
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.config("WebSocketClient connection successfully opened!");
            handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("WebSocketClient connection disconnected!");
            //proper CLOSE after communications
            if (getState() == ConnectionState.DISCONNECTED ||
                    state.compareAndSet(ConnectionState.LOGGED_IN, ConnectionState.DISCONNECTED)) {
                listener.onDisconnect(client.channel);
                //login failed and the connection was closed from the GDS side
            } else if (getState() != ConnectionState.FAILED) {
                log.warning("The state should be either FAILED or LOGGED_IN but found " + getState() + "!");
            }

            super.channelInactive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (getState() == ConnectionState.FAILED) {
                log.config("Incoming message but the client is already in a failed state! (msg: " + msg.toString() + ")");
                return;
            }
            if (getState() == ConnectionState.DISCONNECTED) {
                log.config("Incoming message but the client is already in a disconnected state! (msg: " + msg.toString() + ")");
                return;
            }
            Channel ch = ctx.channel();
            if (!handshaker.isHandshakeComplete()) {
                try {
                    handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                    log.info("WebSocketClient connection established!");
                    client.channel = ch;
                    handshakeFuture.setSuccess();
                    if (!state.compareAndSet(ConnectionState.CONNECTING, ConnectionState.CONNECTED)) {
                        if (getState() != ConnectionState.DISCONNECTED) {
                            String exceptionMessage = "Expected CONNECTING but got " + getState();
                            state.set(ConnectionState.FAILED);
                            throw new IllegalStateException(exceptionMessage);
                        }
                    }

                    if (!state.compareAndSet(ConnectionState.CONNECTED, ConnectionState.LOGGING_IN)) {
                        if (getState() == ConnectionState.DISCONNECTED) {
                            String exceptionMessage = "Expected CONNECTED but got " + getState();
                            state.set(ConnectionState.FAILED);
                            throw new IllegalStateException(exceptionMessage);
                        }
                    }

                    MessageHeader header = MessageManager.createMessageHeaderBase(userName, MessageDataType.CONNECTION_0);
                    //Current GDS version is 5.1
                    MessageData data = MessageManager.createMessageData0Connection(true, (5 << 16 | 1), false, null, userPassword);
                    log.config("Sending login message..");
                    byte[] message = MessageManager.createMessage(header, data);
                    ch.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(message)));
                } catch (WebSocketHandshakeException e) {
                    log.info("WebSocketClient failed to connect!");
                    handshakeFuture.setFailure(e);
                    if (getState() != ConnectionState.FAILED) {
                        state.set(ConnectionState.FAILED);
                        listener.onConnectionFailure(ch, Either.fromLeft(e));
                    }
                }
                return;
            }

            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                throw new IllegalStateException(
                        "Unexpected FullHttpResponse received! (getStatus=" + response.status() +
                                ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
            }

            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof BinaryWebSocketFrame) {
                log.fine("WebSocketClient received BinaryWebSocketFrame");
                byte[] binaryFrame = new byte[frame.content().readableBytes()];
                frame.content().readBytes(binaryFrame);
                handleIncomingMessage(binaryFrame);
            } else if (frame instanceof TextWebSocketFrame) {
                log.fine("WebSocketClient received TextWebSocketFrame");
            } else if (frame instanceof PingWebSocketFrame) {
                log.fine("WebSocketClient received ping");
                ch.writeAndFlush(new PongWebSocketFrame());
            } else if (frame instanceof PongWebSocketFrame) {
                log.fine("WebSocketClient received pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                log.config("WebSocketClient received closing frame..");
                CloseWebSocketFrame closeWebSocketFrame = (CloseWebSocketFrame) frame;
                log.config("Close status: " + closeWebSocketFrame.statusCode() + ", reason: " + closeWebSocketFrame.reasonText());
                if (getState() != ConnectionState.LOGGED_IN) {
                    state.set(ConnectionState.FAILED);
                    countDownLatch.countDown();
                    listener.onConnectionFailure(ch, Either.fromLeft(new Exception(closeWebSocketFrame.reasonText())));
                }
                close();
            } else {
                log.fine("Unsupported frame type: " + frame.getClass().getName());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.severe(Objects.toString(cause));
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            client.close();

            if (getState() != ConnectionState.FAILED) {
                if (getState() != ConnectionState.LOGGED_IN) {
                    state.set(ConnectionState.FAILED);
                    listener.onConnectionFailure(ctx.channel(), Either.fromLeft(cause));
                }
            }
            throw new RuntimeException(cause);
        }
    }
}
