/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.message.data.MessageData3EventAck;
import hu.arheu.gds.message.header.MessageHeaderBase;

import java.io.Externalizable;

/**
 * Represents a response given to an Event (type 2) message.
 */
public class EventResponse {
    private MessageHeaderBase header;
    private MessageData3EventAck data;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public EventResponse() {
    }

    public EventResponse(MessageHeaderBase header, MessageData3EventAck data) {
        this.header = header;
        this.data = data;
    }

    public MessageHeaderBase getHeader() {
        return header;
    }

    public MessageData3EventAck getData() {
        return data;
    }
}
