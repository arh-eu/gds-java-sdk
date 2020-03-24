package hu.arh.gds.message.data;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData6AttachmentResponse extends MessageData implements MessageData6AttachmentResponseDescriptor {
   
    public MessageData6AttachmentResponse(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData6AttachmentResponse(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageData6AttachmentResponse() throws IOException { }
}
