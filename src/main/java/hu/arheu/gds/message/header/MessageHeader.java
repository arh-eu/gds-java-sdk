package hu.arheu.gds.message.header;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.util.ReadException;
import hu.arheu.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageHeader extends MessagePart {

    protected MessageHeaderTypeHelper typeHelper;

    public MessageHeaderTypeHelper getTypeHelper() {
        return this.typeHelper;
    }

    public MessageHeader(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageHeader(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageHeader() throws IOException {

    }
}
