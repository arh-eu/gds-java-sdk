/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.message.data.MessageData11QueryRequestAck;
import hu.arheu.gds.message.header.MessageHeaderBase;

import java.io.Externalizable;


/**
 * Represents a response to a Query Request (type 10) or a Next Query Page request (type 12) message.
 */
public class QueryResponse {
    private MessageHeaderBase header;
    private MessageData11QueryRequestAck data;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public QueryResponse() {
    }

    public QueryResponse(MessageHeaderBase header, MessageData11QueryRequestAck data) {
        this.header = header;
        this.data = data;
    }

    public MessageHeaderBase getHeader() {
        return header;
    }

    public MessageData11QueryRequestAck getData() {
        return data;
    }
}

