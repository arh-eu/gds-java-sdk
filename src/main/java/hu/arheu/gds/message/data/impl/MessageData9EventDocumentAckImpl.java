
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.EventDocumentResultHolder;
import hu.arheu.gds.message.data.MessageData9EventDocumentAck;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MessageData9EventDocumentAckImpl extends MessagePart implements MessageData9EventDocumentAck {

    private AckStatus globalStatus;
    private List<EventDocumentResultHolder> result;
    private String globalException;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData9EventDocumentAckImpl() {
    }

    public MessageData9EventDocumentAckImpl(AckStatus globalStatus,
                                            List<EventDocumentResultHolder> result,
                                            String globalException) throws ValidationException {

        this.globalStatus = globalStatus;
        this.result = result;
        this.globalException = globalException;

        checkContent();
    }

    public MessageData9EventDocumentAckImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData9EventDocumentAckImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public List<EventDocumentResultHolder> getResults() {
        return this.result;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void checkContent() {
        Validator.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
        Validator.requireNonEmptyCollection(this.result, this.getClass().getSimpleName(),
                "result");
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {
        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        if (this.result != null) {
            WriterHelper.packArrayHeader(packer, this.result.size());
            for (EventDocumentResultHolder eventDocumentResultHolder : this.result) {
                eventDocumentResultHolder.packContentTo(packer);
            }
        } else {
            WriterHelper.packNil(packer);
        }
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event document ack data",
                this.getClass().getSimpleName())) {
            ReaderHelper.unpackArrayHeader(unpacker, 3, "event document ack data",
                    this.getClass().getSimpleName());
            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));
            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "result",
                    this.getClass().getSimpleName())) {
                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "result",
                        this.getClass().getSimpleName());
                this.result = new ArrayList<>();
                for (int i = 0; i < arrayHeaderSize; ++i) {
                    EventDocumentResultHolderImpl holder = new EventDocumentResultHolderImpl();
                    holder.unpackContentFrom(unpacker);
                    this.result.add(holder);
                }
            } else {
                ReaderHelper.unpackNil(unpacker);
            }
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
        if (o == null || getClass() != o.getClass()) return false;
        MessageData9EventDocumentAckImpl that = (MessageData9EventDocumentAckImpl) o;
        return globalStatus == that.globalStatus
                && Objects.equals(result, that.result)
                && Objects.equals(globalException, that.globalException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalStatus, result, globalException);
    }

    @Override
    public String toString() {
        return "MessageData9EventDocumentAckImpl{" +
                "globalStatus=" + globalStatus +
                ", resultLen=" + result.size() +
                '}';
    }
}
