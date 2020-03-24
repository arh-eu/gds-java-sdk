package hu.arh.gds.message.data;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData8EventDocument extends MessageData implements MessageData8EventDocumentDescriptor {

    public MessageData8EventDocument(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData8EventDocument(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    public MessageData8EventDocument() throws IOException { }
}
