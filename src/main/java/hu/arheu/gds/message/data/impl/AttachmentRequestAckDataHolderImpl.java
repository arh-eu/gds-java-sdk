package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.data.AttachmentRequestAckDataHolder;
import hu.arheu.gds.message.data.AttachmentResultHolder;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Objects;

public class AttachmentRequestAckDataHolderImpl implements AttachmentRequestAckDataHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 3;

    private AckStatus status;
    private AttachmentResultHolder result;
    private Long remainedWaitTimeMillis;

    public AttachmentRequestAckDataHolderImpl(AckStatus status,
                                              AttachmentResultHolder result,
                                              Long remainedWaitTimeMillis) {
        this.status = status;
        this.result = result;
        this.remainedWaitTimeMillis = remainedWaitTimeMillis;
        checkContent(this);
    }

    private static void checkContent(AttachmentRequestAckDataHolder data) {
        ExceptionHelper.requireNonNullValue(data.getStatus(), AttachmentRequestAckDataHolderImpl.class.getSimpleName(),
                "status");
        ExceptionHelper.requireNonNullValue(data.getResult(), AttachmentRequestAckDataHolderImpl.class.getSimpleName(),
                "result");
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
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packPackable(packer, this.result);
        WriterHelper.packValue(packer, this.remainedWaitTimeMillis);
    }

    public static AttachmentRequestAckDataHolder unpackConent(MessageUnpacker unpacker, AttachmentResultHolderType attachmentResultHolderType)
            throws IOException, ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "ack data",
                AttachmentRequestAckDataHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "ack data",
                    AttachmentRequestAckDataHolderImpl.class.getSimpleName());

            AttachmentRequestAckDataHolder dataTemp = new AttachmentRequestAckDataHolderImpl(
                    AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                            AttachmentRequestAckDataHolderImpl.class.getSimpleName())),
                    AttachmentResultHolderImpl.unpackContent(unpacker, attachmentResultHolderType),
                    ReaderHelper.unpackLongValue(unpacker, "remained time millis",
                            AttachmentRequestAckDataHolderImpl.class.getSimpleName()));

            checkContent(dataTemp);
            return dataTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentRequestAckDataHolderImpl)) return false;
        AttachmentRequestAckDataHolderImpl that = (AttachmentRequestAckDataHolderImpl) o;
        return status == that.status &&
                Objects.equals(result, that.result) &&
                Objects.equals(remainedWaitTimeMillis, that.remainedWaitTimeMillis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, result, remainedWaitTimeMillis);
    }

    @Override
    public String toString() {
        return "AttachmentRequestAckDataHolderImpl{" +
                "status=" + status +
                ", result=" + result +
                ", remainedWaitTimeMillis=" + remainedWaitTimeMillis +
                '}';
    }
}
