package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.AttachmentResponseAckResultHolder;
import hu.arh.gds.message.data.AttachmentResultHolder;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Objects;

public class AttachmentResponseAckResultHolderImpl implements AttachmentResponseAckResultHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 2;

    private AckStatus status;
    private AttachmentResultHolder result;

    public AttachmentResponseAckResultHolderImpl(AckStatus status, AttachmentResultHolder result) {
        this.status = status;
        this.result = result;
    }

    private static void checkContent(AttachmentResponseAckResultHolder attachmentResponseAckResultHolder) {
        ExceptionHelper.requireNonNullValue(attachmentResponseAckResultHolder.getStatus(),
                attachmentResponseAckResultHolder.getClass().getSimpleName(),
                "status");
        ExceptionHelper.requireNonNullValue(attachmentResponseAckResultHolder.getResult(),
                attachmentResponseAckResultHolder.getClass().getSimpleName(),
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
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packPackable(packer, this.result);
    }

    public static AttachmentResponseAckResultHolder unpackContent(MessageUnpacker unpacker) throws IOException, ReadException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "ack data",
                AttachmentResponseAckResultHolder.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "ack data",
                    AttachmentRequestAckDataHolderImpl.class.getSimpleName());

            AttachmentResponseAckResultHolder dataTemp = new AttachmentResponseAckResultHolderImpl(
                    AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                            AttachmentRequestAckDataHolderImpl.class.getSimpleName())),
                    AttachmentResultHolderImpl.unpackContent(unpacker, AttachmentResultHolderType.ATTACHMENT_RESPONSE_ACK));
            checkContent(dataTemp);
            return dataTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentResponseAckResultHolderImpl)) return false;
        AttachmentResponseAckResultHolderImpl that = (AttachmentResponseAckResultHolderImpl) o;
        return status == that.status &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, result);
    }

    @Override
    public String toString() {
        return "AttachmentResponseAckResultHolderImpl{" +
                "status=" + status +
                ", result=" + result +
                '}';
    }
}
