/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/28
 */

package hu.arheu.gds.client;

import hu.arheu.gds.message.clienttypes.AttachmentResult;
import hu.arheu.gds.message.clienttypes.EventDocumentResponse;
import hu.arheu.gds.message.clienttypes.EventResponse;
import hu.arheu.gds.message.clienttypes.QueryResponse;
import hu.arheu.gds.message.data.*;
import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.data.impl.AttachmentResponseAckResultHolderImpl;
import hu.arheu.gds.message.data.impl.AttachmentResultHolderImpl;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.header.MessageHeaderBase;
import hu.arheu.gds.message.util.MessageManager;
import hu.arheu.gds.message.util.ValidationException;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import org.msgpack.value.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;


/**
 * Synchronized version (wrapper) for the {@link AsyncGDSClient} class.
 * This version uses {@link ConcurrentHashMap} and {@link CountDownLatch} to wrap the calls and their responses.
 * <p>
 * Since messages might not arrive in time, a timeout has to be specified for the waiting to avoid the code to be stuck.
 */
public final class SyncGDSClient {
    public final static class SyncGDSClientBuilder {

        private Logger logger;
        private SslContext sslContext;
        private String URI;
        private String userName;
        private String userPassword;
        private long timeout;

        private SyncGDSClientBuilder() {
        }

        /**
         * @param logger the Logger instance used for the client
         * @return this builder
         */
        public SyncGDSClientBuilder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * @param URI the URI of the GDS
         * @return this builder
         */
        public SyncGDSClientBuilder withURI(String URI) {
            this.URI = URI;
            return this;
        }

        /**
         * @param userName used for the login message and in the headers
         * @return this builder
         */
        public SyncGDSClientBuilder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * @param userPassword used for password authentication
         * @return this builder
         */
        public SyncGDSClientBuilder withUserPassword(String userPassword) {
            this.userPassword = userPassword;
            return this;
        }

        /**
         * @param timeout used for connection timeout
         * @return this builder
         * @throws IllegalArgumentException if the value is zero or negative
         */
        public SyncGDSClientBuilder withTimeout(long timeout) throws IllegalArgumentException {
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
        public SyncGDSClientBuilder withTLS(String cert, String secret) throws Throwable {
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
        public SyncGDSClientBuilder withTLS(InputStream cert, String secret) throws Throwable {
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
        public SyncGDSClientBuilder withTLS(InputStream cert, char[] secret) throws Throwable {
            this.sslContext = AsyncGDSClient.createSSLContext(cert, secret);
            return this;
        }

        public SyncGDSClient build() {
            return new SyncGDSClient(URI, userName, userPassword, logger, (timeout > 0 ? timeout : 3000L), sslContext);
        }
    }

    /**
     * Creates a {@link SyncGDSClientBuilder} instance that can be used to set the initial parameters for the client.
     *
     * @return a new builder instance
     */
    public static SyncGDSClientBuilder getBuilder() {
        return new SyncGDSClientBuilder();
    }

    @FunctionalInterface
    private interface MessageOperation {
        MessageOperation SKIP = () -> {
        };

        void run() throws IOException, ValidationException;
    }

    private final AsyncGDSClient asyncGDSClient;
    private final ConcurrentHashMap<String, Pair<CountDownLatch, Pair<MessageHeaderBase, MessageData>>> incomingCache;
    private final AtomicReference<Pair<MessageHeaderBase, MessageData1ConnectionAck>> loginResponse;

    private volatile Either<Throwable, Pair<MessageHeaderBase, MessageData1ConnectionAck>> connectionFailureReason;
    private volatile CountDownLatch connectLatch;
    private volatile CountDownLatch closeLatch;

    private final Logger log;
    private final long timeout;
    private final String userName;

    private boolean clientUsed;

    private final Object lock = new Object();

    /**
     * @param uri          The URI of the given GDS instance.
     * @param userName     the username used to log in.
     * @param userPassword the password used for authentication in the GDS. {@code null}, if not used.
     * @param log          The Logger instance used for any error message. If {@code null}, default one will be created.
     * @param timeout      The timeout after the client throws a {@link GDSTimeoutException} if the message does not arrive.
     * @param sslContext   the SSLContext to be used if connecting via TLS, {@code null} otherwise.
     *                     This can be created by the {@link AsyncGDSClient#createSSLContext(InputStream, char[])} method.
     */
    public SyncGDSClient(String uri, String userName, String userPassword, Logger log, long timeout, SslContext sslContext) {
        GDSMessageListener listener = new GDSMessageListener() {

            @Override
            public void onConnectionSuccess(Channel ch, MessageHeaderBase header, MessageData1ConnectionAck response) {
                loginResponse.set(new Pair<>(header, response));
                connectLatch.countDown();
            }

            @Override
            public void onConnectionFailure(Channel channel, Either<Throwable, Pair<MessageHeaderBase, MessageData1ConnectionAck>> reason) {
                connectionFailureReason = reason;
                connectLatch.countDown();
            }

            @Override
            public void onDisconnect(Channel ch) {
                closeLatch.countDown();
            }

            @Override
            public void onEventAck3(MessageHeaderBase header, MessageData3EventAck response) {
                handleIncomingForLatch(header, response);
            }

            @Override
            public void onAttachmentRequestAck5(MessageHeaderBase header, MessageData5AttachmentRequestAck requestAck) {
                handleIncomingForLatch(header, requestAck);
            }

            @Override
            public void onAttachmentResponse6(MessageHeaderBase header, MessageData6AttachmentResponse response) {
                handleIncomingForLatch(header, response);
            }

            @Override
            public void onEventDocumentAck9(MessageHeaderBase header, MessageData9EventDocumentAck eventDocumentAck) {
                handleIncomingForLatch(header, eventDocumentAck);
            }

            @Override
            public void onQueryRequestAck11(MessageHeaderBase header, MessageData11QueryRequestAck response) {
                handleIncomingForLatch(header, response);
            }

            private void handleIncomingForLatch(MessageHeaderBase header, MessageData data) {
                Pair<CountDownLatch, Pair<MessageHeaderBase, MessageData>> latchPair = incomingCache.get(header.getMessageId());
                if (latchPair != null) {
                    latchPair.setSecond(new Pair<>(header, data));
                    latchPair.getFirst().countDown();
                } else {
                    log.warning("The message with ID " + header.getMessageId() + " was not expected by the client.");
                }
            }

        };

        if (log == null) {
            this.log = AsyncGDSClient.createDefaultLogger("SyncGDSClient");
        } else {
            this.log = log;
        }

        this.asyncGDSClient = new AsyncGDSClient(uri, userName, userPassword, timeout, this.log, listener, sslContext, null, true);
        this.userName = userName;
        this.timeout = timeout;

        clientUsed = false;

        connectLatch = new CountDownLatch(1);
        closeLatch = new CountDownLatch(1);
        loginResponse = new AtomicReference<>();
        incomingCache = new ConcurrentHashMap<>();

        this.log.config("SyncGDSClient successfully initialized.");
    }


    /**
     * Checks whether the connection is active and the login message was successfully sent and the login ACK received
     * with success status.
     *
     * @return {@code true} if the connection is active, {@code false} otherwise.
     */
    public boolean isConnected() {
        return asyncGDSClient.isConnected();
    }

    /**
     * Sets up the connection to the GDS instance, creates the WebSocket channel, sends the login message and awaits the
     * response for it. The return value indicates whether the login was successful.
     * <p>
     * If the connection or the login was successful the {@link SyncGDSClient#getConnectionError()} ()}
     * will contain the response errors.
     *
     * @return {@code true} on successful login, {@code false} otherwise.
     */
    public boolean connect() {
        synchronized (lock) {
            if (clientUsed) {
                throw new IllegalStateException("The client was already used, cannot be used again!");
            }

            clientUsed = true;

            asyncGDSClient.connect();
            try {
                if (!connectLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                    connectionFailureReason = Either.fromLeft(new GDSTimeoutException("The GDS did not reply within "
                    + timeout + "ms! (Is the URI correct?)"));
                    return false;
                }
                return (asyncGDSClient.getState() == ConnectionState.LOGGED_IN);
            } catch (InterruptedException e) {
                log.severe(e.getMessage());
                throw new Error(e);
            }
        }
    }

    /**
     * Closes the connection towards the GDS.
     */
    public void close() {
        synchronized (lock) {
            asyncGDSClient.close();
            try {
                closeLatch.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.severe(e.getMessage());
                throw new Error(e);

            }
        }
    }


    /**
     * Returns whether the connection failed because of network error (invalid URI / server response timeout).
     *
     * @return true if the connection was not successful, false otherwise.
     */
    public boolean hasConnectionFailed() {
        return connectionFailureReason != null && connectionFailureReason.isLeftSet();
    }

    /**
     * Returns whether the connection failed because of invalid login credentials.
     *
     * @return true if the login was not successful, false on successful login or connection errors.
     */
    public boolean hasLoginFailed() {
        return connectionFailureReason != null && connectionFailureReason.isRightSet();
    }

    /**
     * Returns the {@link Throwable} instance containing the error while the client tried to create the WebSocket connection
     * towards the GDS instance
     * <p>
     * Should be only invoked if {@link SyncGDSClient#hasConnectionFailed()} returns {@code true}.
     *
     * @return the reason why the connection failed to set up
     */
    public Throwable getConnectionError() {
        return connectionFailureReason.getLeft();
    }


    /**
     * Returns the {@link Throwable} instance containing the error while the client tried to create the WebSocket connection
     * towards the GDS instance
     * <p>
     * Should be only invoked if {@link SyncGDSClient#hasConnectionFailed()} returns {@code true}.
     *
     * @return the reason login failed
     */
    public Pair<MessageHeaderBase, MessageData1ConnectionAck> getLoginFailureReason() {
        return connectionFailureReason.getRight();
    }

    /**
     * Returns the result of the login. If the connection failed, returns {@code null}.
     * On failure, the {@link SyncGDSClient#getLoginFailureReason()}} should be used to determine why the login failed
     * (via {@link Either#getRight()}).
     *
     * @return the login response pair on successful login, {@code null} otherwise
     */
    public Pair<MessageHeaderBase, MessageData1ConnectionAck> getLoginResponse() {
        return loginResponse.get();
    }

    //<editor-fold desc="Methods overloaded to send different message types to the GDS">

    /**
     * Sends an event message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param operations     The list of strings containing the event operations.
     * @param binaryContents The attachments sent along with the message.
     * @param priorityLevels The priority levels
     * @return the event ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */

    public EventResponse sendEvent2(String operations,
                                    Map<String, byte[]> binaryContents,
                                    List<PriorityLevelHolder> priorityLevels)
            throws IOException, ValidationException {
        return sendEvent2(MessageManager.createMessageHeaderBase(userName, MessageDataType.EVENT_2),
                MessageManager.createMessageData2Event(operations, binaryContents, priorityLevels));
    }


    /**
     * Sends an event message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param operations     The list of strings containing the event operations.
     * @param binaryContents The attachments sent along with the message.
     * @param priorityLevels The priority levels
     * @return the event ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */

    public EventResponse sendEvent2(List<String> operations,
                                    Map<String, byte[]> binaryContents,
                                    List<PriorityLevelHolder> priorityLevels)
            throws IOException, ValidationException {
        return sendEvent2(MessageManager.createMessageHeaderBase(userName, MessageDataType.EVENT_2),
                MessageManager.createMessageData2Event(operations, binaryContents, priorityLevels));
    }

    /**
     * Sends an event message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param event the event to be sent to the GDS.
     * @return the event ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public EventResponse sendEvent2(MessageData2Event event)
            throws IOException, ValidationException {
        return sendEvent2(MessageManager.createMessageHeaderBase(userName, MessageDataType.EVENT_2), event);
    }

    /**
     * Sends an event message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param messageID the messageID to be used in the header.
     * @param event     the event to be sent to the GDS.
     * @return the event ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */

    public EventResponse sendEvent2(String messageID, MessageData2Event event)
            throws IOException, ValidationException {
        return sendEvent2(MessageManager.createMessageHeaderBase(userName, messageID, MessageDataType.EVENT_2), event);
    }

    /**
     * Sends an event message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param header the message header
     * @param event  the event to be sent to the GDS.
     * @return the event ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public EventResponse sendEvent2(MessageHeaderBase header, MessageData2Event event)
            throws IOException, ValidationException {
        String messageID = header.getMessageId();
        return awaitEventACK3(processOutgoingMessage(messageID, () -> asyncGDSClient.sendEvent2(header, event)), messageID);
    }


    /**
     * Sends an attachment request message, awaiting the reply. If the GDS does not respond within the given time limit
     * ({@code timeout}), will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     * Since the GDS might not have the attachment stored, it is possible that the first reply will not contain any binaries,
     * and the GDS will send it later in an other message, so the attachment itself will be sent in either
     * a {@link MessageData5AttachmentRequestAck} or an {@link MessageData6AttachmentResponse} type of message.
     * Therefore the return type will be an {@link Either} type as well.
     *
     * @param request the attachment request to be sent to the GDS.
     * @return the attachment result
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public AttachmentResult
    sendAttachmentRequest4(String request) throws IOException, ValidationException {
        return sendAttachmentRequest4(MessageManager.createMessageHeaderBase(userName, MessageDataType.ATTACHMENT_REQUEST_4),
                MessageManager.createMessageData4AttachmentRequest(request));
    }

    /**
     * Sends an attachment request message, awaiting the reply. If the GDS does not respond within the given time limit
     * ({@code timeout}), will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     * Since the GDS might not have the attachment stored, it is possible that the first reply will not contain any binaries,
     * and the GDS will send it later in an other message, so the attachment itself will be sent in either
     * a {@link MessageData5AttachmentRequestAck} or an {@link MessageData6AttachmentResponse} type of message.
     * Therefore the return type will be an {@link Either} type as well.
     *
     * @param request the attachment request to be sent to the GDS.
     * @return the attachment result
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public AttachmentResult
    sendAttachmentRequest4(MessageData4AttachmentRequest request) throws IOException, ValidationException {
        return sendAttachmentRequest4(MessageManager.createMessageHeaderBase(userName, MessageDataType.ATTACHMENT_REQUEST_4), request);
    }


    /**
     * Sends an attachment request message, awaiting the reply. If the GDS does not respond within the given time limit
     * ({@code timeout}), will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     * Since the GDS might not have the attachment stored, it is possible that the first reply will not contain any binaries,
     * and the GDS will send it later in an other message, so the attachment itself will be sent in either
     * a {@link MessageData5AttachmentRequestAck} or an {@link MessageData6AttachmentResponse} type of message.
     * Therefore the return type will be an {@link Either} type as well.
     *
     * @param messageID the message ID to be used in the header
     * @param request   the attachment request to be sent to the GDS.
     * @return the attachment result
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public AttachmentResult
    sendAttachmentRequest4(String messageID, MessageData4AttachmentRequest request) throws IOException, ValidationException {
        return sendAttachmentRequest4(MessageManager.createMessageHeaderBase(userName, messageID, MessageDataType.ATTACHMENT_REQUEST_4), request);
    }

    /**
     * Sends an attachment request message, awaiting the reply. If the GDS does not respond within the given time limit
     * ({@code timeout}), will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     * Since the GDS might not have the attachment stored, it is possible that the first reply will not contain any binaries,
     * and the GDS will send it later in an other message, so the attachment itself will be sent in either
     * a {@link MessageData5AttachmentRequestAck} or an {@link MessageData6AttachmentResponse} type of message.
     * Therefore the return type will be an {@link Either} type as well.
     *
     * @param header  the message header to be used
     * @param request the attachment request to be sent to the GDS.
     * @return the attachment result
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public AttachmentResult
    sendAttachmentRequest4(MessageHeaderBase header, MessageData4AttachmentRequest request) throws IOException, ValidationException {
        String messageID = header.getMessageId();
        return awaitAttachment(processOutgoingMessage(messageID, () -> asyncGDSClient.sendAttachmentRequest4(header, request)), messageID);
    }

    /**
     * Sends an event document response message, awaiting the reply. If the GDS does not respond within the given
     * time limit ({@code timeout}), will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param tableName    the table name
     * @param fieldHolders the field holder values
     * @param records      the records
     * @return The event document ACK message sent by the GDS.
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public EventDocumentResponse sendEventDocument8(String tableName,
                                                    List<FieldHolder> fieldHolders,
                                                    List<List<Value>> records) throws IOException, ValidationException {
        return sendEventDocument8(MessageManager.createMessageData8EventDocument(tableName, fieldHolders, records));
    }


    /**
     * Sends an event document message, awaiting the reply. If the GDS does not respond within the given
     * time limit ({@code timeout}), will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param tableName        the table name
     * @param fieldHolders     the field holder values
     * @param records          the records
     * @param returningOptions the returning fields
     * @return The event document ACK message sent by the GDS.
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public EventDocumentResponse sendEventDocument8(String tableName,
                                                    List<FieldHolder> fieldHolders,
                                                    List<List<Value>> records,
                                                    Map<Integer, List<String>> returningOptions) throws IOException, ValidationException {
        return sendEventDocument8(MessageManager.createMessageData8EventDocument(tableName, fieldHolders, records, returningOptions));
    }

    /**
     * Sends an event document message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param eventDocument the event document to be sent to the GDS.
     * @return the event document ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public EventDocumentResponse sendEventDocument8(MessageData8EventDocument eventDocument)
            throws IOException, ValidationException {
        return sendEventDocument8(MessageManager.createMessageHeaderBase(userName, MessageDataType.EVENT_DOCUMENT_8), eventDocument);
    }

    /**
     * Sends an event document message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param messageID     the message ID to be used in the header
     * @param eventDocument the event document to be sent to the GDS.
     * @return the event document ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public EventDocumentResponse sendEventDocument8(String messageID, MessageData8EventDocument eventDocument)
            throws IOException, ValidationException {
        return sendEventDocument8(MessageManager.createMessageHeaderBase(userName, messageID, MessageDataType.EVENT_DOCUMENT_8), eventDocument);
    }

    /**
     * Sends an event document message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param header        the message header
     * @param eventDocument the event document to be sent to the GDS.
     * @return the event document ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public EventDocumentResponse sendEventDocument8(MessageHeaderBase header, MessageData8EventDocument eventDocument)
            throws IOException, ValidationException {
        String messageID = header.getMessageId();
        return awaitEventDocumentACK9(processOutgoingMessage(messageID, () -> asyncGDSClient.sendEventDocument8(header, eventDocument)), messageID);
    }


    /**
     * Sends a query message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @return the query ACK with the result.
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public QueryResponse sendQueryRequest10(
            String query,
            ConsistencyType consistencyType,
            Long timeout) throws IOException, ValidationException {
        return sendQueryRequest10(MessageManager.createMessageHeaderBase(userName, MessageDataType.QUERY_REQUEST_10),
                MessageManager.createMessageData10QueryRequest(query, consistencyType, timeout));
    }

    /**
     * Sends a query message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @param pageSize        the page size used for the query
     * @param queryType       the type of the query (scroll/page)
     * @return the query ACK with the result.
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public QueryResponse sendQueryRequest10(
            String query,
            ConsistencyType consistencyType,
            Long timeout,
            Integer pageSize,
            Integer queryType) throws IOException, ValidationException {
        return sendQueryRequest10(MessageManager.createMessageHeaderBase(userName, MessageDataType.QUERY_REQUEST_10),
                MessageManager.createMessageData10QueryRequest(query, consistencyType, timeout, pageSize, queryType));
    }

    /**
     * Sends a query message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param request the query request to be sent to the GDS.
     * @return the query ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public QueryResponse sendQueryRequest10(MessageData10QueryRequest request)
            throws IOException, ValidationException {
        return sendQueryRequest10(MessageManager.createMessageHeaderBase(userName, MessageDataType.QUERY_REQUEST_10), request);
    }


    /**
     * Sends a query request, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param messageID the message ID to be used in the header
     * @param request   the query request to be sent to the GDS.
     * @return the query ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public QueryResponse sendQueryRequest10(String messageID, MessageData10QueryRequest request)
            throws IOException, ValidationException {
        return sendQueryRequest10(MessageManager.createMessageHeaderBase(userName, messageID, MessageDataType.QUERY_REQUEST_10), request);
    }


    /**
     * Sends a query request, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param header  the message header
     * @param request the query request to be sent to the GDS.
     * @return the query ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public QueryResponse sendQueryRequest10(MessageHeaderBase header, MessageData10QueryRequest request)
            throws IOException, ValidationException {
        String messageID = header.getMessageId();
        return awaitQueryACK11(processOutgoingMessage(messageID, () -> asyncGDSClient.sendQueryRequest10(header, request)), messageID);
    }


    /**
     * Sends a next query page request message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param queryContextHolder the ContextHolder containing information about the current query status
     * @param timeout            the timeout used in the GDS for the query
     * @return the query ACK with the result.
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public QueryResponse sendNextQueryPage12(
            QueryContextHolder queryContextHolder, Long timeout) throws IOException, ValidationException {
        return sendNextQueryPage12(MessageManager.createMessageHeaderBase(userName, MessageDataType.NEXT_QUERY_PAGE_12),
                MessageManager.createMessageData12NextQueryPage(queryContextHolder, timeout));
    }


    /**
     * Sends a next query page request message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param queryContextHolder the ContextHolder containing information about the current query status
     * @param timeout            the timeout used in the GDS for the query
     * @return the query ACK with the result.
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public QueryResponse sendNextQueryPage12(
            QueryContextHolderSerializable queryContextHolder,
            Long timeout) throws IOException, ValidationException {
        return sendNextQueryPage12(MessageManager.createMessageHeaderBase(userName, MessageDataType.NEXT_QUERY_PAGE_12),
                MessageManager.createMessageData12NextQueryPage(queryContextHolder, timeout));
    }

    /**
     * Sends a next query page request message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param request the query request to be sent to the GDS.
     * @return the query ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public QueryResponse sendNextQueryPage12(MessageData12NextQueryPage request)
            throws IOException, ValidationException {
        return sendNextQueryPage12(MessageManager.createMessageHeaderBase(userName, MessageDataType.NEXT_QUERY_PAGE_12), request);
    }

    /**
     * Sends a next query page request message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param messageID the message ID to be used in the header
     * @param request   the query request to be sent to the GDS.
     * @return the query ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public QueryResponse sendNextQueryPage12(String messageID, MessageData12NextQueryPage request)
            throws IOException, ValidationException {
        return sendNextQueryPage12(MessageManager.createMessageHeaderBase(userName, messageID, MessageDataType.NEXT_QUERY_PAGE_12), request);
    }


    /**
     * Sends a next query page request message, awaiting the reply. If the GDS does not respond within the given time limit ({@code timeout}),
     * will throw a {@link GDSTimeoutException}.
     * Otherwise returns the response given by the GDS.
     *
     * @param header  the message header
     * @param request the query request to be sent to the GDS.
     * @return the query ACK with the result.
     * @throws IOException         if the message cannot be packed
     * @throws ValidationException if any value constraints the restrictions in the structure of the header or the body.
     */
    public QueryResponse sendNextQueryPage12(MessageHeaderBase header, MessageData12NextQueryPage request)
            throws IOException, ValidationException {
        String messageID = header.getMessageId();
        return awaitQueryACK11(processOutgoingMessage(messageID, () -> asyncGDSClient.sendNextQueryPage12(header, request)), messageID);
    }
    // </editor-fold>


    /**
     * Creates a {@link CountDownLatch} for the given message, puts it into a cache by the specified message ID
     * and invokes the operation that's necessary to send it.
     *
     * @param messageID        the ID for the message that will be used to await it and avoid multiple messages with the same ID.
     * @param messageOperation the operation to send the message
     * @return the created CountDownLatch used to await the message
     * @throws IOException              if the message cannot be packed
     * @throws ValidationException      if any value constraints the restrictions in the structure of the header or the body.
     * @throws IllegalArgumentException if the given messageID is already used for an outgoing message which
     */
    private CountDownLatch processOutgoingMessage(String messageID, MessageOperation messageOperation) throws IOException, ValidationException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        if (incomingCache.putIfAbsent(messageID, new Pair<>(countDownLatch, null)) != null) {
            throw new IllegalStateException("There is already an outgoing message with the ID " + messageID + "!");
        }
        messageOperation.run();
        return countDownLatch;
    }

    /**
     * Awaits a general type of message, returning with the result. If timeout occurs, throws {@link GDSTimeoutException}.
     *
     * @param countDownLatch the CountDownLatch attached to the current message.
     * @param messageID      the ID of the message the client is currently waiting for
     * @return the header and the data for the given message
     * @throws GDSTimeoutException  if the message does not arrive in time specified by {@link SyncGDSClient#timeout}
     * @throws InterruptedException if the current thread gets interrupted
     */
    private Pair<MessageHeaderBase, MessageData> awaitMessage(CountDownLatch countDownLatch, String messageID) throws InterruptedException {
        if (!countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
            incomingCache.remove(messageID);
            throw new GDSTimeoutException("The GDS did not reply in time for the request with ID: " + messageID);
        }
        return incomingCache.remove(messageID).getSecond();
    }

    /**
     * Awaits an event ACK message, returning with the result. If timeout occurs, throws {@link GDSTimeoutException}.
     *
     * @param countDownLatch the CountDownLatch attached to the current message.
     * @param messageID      the ID of the message the client is currently waiting for
     * @return the header and the data for the given message
     * @throws GDSTimeoutException if the message does not arrive in time specified by {@link SyncGDSClient#timeout}
     * @throws Error               if the current thread gets interrupted
     */
    private EventResponse awaitEventACK3(CountDownLatch countDownLatch, String messageID) {
        try {
            Pair<MessageHeaderBase, MessageData> resultPair = awaitMessage(countDownLatch, messageID);
            if (resultPair.getSecond().getTypeHelper().isEventAckMessageData3()) {
                return new EventResponse(resultPair.getFirst(), resultPair.getSecond().getTypeHelper().asEventAckMessageData3());
            } else {
                String msg = "The type for the reply for the message with ID " + messageID + " is invalid.";
                log.config(msg);
                throw new IllegalStateException(msg);
            }

        } catch (InterruptedException e) {
            incomingCache.remove(messageID);
            log.severe(e.getMessage());
            throw new Error(e);
        }
    }


    /**
     * Awaits an attachment message, returning with the result. If timeout occurs, throws {@link GDSTimeoutException}.
     *
     * @param countDownLatch the CountDownLatch attached to the current message.
     * @param messageID      the ID of the message the client is currently waiting for
     * @return the header and the data for the given message
     * @throws GDSTimeoutException if the message does not arrive in time specified by {@link SyncGDSClient#timeout}
     * @throws Error               if the current thread gets interrupted
     */
    private AttachmentResult awaitAttachment(CountDownLatch countDownLatch, String messageID) {
        try {
            Pair<MessageHeaderBase, MessageData> resultPair = awaitMessage(countDownLatch, messageID);
            if (resultPair.getSecond().getTypeHelper().isAttachmentRequestAckMessageData5()) {
                MessageData5AttachmentRequestAck messageData5 = resultPair.getSecond().getTypeHelper().asAttachmentRequestAckMessageData5();
                if (messageData5.getData() == null || messageData5.getData().getResult().getAttachment() != null) {
                    return new AttachmentResult(resultPair.getFirst(),
                            Either.fromLeft(resultPair.getSecond().getTypeHelper().asAttachmentRequestAckMessageData5()));
                } else {
                    try {
                        log.config("The incoming message with the ID " + messageID + " did not contain the attachment, " +
                                "awaiting it from the GDS..");

                        //we do not have to send anything at all as it's an incoming message without anything left
                        // to send to the GDS.
                        resultPair = awaitMessage(processOutgoingMessage(messageID, MessageOperation.SKIP), messageID);
                        MessageData6AttachmentResponse attachmentResponse = (MessageData6AttachmentResponse) resultPair.getSecond();
                        try {
                            log.config("Sending attachment ACK for the received attachment response..");
                            asyncGDSClient.sendAttachmentResponseAck7(messageID, MessageManager.createMessageData7AttachmentResponseAck(
                                    AckStatus.OK,
                                    new AttachmentResponseAckResultHolderImpl(AckStatus.CREATED,
                                            new AttachmentResultHolderImpl(
                                                    attachmentResponse.getResult().getRequestIds(),
                                                    attachmentResponse.getResult().getOwnerTable(),
                                                    attachmentResponse.getResult().getAttachmentId()
                                            )),
                                    null));
                        } catch (IOException | ValidationException e) {
                            //this should never happen as the message creation only contains valid values above.
                            log.severe(e.toString());
                        }
                        return new AttachmentResult(resultPair.getFirst(), Either.fromRight(attachmentResponse));

                    } catch (IOException | ValidationException e) {
                        //this should never happen as our awaitMessage runnable is an empty statement
                        //without any operations therefore it cannot throw these
                        log.severe(e.toString());
                        throw new Error("Unexpected error happened while waiting for the ACKResponse!", e);
                    }
                }
            } else {
                String msg = "The type for the reply for the message with ID " + messageID + " is invalid.";
                log.config(msg);
                throw new IllegalStateException(msg);
            }

        } catch (InterruptedException e) {
            incomingCache.remove(messageID);
            log.severe(e.getMessage());
            throw new Error(e);
        }
    }

    /**
     * Awaits an event document ACK message, returning with the result. If timeout occurs, throws {@link GDSTimeoutException}.
     *
     * @param countDownLatch the CountDownLatch attached to the current message.
     * @param messageID      the ID of the message the client is currently waiting for
     * @return the header and the data for the given message
     * @throws GDSTimeoutException if the message does not arrive in time specified by {@link SyncGDSClient#timeout}
     * @throws Error               if the current thread gets interrupted
     */
    private EventDocumentResponse awaitEventDocumentACK9(CountDownLatch countDownLatch, String messageID) {
        try {
            Pair<MessageHeaderBase, MessageData> resultPair = awaitMessage(countDownLatch, messageID);
            if (resultPair.getSecond().getTypeHelper().isEventDocumentAckMessageData9()) {
                return new EventDocumentResponse(resultPair.getFirst(), resultPair.getSecond().getTypeHelper().asEventDocumentAckMessageData9());
            } else {
                String msg = "The type for the reply for the message with ID " + messageID + " is invalid.";
                log.config(msg);
                throw new IllegalStateException(msg);
            }

        } catch (InterruptedException e) {
            incomingCache.remove(messageID);
            log.severe(e.getMessage());
            throw new Error(e);
        }
    }

    /**
     * Awaits a query ACK message, returning with the result. If timeout occurs, throws {@link GDSTimeoutException}.
     *
     * @param countDownLatch the CountDownLatch attached to the current message.
     * @param messageID      the ID of the message the client is currently waiting for
     * @return the header and the data for the given message
     * @throws GDSTimeoutException if the message does not arrive in time specified by {@link SyncGDSClient#timeout}
     * @throws Error               if the current thread gets interrupted
     */
    private QueryResponse awaitQueryACK11(CountDownLatch countDownLatch, String messageID) {
        try {
            Pair<MessageHeaderBase, MessageData> resultPair = awaitMessage(countDownLatch, messageID);
            if (resultPair.getSecond().getTypeHelper().isQueryRequestAckMessageData11()) {
                return new QueryResponse(resultPair.getFirst(), resultPair.getSecond().getTypeHelper().asQueryRequestAckMessageData11());
            } else {
                String msg = "The type for the reply for the message with ID " + messageID + " is invalid.";
                log.config(msg);
                throw new IllegalStateException(msg);
            }

        } catch (InterruptedException e) {
            incomingCache.remove(messageID);
            log.severe(e.getMessage());
            throw new Error(e);
        }
    }
}
