
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.AttachmentResponseAckResultHolder;
import hu.arheu.gds.message.data.AttachmentResultHolder;
import hu.arheu.gds.message.data.MessageData7AttachmentResponseAck;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.Externalizable;
import java.util.Objects;


public class MessageData7AttachmentResponseAckImpl extends MessagePart implements MessageData7AttachmentResponseAck {

    private AckStatus globalStatus;
    private AttachmentResponseAckResultHolder data;
    private String globalException;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData7AttachmentResponseAckImpl() {
    }

    public MessageData7AttachmentResponseAckImpl(AckStatus globalStatus,
                                                 AttachmentResponseAckResultHolder data,
                                                 String globalException) throws ValidationException {

        this.globalStatus = globalStatus;
        this.data = data;
        this.globalException = globalException;

        checkContent();
    }

    public MessageData7AttachmentResponseAckImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData7AttachmentResponseAckImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    public AttachmentResponseAckResultHolder getData() {
        return this.data;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    public boolean isAttachmentResponseAckMessageData7() {
        return true;
    }

    @Override
    public void checkContent() {
        Validator.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException, ValidationException {
        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        WriterHelper.packMessagePart(packer, this.data);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "attachment response ack data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "attachment response ack data",
                    this.getClass().getSimpleName());
            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));
            this.data = new AttachmentResponseAckResultHolderImpl();
            this.data.unpackContentFrom(unpacker);
            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageData7AttachmentResponseAckImpl)) return false;
        MessageData7AttachmentResponseAckImpl that = (MessageData7AttachmentResponseAckImpl) o;
        return globalStatus == that.globalStatus &&
                Objects.equals(data, that.data) &&
                Objects.equals(globalException, that.globalException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalStatus, data, globalException);
    }

    @Override
    public String toString() {
        AttachmentResultHolder arh = data != null ? data.getResult() : null;
        return "MessageData7AttachmentResponseAckImpl{" +
                "globalStatus=" + globalStatus +
                ", table=" + (null != arh ? arh.getOwnerTable() : "null") +
                ", attachmentId=" + (null != arh ? arh.getAttachmentId() : "null") +
                ", attachmentSize=" + ((null != arh && null != arh.getAttachment())
                    ? arh.getAttachment().length : "null") +
                '}';
    }
}
