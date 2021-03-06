/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.client.Either;
import hu.arheu.gds.client.Pair;
import hu.arheu.gds.message.data.MessageData5AttachmentRequestAck;
import hu.arheu.gds.message.data.MessageData6AttachmentResponse;
import hu.arheu.gds.message.header.MessageHeaderBase;

/**
 * Represents the result for an Attachment Request.
 * Since the reply can arrive in either a type 5 or a type 6 message, the data part is an either object.
 */
public class AttachmentResult extends GDSMessage<Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse>> {

    public AttachmentResult(Pair<MessageHeaderBase, Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse>> response) {
        super(response);
    }

    public AttachmentResult(MessageHeaderBase header, Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse> data) {
        super(header, data);
    }

    public boolean isAttachmentRequestAck() {
        return getData().isLeftSet();
    }

    public MessageData5AttachmentRequestAck getDataAsAttachmentRequestAck() {
        return getData().getLeft();
    }

    public boolean isAttachmentResponse() {
        return getData().isRightSet();
    }

    public MessageData6AttachmentResponse getDataAsAttachmentResponse() {
        return getData().getRight();
    }
}
