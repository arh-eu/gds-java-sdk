
package hu.arheu.gds.message.data;

import java.util.List;


public interface MessageData3EventAck extends Ack, MessageData {

    List<EventResultHolder> getEventResult();

    @Override
    default MessageData3EventAck asEventAckMessageData3() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isEventAckMessageData3() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.EVENT_ACK_3;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
