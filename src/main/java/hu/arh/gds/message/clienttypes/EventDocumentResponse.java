/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arh.gds.message.clienttypes;

import hu.arh.gds.client.Pair;
import hu.arh.gds.message.data.MessageData9EventDocumentAck;
import hu.arh.gds.message.header.MessageHeaderBase;

public class EventDocumentResponse extends GDSMessage<MessageData9EventDocumentAck> {
    public EventDocumentResponse(Pair<MessageHeaderBase, MessageData9EventDocumentAck> response) {
        super(response);
    }

    public EventDocumentResponse(MessageHeaderBase header, MessageData9EventDocumentAck data) {
        super(header, data);
    }
}
