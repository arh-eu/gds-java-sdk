
package hu.arheu.gds.message.data;

import java.util.List;


public interface MessageData9EventDocumentAck extends Ack, MessageData {

    List<EventDocumentResultHolder> getResults();

    @Override
    default MessageData9EventDocumentAck asEventDocumentAckMessageData9() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isEventDocumentAckMessageData9() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.EVENT_DOCUMENT_ACK_9;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
