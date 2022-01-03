
package hu.arheu.gds.message.data;


public interface MessageData7AttachmentResponseAck extends Ack, MessageData {

    AttachmentResponseAckResultHolder getData();

    @Override
    default MessageData7AttachmentResponseAck asAttachmentResponseAckMessageData7() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isAttachmentResponseAckMessageData7() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.ATTACHMENT_RESPONSE_ACK_7;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
