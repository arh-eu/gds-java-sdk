
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.EventResultHolder;
import hu.arheu.gds.message.data.MessageData3EventAck;
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


public class MessageData3EventAckImpl extends MessagePart implements MessageData3EventAck {

    private List<EventResultHolder> eventResults;
    private AckStatus globalStatus;
    private String globalException;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData3EventAckImpl() {
    }

    public MessageData3EventAckImpl(List<EventResultHolder> eventResults,
                                    AckStatus globalStatus,
                                    String globalException) throws ValidationException {

        this.eventResults = eventResults;
        this.globalStatus = globalStatus;
        this.globalException = globalException;

        checkContent();
    }

    public MessageData3EventAckImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData3EventAckImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    @Override
    public List<EventResultHolder> getEventResult() {
        return this.eventResults;
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void checkContent() throws ValidationException {
        Validator.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        WriterHelper.packMessagePartCollection(packer, this.eventResults);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event ack data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "event ack data",
                    this.getClass().getSimpleName());

            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event results",
                    this.getClass().getSimpleName())) {

                this.eventResults = new ArrayList<>();
                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "event results",
                        this.getClass().getSimpleName());
                for (int i = 0; i < arrayHeaderSize; ++i) {
                    EventResultHolderImpl holder = new EventResultHolderImpl();
                    holder.unpackContentFrom(unpacker);
                    this.eventResults.add(holder);
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
        MessageData3EventAckImpl that = (MessageData3EventAckImpl) o;
        return Objects.equals(eventResults, that.eventResults)
                && globalStatus == that.globalStatus
                && Objects.equals(globalException, that.globalException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventResults, globalStatus, globalException);
    }

    @Override
    public String toString() {
        return "MessageData3EventAckImpl{" +
                "eventResults=" + eventResults +
                ", globalStatus=" + globalStatus +
                '}';
    }
}
