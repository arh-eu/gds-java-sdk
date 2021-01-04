package hu.arheu.gds.message.data;

import java.util.List;

public interface MessageData3EventAckDescriptor extends Ack {
    List<EventResultHolder> getEventResult();
}
