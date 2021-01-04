package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.ReadException;
import hu.arheu.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData7AttachmentResponseAck extends MessageData implements MessageData7AttachmentResponseAckDescriptor {

    public MessageData7AttachmentResponseAck(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData7AttachmentResponseAck(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageData7AttachmentResponseAck() throws IOException { }
}
