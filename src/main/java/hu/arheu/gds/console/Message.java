package hu.arheu.gds.console;

import hu.arheu.gds.message.data.MessageData;
import hu.arheu.gds.message.header.MessageHeader;

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
