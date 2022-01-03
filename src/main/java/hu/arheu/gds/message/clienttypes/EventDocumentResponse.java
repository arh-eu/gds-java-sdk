/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.message.data.MessageData9EventDocumentAck;
import hu.arheu.gds.message.header.MessageHeaderBase;

import java.io.Externalizable;

/**
 * Represents a response to an Event Document message (type 8).
 */
public class EventDocumentResponse {

    private MessageHeaderBase header;
    private MessageData9EventDocumentAck data;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public EventDocumentResponse() {
    }

    public EventDocumentResponse(MessageHeaderBase header, MessageData9EventDocumentAck data) {
        this.header = header;
        this.data = data;
    }

    public MessageHeaderBase getHeader() {
        return header;
    }

    public MessageData9EventDocumentAck getData() {
        return data;
    }
}
