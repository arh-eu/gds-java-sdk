package hu.arh.gds.client;

import hu.arh.gds.message.header.MessageDataType;

import java.io.IOException;

public abstract class ReceivedMessageHandler {

    boolean isMessageSendingProcessEnd = false;

    abstract void messageReceived(byte[] message) throws IOException;
}
