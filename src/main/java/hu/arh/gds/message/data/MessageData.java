package hu.arh.gds.message.data;

import hu.arh.gds.message.MessagePart;
import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData extends MessagePart {

    protected MessageDataTypeHelper typeHelper;

    public MessageDataTypeHelper getTypeHelper() {
        return this.typeHelper;
    }

    public MessageData(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }
    
    public MessageData() throws IOException { }
}
