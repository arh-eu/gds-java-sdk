package hu.arh.gds.client;

import hu.arh.gds.client.websocket.ResponseHandler;
import hu.arh.gds.client.websocket.WebSocketClient;
import hu.arh.gds.message.header.MessageDataType;

import java.util.logging.Logger;

public class Client {
    private WebSocketClient webSocketClient;
    private ResponseHandler responseHandler;
    private ReceivedMessageHandler receivedMessageHandler;

    public static final Logger logger = Logger.getLogger("logging");

    public Client(String url) {
        this.responseHandler = new ResponseHandler() {
            @Override
            public void handleResponse(byte[] message) {
                hu.arh.gds.client.Client.this.handleResponse(message);
            }

            @Override
            public void handleResponse(String message) {
                hu.arh.gds.client.Client.this.handleResponse(message);
            }
        };
        try {
            this.webSocketClient = new WebSocketClient(url, responseHandler);
        } catch (Throwable throwable) {
            logger.severe("An error occured while creating client simulator: " + throwable.getMessage());
        }
    }

    public Client(String url, ReceivedMessageHandler receivedMessageHandler) {
        this.responseHandler = new ResponseHandler() {
            @Override
            public void handleResponse(byte[] message) {
                hu.arh.gds.client.Client.this.handleResponse(message);
            }

            @Override
            public void handleResponse(String message) {
                hu.arh.gds.client.Client.this.handleResponse(message);
            }
        };
        this.receivedMessageHandler = receivedMessageHandler;
        try {
            this.webSocketClient = new WebSocketClient(url, responseHandler);
        } catch (Throwable throwable) {
            logger.severe("An error occured while creating client simulator: " + throwable.getMessage());
        }
    }

    public void connect() {
        try {
            webSocketClient.connect();
        } catch (Throwable throwable) {
            logger.severe("An error occured while connecting to server: " + throwable.getMessage());
        }
    }

    public void close() {
        try {
            webSocketClient.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.severe("An error occured while closing connection: " + throwable.getMessage());
        }
    }

    public void sendMessage(byte[] message) {
        try {
            logger.info("WebSocket Client sending message");
            webSocketClient.send(message);
        } catch (Throwable throwable) {
            logger.severe("An error occured while sending message: " + throwable.getMessage());
        }
    }

    public void handleResponse(byte[] message) {
        try {
            logger.info("WebSocket Client received message");
            if(receivedMessageHandler != null) {
                receivedMessageHandler.messageReceived(message);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.severe("An error occured while handling response message: " + throwable.getMessage());
        }
    }

    public void handleResponse(String message) {
        logger.info("Websocket Client received message: " + message);
    }
}