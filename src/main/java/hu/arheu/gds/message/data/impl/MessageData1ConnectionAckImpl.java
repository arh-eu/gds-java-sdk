
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageData0Connection;
import hu.arheu.gds.message.data.MessageData1ConnectionAck;
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
import java.util.Map;
import java.util.Objects;


public class MessageData1ConnectionAckImpl extends MessagePart implements MessageData1ConnectionAck {

    private MessageData0Connection ackDataOk;
    private Map<Integer, String> ackDataUnauthorizedItems;
    private AckStatus globalStatus;
    private String globalException;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData1ConnectionAckImpl() {
    }

    public MessageData1ConnectionAckImpl(MessageData0Connection ackDataOk,
                                         Map<Integer, String> ackDataUnauthorizedItems,
                                         AckStatus globalStatus,
                                         String globalException) throws ValidationException {

        this.ackDataOk = ackDataOk;
        this.ackDataUnauthorizedItems = ackDataUnauthorizedItems;
        this.globalStatus = globalStatus;
        this.globalException = globalException;
        checkContent();
    }

    public MessageData1ConnectionAckImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData1ConnectionAckImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public MessageData0Connection getAckDataOk() {
        return this.ackDataOk;
    }

    @Override
    public Map<Integer, String> getAckDataUnauthorizedItems() {
        return this.ackDataUnauthorizedItems;
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    @Override
    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");

        if (this.globalStatus.equals(AckStatus.OK)) {
            Validator.requireNonNullValue(this.ackDataOk, this.getClass().getSimpleName(), "ackDataOk");
        } else if (this.globalStatus.equals(AckStatus.UNAUTHORIZED)) {
            Validator.requireNonNullValue(this.ackDataUnauthorizedItems, this.getClass().getSimpleName(),
                    "ackDataUnauthorizedItems");
            Validator.requireNonEmptyMap(this.ackDataUnauthorizedItems, this.getClass().getSimpleName(),
                    "ackDataUnauthorizedItems");
        }
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.globalStatus.getValue());

        if (this.ackDataOk != null) {
            if (ackDataOk.getClusterName() != null) {
                if (ackDataOk.getPassword() != null) {
                    WriterHelper.packArrayHeader(packer, 6);
                } else {
                    WriterHelper.packArrayHeader(packer, 5);
                }
                WriterHelper.packValue(packer, ackDataOk.getClusterName());
            } else {
                if (ackDataOk.getPassword() != null) {
                    WriterHelper.packArrayHeader(packer, 5);
                } else {
                    WriterHelper.packArrayHeader(packer, 4);
                }
            }

            WriterHelper.packValue(packer, ackDataOk.getServeOnTheSameConnection());
            WriterHelper.packValue(packer, ackDataOk.getProtocolVersionNumber());
            WriterHelper.packValue(packer, ackDataOk.getFragmentationSupported());
            WriterHelper.packValue(packer, ackDataOk.getFragmentTransmissionUnit());
            if (ackDataOk.getPassword() != null) {
                WriterHelper.packArrayHeader(packer, 1);
                WriterHelper.packValue(packer, ackDataOk.getPassword());
            }

        } else if (this.ackDataUnauthorizedItems != null) {

            WriterHelper.packMapIntegerStringValues(packer, this.ackDataUnauthorizedItems);

        } else {
            WriterHelper.packNil(packer);
        }
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        ValueType nextValueType = ReaderHelper.getNextValueType(unpacker);
        if (nextValueType.isArrayType()) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "connection ack data",
                    this.getClass().getSimpleName());

            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));

            nextValueType = ReaderHelper.getNextValueType(unpacker);

            if (nextValueType.isArrayType()) {
                this.ackDataOk = new MessageData0ConnectionImpl();
                this.ackDataOk.unpackContentFrom(unpacker);
            } else if (nextValueType.isMapType()) {

                this.ackDataUnauthorizedItems = ReaderHelper.unpackMapIntegerStringValues(unpacker,
                        null,
                        "connection ack message data key",
                        "connection ack message data value",
                        null,
                        this.getClass().getSimpleName());

            } else if (!nextValueType.isNilType()) {
                throw new ReadException(
                        String.format("Value type: %s not allowed here [%s]. Expected value type: %s.",
                                nextValueType,
                                "Message Data",
                                ValueType.ARRAY + "/" + ValueType.MAP));
            } else {
                ReaderHelper.unpackNil(unpacker);
            }

            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());

        } else if (!nextValueType.isNilType()) {
            throw new ReadException(
                    String.format("Value type: %s not allowed here [%s]. Expected value type: %s.",
                            nextValueType,
                            "Message Data",
                            ValueType.ARRAY));
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData1ConnectionAckImpl that = (MessageData1ConnectionAckImpl) o;
        return Objects.equals(ackDataOk, that.ackDataOk)
                && Objects.equals(ackDataUnauthorizedItems, that.ackDataUnauthorizedItems)
                && globalStatus == that.globalStatus
                && Objects.equals(globalException, that.globalException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ackDataOk, ackDataUnauthorizedItems, globalStatus, globalException);
    }
}
