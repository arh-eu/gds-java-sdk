
package hu.arheu.gds.message.data;

import java.util.List;
import java.util.Map;


public interface MessageData2Event extends MessageData {

    String getOperations();

    Map<String, byte[]> getBinaryContents();

    List<PriorityLevelHolder> getPriorityLevels();

    @Override
    default MessageData2Event asEventMessageData2() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isEventMessageData2() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.EVENT_2;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
