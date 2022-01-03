
package hu.arheu.gds.message.data;

import java.util.Map;


public interface MessageData1ConnectionAck extends Ack, MessageData {
    MessageData0Connection getAckDataOk();

    Map<Integer, String> getAckDataUnauthorizedItems();

    @Override
    default MessageData1ConnectionAck asConnectionAckMessageData1() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isConnectionAckMessageData1() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.CONNECTION_ACK_1;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
