package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.ReadException;
import hu.arheu.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData1ConnectionAck extends MessageData implements MessageData1ConnectionAckDescriptor {

    public MessageData1ConnectionAck(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData1ConnectionAck(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageData1ConnectionAck() throws IOException { }
}
