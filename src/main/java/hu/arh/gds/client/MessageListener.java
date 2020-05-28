package hu.arh.gds.client;

import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.header.MessageHeader;

import java.io.IOException;

public abstract class MessageListener {
    public abstract void onMessageReceived(MessageHeader header, MessageData data);
}
