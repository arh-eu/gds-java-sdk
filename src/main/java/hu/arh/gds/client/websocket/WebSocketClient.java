package hu.arh.gds.client.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class WebSocketClient {
    private final URI URI;
    private SslContext sslCtx;
    private final String host;
    private final int port;
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel ch;
    private ChannelFuture channelFuture;

    private BinaryMessageListener binaryMessageListener;

    private final Logger logger;

    public WebSocketClient(String url, String cert, String secret, Logger logger) throws Throwable {
        this.logger = logger;
        this.URI = new URI(url);
        String scheme = URI.getScheme();
        host = URI.getHost();
        port = URI.getPort();


        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            logger.severe("Only WS(S) is supported");
            return;
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        if (ssl) {
            try {
                loadTLS(cert, secret);
            } catch (FileNotFoundException fnfe) {
                throw new IllegalArgumentException(String.format("The specified file for your TLS private key (%1$s) could not be found!", cert));
            }
            logger.info("SSL context initialized!");
        } else {
            sslCtx = null;
        }
    }

    private void loadTLS(String cert, String secret) throws Throwable {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream in = new FileInputStream(cert);
        char[] password = secret.toCharArray();
        keyStore.load(in, password);

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
        //(certificates, (PrivateKey)key);

        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(null, null);
        keyStore.setKeyEntry("server", key, new char[0],
                certificates.toArray(new X509Certificate[0]));
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, new char[0]);


        sslCtx = SslContextBuilder
                .forClient()
                .keyManager(keyManagerFactory)
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    public void setBinaryMessageListener(BinaryMessageListener binaryMessageListener) {
        this.binaryMessageListener = binaryMessageListener;
    }

    public ChannelFuture connect() throws Throwable {
        if (!isActive()) {
            if (isOpen()) {
                close();
            }
            try {
                group = new NioEventLoopGroup();
                WebSocketClientHandler webSocketClientHandler = new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                URI, WebSocketVersion.V13, null, true,
                                new DefaultHttpHeaders()), binaryMessageListener, logger, group);
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                if (sslCtx != null) {
                                    p.addFirst(sslCtx.newHandler(ch.alloc(), host, port));
                                }
                                p.addLast(
                                        new HttpClientCodec(),
                                        new HttpObjectAggregator(8192),
                                        WebSocketClientCompressionHandler.INSTANCE,
                                        webSocketClientHandler);
                            }
                        });
                ch = bootstrap.connect(URI.getHost(), port).sync().channel();
                logger.info("Awaiting handshake...");
                this.channelFuture = webSocketClientHandler.handshakeFuture().sync();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                logger.info(throwable.toString());
                if (binaryMessageListener != null) {
                    binaryMessageListener.onConnectionFailed(throwable.getMessage());
                }
                group.shutdownGracefully();
                throw throwable;
            }
        }
        return channelFuture;
    }

    public ChannelFuture send(byte[] msg) throws Throwable {
        if (!isActive()) {
            connect();
        }
        WebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(msg));
        logger.info("WebSocketClient sending BinaryWebSocketFrame...");
        return ch.writeAndFlush(frame);
    }

    public void close() throws InterruptedException {
        if (isOpen()) {
            logger.info("WebSocketClient closing channel...");
            ch.writeAndFlush(new CloseWebSocketFrame());
            //ch.closeFuture().sync();
            ch.closeFuture();
            group.shutdownGracefully();
        }
    }

    public boolean isActive() {
        return ch != null && ch.isActive();
    }

    public boolean isOpen() {
        return ch != null && ch.isOpen();
    }
}

