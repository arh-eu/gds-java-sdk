package hu.arheu.gds.message.data;

import hu.arheu.gds.message.header.MessageDataType;

public abstract  class MessageDataTypeHelper {

    public abstract MessageDataType getMessageDataType();

    public MessageData0Connection asConnectionMessageData0() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData0Connection.class.getSimpleName()));
    }

    public MessageData1ConnectionAck asConnectionAckMessageData1() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData1ConnectionAck.class.getSimpleName()));
    }

    public MessageData2Event asEventMessageData2() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData2Event.class.getSimpleName()));
    }

    public MessageData3EventAck asEventAckMessageData3() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData3EventAck.class.getSimpleName()));
    }

    public MessageData4AttachmentRequest asAttachmentRequestMessageData4() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData4AttachmentRequest.class.getSimpleName()));
    }

    public MessageData5AttachmentRequestAck asAttachmentRequestAckMessageData5() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData5AttachmentRequestAck.class.getSimpleName()));
    }

    public MessageData6AttachmentResponse asAttachmentResponseMessageData6() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData6AttachmentResponse.class.getSimpleName()));
    }

    public MessageData7AttachmentResponseAck asAttachmentResponseAckMessageData7() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData7AttachmentResponseAck.class.getSimpleName()));
    }

    public MessageData8EventDocument asEventDocumentMessageData8() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData8EventDocument.class.getSimpleName()));
    }

    public MessageData9EventDocumentAck asEventDocumentAckMessageData9() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData9EventDocumentAck.class.getSimpleName()));
    }

    public MessageData10QueryRequest asQueryRequestMessageData10() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData10QueryRequest.class.getSimpleName()));
    }

    public MessageData11QueryRequestAck asQueryRequestAckMessageData11() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData11QueryRequestAck.class.getSimpleName()));
    }

    public MessageData12NextQueryPage asNextQueryPageMessageData12() throws ClassCastException {
        throw  new ClassCastException(String.format("%s cannot be cast to %s", this.getClass().getSimpleName(), MessageData12NextQueryPage.class.getSimpleName()));
    }

    public boolean isConnectionMessageData0() {
        return false;
    }
    public boolean isConnectionAckMessageData1() {
        return false;
    }
    public boolean isEventMessageData2() {
        return false;
    }
    public boolean isEventAckMessageData3() {
        return false;
    }
    public boolean isAttachmentRequestMessageData4() {
        return false;
    }
    public boolean isAttachmentRequestAckMessageData5() {
        return false;
    }
    public boolean isAttachmentResponseMessageData6() {
        return false;
    }
    public boolean isAttachmentResponseAckMessageData7() {
        return false;
    }
    public boolean isEventDocumentMessageData8() {
        return false;
    }
    public boolean isEventDocumentAckMessageData9() {
        return false;
    }
    public boolean isQueryRequestMessageData10() {
        return false;
    }
    public boolean isQueryRequestAckMessageData11() {
        return false;
    }
    public boolean isNextQueryPageMessageData12() {
        return false;
    }
}
