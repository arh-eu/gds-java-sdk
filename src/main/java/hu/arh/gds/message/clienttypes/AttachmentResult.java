/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arh.gds.message.clienttypes;

import hu.arh.gds.client.Either;
import hu.arh.gds.client.Pair;
import hu.arh.gds.message.data.MessageData5AttachmentRequestAck;
import hu.arh.gds.message.data.MessageData6AttachmentResponse;
import hu.arh.gds.message.header.MessageHeaderBase;

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
