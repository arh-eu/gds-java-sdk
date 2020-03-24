package hu.arh.gds.message.data;

import java.util.List;

public interface MessageData9EventDocumentAckDescriptor extends Ack {
    List<EventDocumentResultHolder> getResults();
}
