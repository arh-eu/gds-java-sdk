
package hu.arheu.gds.message.data;



public interface MessageData12NextQueryPage extends MessageData {

    QueryContextHolder getQueryContextHolder();

    Long getTimeout();

    @Override
    default MessageData12NextQueryPage asNextQueryPageMessageData12() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isNextQueryPageMessageData12() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.NEXT_QUERY_PAGE_12;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 2;
    }
}
