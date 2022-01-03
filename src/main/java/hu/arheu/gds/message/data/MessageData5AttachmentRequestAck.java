
package hu.arheu.gds.message.data;


public interface MessageData5AttachmentRequestAck extends Ack, MessageData {

    AttachmentRequestAckDataHolder getData();

    @Override
    default MessageData5AttachmentRequestAck asAttachmentRequestAckMessageData5() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isAttachmentRequestAckMessageData5() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.ATTACHMENT_REQUEST_ACK_5;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
