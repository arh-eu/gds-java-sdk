
package hu.arheu.gds.message.data;


public interface MessageData6AttachmentResponse extends MessageData {

    AttachmentResultHolder getResult();

    /**
     * Placeholder value, always {@code null} currently.
     *
     * @return {@code null}
     */
    EventHolder getEventHolder();

    @Override
    default MessageData6AttachmentResponse asAttachmentResponseMessageData6() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isAttachmentResponseMessageData6() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.ATTACHMENT_RESPONSE_6;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 2;
    }
}
