package hu.arheu.gds.console;

import hu.arheu.gds.message.data.QueryResponseHolder;

public class QueryAckHolder {
    private final String messageId;
    private final QueryResponseHolder queryResponseHolder;

    public QueryAckHolder(String messageId, QueryResponseHolder queryResponseHolder) {
        this.messageId = messageId;
        this.queryResponseHolder = queryResponseHolder;
    }

    public String getMessageId() {
        return messageId;
    }

    public QueryResponseHolder getQueryResponseHolder() {
        return queryResponseHolder;
    }
}
