/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.client.Pair;
import hu.arheu.gds.message.data.MessageData3EventAck;
import hu.arheu.gds.message.header.MessageHeaderBase;

/**
 * Represents a response given to an Event (type 2) message.
 */
public class EventResponse extends GDSMessage<MessageData3EventAck> {

    public EventResponse(Pair<MessageHeaderBase, MessageData3EventAck> response) {
        super(response);
    }

    public EventResponse(MessageHeaderBase header, MessageData3EventAck data) {
        super(header, data);
    }
}
