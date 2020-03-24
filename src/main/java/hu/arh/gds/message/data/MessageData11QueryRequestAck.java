package hu.arh.gds.message.data;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData11QueryRequestAck extends MessageData implements MessageData11QueryRequestAckDescriptor {

    public MessageData11QueryRequestAck(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData11QueryRequestAck(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageData11QueryRequestAck() throws IOException { }
}
