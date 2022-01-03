
package hu.arheu.gds.message.data;


import hu.arheu.gds.message.util.GdsMessagePart;

public interface MessageData extends GdsMessagePart {

    byte[] getBinary();

    MessageDataType getMessageDataType();

    default MessageData0Connection asConnectionMessageData0() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData0Connection.class.getSimpleName()));
    }

    default MessageData1ConnectionAck asConnectionAckMessageData1() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData1ConnectionAck.class.getSimpleName()));
    }

    default MessageData2Event asEventMessageData2() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData2Event.class.getSimpleName()));
    }

    default MessageData3EventAck asEventAckMessageData3() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData3EventAck.class.getSimpleName()));
    }

    default MessageData4AttachmentRequest asAttachmentRequestMessageData4() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData4AttachmentRequest.class.getSimpleName()));
    }

    default MessageData5AttachmentRequestAck asAttachmentRequestAckMessageData5() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData5AttachmentRequestAck.class.getSimpleName()));
    }

    default MessageData6AttachmentResponse asAttachmentResponseMessageData6() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData6AttachmentResponse.class.getSimpleName()));
    }

    default MessageData7AttachmentResponseAck asAttachmentResponseAckMessageData7() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData7AttachmentResponseAck.class.getSimpleName()));
    }

    default MessageData8EventDocument asEventDocumentMessageData8() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData8EventDocument.class.getSimpleName()));
    }

    default MessageData9EventDocumentAck asEventDocumentAckMessageData9() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData9EventDocumentAck.class.getSimpleName()));
    }

    default MessageData10QueryRequest asQueryRequestMessageData10() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData10QueryRequest.class.getSimpleName()));
    }

    default MessageData11QueryRequestAck asQueryRequestAckMessageData11() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData11QueryRequestAck.class.getSimpleName()));
    }

    default MessageData12NextQueryPage asNextQueryPageMessageData12() throws ClassCastException {
        throw new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData12NextQueryPage.class.getSimpleName()));
    }

    default boolean isConnectionMessageData0() {
        return false;
    }

    default boolean isConnectionAckMessageData1() {
        return false;
    }

    default boolean isEventMessageData2() {
        return false;
    }

    default boolean isEventAckMessageData3() {
        return false;
    }

    default boolean isAttachmentRequestMessageData4() {
        return false;
    }

    default boolean isAttachmentRequestAckMessageData5() {
        return false;
    }

    default boolean isAttachmentResponseMessageData6() {
        return false;
    }

    default boolean isAttachmentResponseAckMessageData7() {
        return false;
    }

    default boolean isEventDocumentMessageData8() {
        return false;
    }

    default boolean isEventDocumentAckMessageData9() {
        return false;
    }

    default boolean isQueryRequestMessageData10() {
        return false;
    }

    default boolean isQueryRequestAckMessageData11() {
        return false;
    }

    default boolean isNextQueryPageMessageData12() {
        return false;
    }
}
