/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.client.Pair;
import hu.arheu.gds.message.data.MessageData9EventDocumentAck;
import hu.arheu.gds.message.header.MessageHeaderBase;

/**
 * Represents a response to an Event Document message (type 8).
 */
public class EventDocumentResponse extends GDSMessage<MessageData9EventDocumentAck> {
    public EventDocumentResponse(Pair<MessageHeaderBase, MessageData9EventDocumentAck> response) {
        super(response);
    }

    public EventDocumentResponse(MessageHeaderBase header, MessageData9EventDocumentAck data) {
        super(header, data);
    }
}
