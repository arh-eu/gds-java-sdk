/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.client.Either;
import hu.arheu.gds.message.data.MessageData5AttachmentRequestAck;
import hu.arheu.gds.message.data.MessageData6AttachmentResponse;
import hu.arheu.gds.message.header.MessageHeaderBase;

import java.io.Externalizable;

/**
 * Represents the result for an Attachment Request.
 * Since the reply can arrive in either a type 5 or a type 6 message, the data part is an "Either" object.
 */
public class AttachmentResult {
    private MessageHeaderBase header;
    private Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse> data;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public AttachmentResult() {
    }

    public AttachmentResult(MessageHeaderBase header, Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse> data) {
        this.header = header;
        this.data = data;
    }

    public MessageHeaderBase getHeader() {
        return header;
    }

    public boolean isAttachmentRequestAck() {
        return data.isLeftSet();
    }

    public MessageData5AttachmentRequestAck getDataAsAttachmentRequestAck() {
        return data.getLeft();
    }

    public boolean isAttachmentResponse() {
        return data.isRightSet();
    }

    public MessageData6AttachmentResponse getDataAsAttachmentResponse() {
        return data.getRight();
    }
}
