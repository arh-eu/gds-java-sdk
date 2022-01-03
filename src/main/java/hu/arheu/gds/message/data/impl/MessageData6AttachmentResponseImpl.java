
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.AttachmentResultHolder;
import hu.arheu.gds.message.data.EventHolder;
import hu.arheu.gds.message.data.MessageData6AttachmentResponse;
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


public class MessageData6AttachmentResponseImpl extends MessagePart implements MessageData6AttachmentResponse {

    private AttachmentResultHolder result;
    private EventHolder eventHolder;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData6AttachmentResponseImpl() {
    }

    public MessageData6AttachmentResponseImpl(AttachmentResultHolder result,
                                              EventHolder eventHolder) throws ValidationException {

        this.result = result;
        this.eventHolder = eventHolder;

        checkContent();
    }

    public MessageData6AttachmentResponseImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData6AttachmentResponseImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    @Override
    public AttachmentResultHolder getResult() {
        return this.result;
    }

    @Override
    public EventHolder getEventHolder() {
        return this.eventHolder;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void checkContent() {

        Validator.requireNonNullValue(this.result, this.getClass().getSimpleName(), "result");
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, 2);
        WriterHelper.packMessagePart(packer, this.result);
        WriterHelper.packMessagePart(packer, this.eventHolder);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "attachment response data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, null, "attachment response data",
                    this.getClass().getSimpleName());

            this.result = new AttachmentResultHolderImpl();
            ((AttachmentResultHolderImpl) result).setType(AttachmentResultHolder.Type.ATTACHMENT_RESPONSE);
            result.unpackContentFrom(unpacker);
            if (!ReaderHelper.isNextNil(unpacker)) {
                this.eventHolder = new EventHolderImpl();
                this.eventHolder.unpackContentFrom(unpacker);
            }
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData6AttachmentResponseImpl that = (MessageData6AttachmentResponseImpl) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(eventHolder, that.eventHolder);
    }

    @Override
    public int hashCode() {

        return Objects.hash(result, eventHolder);
    }

    @Override
    public String toString() {
        return "MessageData6AttachmentResponseImpl{" +
                "result.table=" + (null != result ? result.getOwnerTable() : "null") +
                ", result.attachmentId=" + (null != result ? result.getAttachmentId() : "null") +
                ", result.attachmentSize=" + ((null != result && null != result.getAttachment())
                    ? result.getAttachment().length : "null") +
                ", eventHolder=" + eventHolder +
                '}';
    }
}
