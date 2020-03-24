package hu.arh.gds.message.data;

import java.util.Map;

public interface MessageData1ConnectionAckDescriptor extends Ack {
    MessageData0ConnectionDescriptor getAckDataOk();
    Map<Integer, String> getAckDataUnauthorizedItems();    
}
