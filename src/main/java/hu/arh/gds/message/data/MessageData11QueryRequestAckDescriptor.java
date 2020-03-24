package hu.arh.gds.message.data;

import hu.arh.gds.message.util.PublicElementCountable;

public interface MessageData11QueryRequestAckDescriptor extends PublicElementCountable, Ack {
    QueryResponseHolder getQueryResponseHolder();
}
