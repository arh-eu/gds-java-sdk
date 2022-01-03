package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.AttachmentRequestAckDataHolder;
import hu.arheu.gds.message.data.AttachmentResultHolder;
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

public class AttachmentRequestAckDataHolderImpl extends MessagePart implements AttachmentRequestAckDataHolder {
    private AckStatus status;
    private AttachmentResultHolder result;
    private Long remainedWaitTimeMillis;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public AttachmentRequestAckDataHolderImpl() {
    }

    public AttachmentRequestAckDataHolderImpl(AckStatus status,
                                              AttachmentResultHolder result,
                                              Long remainedWaitTimeMillis) throws ValidationException {

        this.status = status;
        this.result = result;
        this.remainedWaitTimeMillis = remainedWaitTimeMillis;

        checkContent();
    }

    @Override
    public void checkContent() throws ValidationException {
        Validator.requireNonNullValue(getStatus(), getClass().getSimpleName(), "status");
        Validator.requireNonNullValue(getResult(), getClass().getSimpleName(), "result");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public AckStatus getStatus() {
        return this.status;
    }

    @Override
    public AttachmentResultHolder getResult() {
        return this.result;
    }

    @Override
    public Long getRemainedWaitTimeMillis() {
        return this.remainedWaitTimeMillis;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packMessagePart(packer, this.result);
        WriterHelper.packValue(packer, this.remainedWaitTimeMillis);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "ack data",
                AttachmentRequestAckDataHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "ack data",
                    AttachmentRequestAckDataHolderImpl.class.getSimpleName());

            status = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                    AttachmentRequestAckDataHolderImpl.class.getSimpleName()));

            result = new AttachmentResultHolderImpl();
            ((AttachmentResultHolderImpl) result).setType(AttachmentResultHolder.Type.ATTACHMENT_REQUEST_ACK);
            result.unpackContentFrom(unpacker);
            remainedWaitTimeMillis = ReaderHelper.unpackLongValue(unpacker, "remained time millis",
                    AttachmentRequestAckDataHolderImpl.class.getSimpleName());

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentRequestAckDataHolderImpl)) return false;
        AttachmentRequestAckDataHolderImpl that = (AttachmentRequestAckDataHolderImpl) o;
        return status == that.status
                && Objects.equals(result, that.result)
                && Objects.equals(remainedWaitTimeMillis, that.remainedWaitTimeMillis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, result, remainedWaitTimeMillis);
    }
}
