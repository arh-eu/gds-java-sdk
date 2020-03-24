package hu.arh.gds.message.data;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData5AttachmentRequestAck extends MessageData implements MessageData5AttachmentRequestAckDescriptor {

    public MessageData5AttachmentRequestAck(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData5AttachmentRequestAck(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageData5AttachmentRequestAck() throws IOException { }
}
