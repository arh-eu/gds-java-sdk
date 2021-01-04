/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.client.Pair;
import hu.arheu.gds.message.data.MessageData11QueryRequestAck;
import hu.arheu.gds.message.header.MessageHeaderBase;


/**
 * Represents a response to a Query Request (type 10) or a Next Query Page request (type 12) message.
 */
public class QueryResponse extends GDSMessage<MessageData11QueryRequestAck> {

    public QueryResponse(Pair<MessageHeaderBase, MessageData11QueryRequestAck> response) {
        super(response);
    }

    public QueryResponse(MessageHeaderBase header, MessageData11QueryRequestAck data) {
        super(header, data);
    }
}

