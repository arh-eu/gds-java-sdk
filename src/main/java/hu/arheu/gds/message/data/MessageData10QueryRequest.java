
package hu.arheu.gds.message.data;


public interface MessageData10QueryRequest extends MessageData {
    String getQuery();

    ConsistencyType getConsistencyType();

    Long getTimeout();

    Integer getPageSize();

    Integer getQueryType();

    @Override
    default MessageData10QueryRequest asQueryRequestMessageData10() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isQueryRequestMessageData10() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.QUERY_REQUEST_10;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 5;
    }
}
