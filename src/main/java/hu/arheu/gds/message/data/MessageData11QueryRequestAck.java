
package hu.arheu.gds.message.data;

public interface MessageData11QueryRequestAck extends Ack, MessageData {
    QueryResponseHolder getQueryResponseHolder();

    @Override
    default MessageData11QueryRequestAck asQueryRequestAckMessageData11() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isQueryRequestAckMessageData11() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.QUERY_REQUEST_ACK_11;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
