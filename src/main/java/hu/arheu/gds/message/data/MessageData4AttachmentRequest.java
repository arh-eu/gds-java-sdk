
package hu.arheu.gds.message.data;


public interface MessageData4AttachmentRequest extends MessageData{
    
    String getRequest();

    @Override
    default MessageData4AttachmentRequest asAttachmentRequestMessageData4() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isAttachmentRequestMessageData4() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.ATTACHMENT_REQUEST_4;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 1;
    }
}
