package hu.arh.gds.client;

import hu.arh.gds.client.websocket.BinaryMessageListener;
import hu.arh.gds.client.websocket.ConnectionStateListener;
import hu.arh.gds.client.websocket.WebSocketClient;
import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.data.MessageData1ConnectionAck;
import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.util.MessageManager;
import hu.arh.gds.message.util.ValidationException;
import hu.arh.gds.message.util.WriteException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class GDSWebSocketClient {
    private WebSocketClient webSocketClient;

    private BinaryMessageListener binaryMessageListener;
    private MessageListener messageListener;
    private ConnectionStateListener connectionStateListener;

    private final String userName;
    private final String password;

    private Log log;

    private AtomicBoolean connectionAckMessageReceived = new AtomicBoolean(false);

    public GDSWebSocketClient(String url, String userName, String password, Log log) {
        this.log = log;
        try {
            this.webSocketClient = new WebSocketClient(url, log);
            this.webSocketClient.setBinaryMessageListener(new BinaryMessageListener() {
                @Override
                public void onMessageReceived(byte[] message) {
                    GDSWebSocketClient.this.onMessageReceived(message);
                }
            });
            this.webSocketClient.setConnectionStateListener(new ConnectionStateListener() {
                @Override
                public void onConnected() { }

                @Override
                public void onDisconnected() {
                    GDSWebSocketClient.this.onWebSocketDisconnected();
                }
            });
        } catch (Throwable throwable) {
            log.error("An error occured while creating client simulator: " + throwable.getMessage());
        }
        this.userName = userName;
        this.password = password;
    }

    public void setBinaryMessageListener(BinaryMessageListener binaryMessageListener) {
        this.binaryMessageListener = binaryMessageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setConnectionStateListener(ConnectionStateListener connectionStateListener) {
        this.connectionStateListener = connectionStateListener;
    }

    public void connect() throws WriteException, IOException, ValidationException {
        if(!webSocketClientConnected()) {
            connectWebSocketClient();
        }
        if(!connectionAckMessageReceived.get()) {
            sendConnectionMessage();
        }
    }

    public boolean connected() {
        return webSocketClientConnected() && connectionAckMessageReceived.get();
    }

    public void close() {
        closeWebSocketConnection();
        connectionAckMessageReceived.set(false);
    }

    public void sendMessage(byte[] message) {
        try {
            if (webSocketClient != null) {
                log.info("GDSWebSocketClient sending message");
                webSocketClient.send(message);
            }
        } catch (Throwable throwable) {
            log.error("An error occurred while sending message: " + throwable.getMessage());
        }
    }

    public void sendMessage(MessageHeader header, MessageData data) throws ValidationException, IOException, WriteException {
        sendMessage(MessageManager.createMessage(header, data));
    }

    public void sendMessage(MessageData data, String messageId) throws IOException, ValidationException, WriteException {
        MessageHeader header = MessageManager.createMessageHeaderBase(
                userName,
                messageId != null ? messageId : UUID.randomUUID().toString(),
                false,
                null,
                null,
                null,
                null,
                data.getTypeHelper().getMessageDataType());
        byte[] message = MessageManager.createMessage(header, data);
        sendMessage(message);
    }

    public void sendMessage(MessageData data) throws IOException, ValidationException, WriteException {
        sendMessage(data, null);
    }

    private void sendConnectionMessage() throws IOException, ValidationException, WriteException {
        MessageHeader header = MessageManager.createMessageHeaderBase(userName, UUID.randomUUID().toString(),
                false, null, null, null, null, MessageDataType.CONNECTION_0);
        MessageData data = MessageManager.createMessageData0Connection(true, 1, false,
                null, password);
        byte[] message = MessageManager.createMessage(header, data);
        sendMessage(message);
    }

    private void connectWebSocketClient() {
        try {
            if (webSocketClient != null) {
                webSocketClient.connect();
            }
        } catch (Throwable throwable) {
            log.error("An error occurred while connecting to server: " + throwable.getMessage());
        }
    }

    private boolean webSocketClientConnected() {
        if (webSocketClient != null) {
            return webSocketClient.isActive();
        } else {
            return false;
        }
    }

    private void closeWebSocketConnection() {
        try {
            if (webSocketClient != null) {
                webSocketClient.close();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.error("An error occurred while closing connection: " + throwable.getMessage());
        }
    }

    private void onWebSocketDisconnected() {
        connectionAckMessageReceived.set(false);
        connectionStateListener.onDisconnected();
    }

    private void onMessageReceived(byte[] message) {
        try {
            if(!connectionAckMessageReceived.get()) {
                if (MessageManager.getMessageDataType(message).equals(MessageDataType.CONNECTION_ACK_1)) {
                    MessageData1ConnectionAck ackData = MessageManager.getMessageData(message).getTypeHelper().asConnectionAckMessageData1();
                    if (ackData.getGlobalStatus().equals(AckStatus.OK)) {
                        connectionAckMessageReceived.set(true);
                        connectionStateListener.onConnected();
                    }
                    return;
                }
            }
            log.info("GDSWebSocketClient received message");
            if (binaryMessageListener != null) {
                binaryMessageListener.onMessageReceived(message);
            }
            if(messageListener != null) {
                messageListener.onMessageReceived(
                        MessageManager.getMessageHeaderFromBinaryMessage(message),
                        MessageManager.getMessageData(message));
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.error("An error occurred while handling response message: " + throwable.getMessage());
        }
    }
}