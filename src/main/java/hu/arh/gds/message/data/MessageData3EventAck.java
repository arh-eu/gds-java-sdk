package hu.arh.gds.message.data;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData3EventAck extends MessageData implements MessageData3EventAckDescriptor {
    
    public MessageData3EventAck(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData3EventAck(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }
    
    public MessageData3EventAck() throws IOException { }
}
