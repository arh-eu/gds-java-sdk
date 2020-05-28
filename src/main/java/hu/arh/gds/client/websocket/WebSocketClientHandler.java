package hu.arh.gds.client.websocket;

import hu.arh.gds.client.Log;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private final ConnectionStateListener connectionStateListener;
    private final BinaryMessageListener binaryMessageListener;

    private Log log;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker,
                                  ConnectionStateListener connectionStateListener,
                                  BinaryMessageListener binaryMessageListener,
                                  Log log) {
        this.log = log;
        this.handshaker = handshaker;
        this.connectionStateListener = connectionStateListener;
        this.binaryMessageListener = binaryMessageListener;
    }



    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(connectionStateListener != null) {
            connectionStateListener.onDisconnected();
        }
        log.info("WebSocketClient disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                log.info("WebSocketClient connected");
                if(connectionStateListener != null) {
                    connectionStateListener.onConnected();
                }
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log.info("WebSocketClient failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if(frame instanceof BinaryWebSocketFrame) {
            log.info("WebSocketClient received BinaryWebSocketFrame");
            byte[] binaryFrame = new byte[frame.content().readableBytes()];
            frame.content().readBytes(binaryFrame);
            if(binaryMessageListener != null) {
                binaryMessageListener.onMessageReceived(binaryFrame);
            }
        } else if (frame instanceof TextWebSocketFrame) {
            log.info("WebSocketClient received TextWebSocketFrame");
        } else if (frame instanceof PongWebSocketFrame) {
            log.info("WebSocketClient received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("WebSocketClient received closing");
            ch.close();
        } else {
            log.info("Unsupported frame type: " + frame.getClass().getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
