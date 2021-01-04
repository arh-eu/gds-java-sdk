package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.PublicElementCountable;

public interface MessageData11QueryRequestAckDescriptor extends PublicElementCountable, Ack {
    QueryResponseHolder getQueryResponseHolder();
}
