package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.ReadException;
import hu.arheu.gds.message.util.ValidationException;

import java.io.IOException;

public abstract class MessageData12NextQueryPage extends MessageData implements MessageData12NextQueryPageDescriptor {
    
    public MessageData12NextQueryPage(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData12NextQueryPage(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }
    
    public MessageData12NextQueryPage() throws IOException { }
}
