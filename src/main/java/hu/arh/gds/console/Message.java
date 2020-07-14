package hu.arh.gds.console;

import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.header.MessageHeader;

public class Message {
    private MessageHeader header;
    private MessageData data;

    public Message(MessageHeader header, MessageData data) {
        this.header = header;
        this.data = data;
    }

    public MessageHeader getHeader() {
        return this.header;
    }

    public MessageData getData() {
        return this.data;
    }
}
