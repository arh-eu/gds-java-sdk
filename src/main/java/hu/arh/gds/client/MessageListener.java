package hu.arh.gds.client;

import hu.arh.gds.client.websocket.ConnectionStateListener;
import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.header.MessageHeader;

public interface MessageListener extends ConnectionStateListener {
    void onMessageReceived(MessageHeader header, MessageData data);
}
