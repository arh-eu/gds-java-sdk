package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.*;
import hu.arh.gds.message.data.MessageData1ConnectionAck;
import hu.arh.gds.message.data.MessageData0ConnectionDescriptor;
import hu.arh.gds.message.data.MessageDataTypeHelper;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Map;

public class MessageData1ConnectionAckImpl extends MessageData1ConnectionAck {
    private MessageData0ConnectionDescriptor ackDataOk;
    private Map<Integer, String> ackDataUnauthorizedItems;
    private AckStatus globalStatus;
    private String globalException;

    public MessageData1ConnectionAckImpl(boolean cache,
                                         MessageData0ConnectionDescriptor ackDataOk,
                                         Map<Integer, String> ackDataUnauthorizedItems,
                                         AckStatus globalStatus,
                                         String globalException) throws IOException, ValidationException {
        this.ackDataOk = ackDataOk;
        this.ackDataUnauthorizedItems = ackDataUnauthorizedItems;
        this.globalStatus = globalStatus;
        this.globalException = globalException;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData1ConnectionAckImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData1ConnectionAckImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected  void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.CONNECTION_ACK_1;
            }
            @Override
            public MessageData1ConnectionAckImpl asConnectionAckMessageData1() {
                return MessageData1ConnectionAckImpl.this;
            }
            @Override
            public boolean isConnectionAckMessageData1() {
                return true;
            }
        };
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    public MessageData0ConnectionDescriptor getAckDataOk() {
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
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
        if (this.globalStatus.equals(AckStatus.OK)) {
            ExceptionHelper.requireNonNullValue(this.ackDataOk, this.getClass().getSimpleName(), "ackDataOk");
        } else if (this.globalStatus.equals(AckStatus.UNAUTHORIZED)) {
            ExceptionHelper.requireNonNullValue(this.ackDataUnauthorizedItems, this.getClass().getSimpleName(),
                    "ackDataUnauthorizedItems");
            ExceptionHelper.requireNonEmptyMap(this.ackDataUnauthorizedItems, this.getClass().getSimpleName(),
                    "ackDataUnauthorizedItems");
        }
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException {
        WriterHelper.packArrayHeader(packer, 3);
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
            packer.packNil();
        }
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException, ValidationException {
        ValueType nextValueType = unpacker.getNextFormat().getValueType();
        if (nextValueType.isArrayType()) {
            ReaderHelper.unpackArrayHeader(unpacker, 3, "connection ack data",
                    this.getClass().getSimpleName());
            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));

            nextValueType = unpacker.getNextFormat().getValueType();
            if (nextValueType.isArrayType()) {
                    int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "connection data",
                            this.getClass().getSimpleName());
                    if (arrayHeaderSize != 4 && arrayHeaderSize != 5 && arrayHeaderSize != 6) {
                        throw new ReadException(String.format(
                                "Array header size [%s] does not match expected header size [%s]", arrayHeaderSize, "4 or 5 or 6"));
                    }

                    String clusterNameTemp = null;
                    Boolean serveOnTheSameConnectionTemp;
                    Integer protocolVersionNumberTemp;
                    Boolean fragmentationSupportedTemp;
                    Long fragmentTransmissionUnitTemp;
                    String passwordTemp = null;

                    Value value1 = unpacker.unpackValue();
                    Value value2 = unpacker.unpackValue();
                    Value value3 = unpacker.unpackValue();
                    Value value4 = unpacker.unpackValue();
                    Value value5 = null;
                    Value value6 = null;

                    if (arrayHeaderSize == 5) {
                        value5 = unpacker.unpackValue();
                    } else if (arrayHeaderSize == 6) {
                        value5 = unpacker.unpackValue();
                        value6 = unpacker.unpackValue();
                    }

                    if (value1.isStringValue() || value1.isNilValue()) {
                        if (value1.isStringValue()) {
                            clusterNameTemp = value1.asStringValue().asString();
                        } else {
                            clusterNameTemp = null;
                        }
                        if (!value2.isBooleanValue()) {
                            throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                                    this.getClass().getSimpleName(),
                                    value2.getValueType(),
                                    ValueType.BOOLEAN,
                                    "serveOnTheSameConnection"));
                        }
                        serveOnTheSameConnectionTemp = value2.asBooleanValue().getBoolean();
                        if (!value3.isIntegerValue()) {
                            throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                                    this.getClass().getSimpleName(),
                                    value3.getValueType(),
                                    ValueType.INTEGER,
                                    "protocolVersionNumber"));
                        }
                        protocolVersionNumberTemp = value3.asIntegerValue().asInt();
                        if (!value4.isBooleanValue()) {
                            throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                                    this.getClass().getSimpleName(),
                                    value4.getValueType(),
                                    ValueType.BOOLEAN,
                                    "fragmentationSupported"));
                        }
                        fragmentationSupportedTemp = value4.asBooleanValue().getBoolean();

                        fragmentTransmissionUnitTemp = value5.isNilValue() ? null : value5.asIntegerValue().asLong();
                    } else {
                        if (!value1.isBooleanValue()) {
                            throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                                    this.getClass().getSimpleName(),
                                    value1.getValueType(),
                                    ValueType.BOOLEAN,
                                    "serveOnTheSameConnection"));
                        }
                        serveOnTheSameConnectionTemp = value1.asBooleanValue().getBoolean();
                        if (!value2.isIntegerValue()) {
                            throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                                    this.getClass().getSimpleName(),
                                    value2.getValueType(),
                                    ValueType.INTEGER,
                                    "protocolVersionNumber"));
                        }
                        protocolVersionNumberTemp = value2.asIntegerValue().asInt();
                        if (!value3.isBooleanValue()) {
                            throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                                    this.getClass().getSimpleName(),
                                    value3.getValueType(),
                                    ValueType.INTEGER,
                                    "fragmentationSupported"));
                        }
                        fragmentationSupportedTemp = value3.asBooleanValue().getBoolean();

                        fragmentTransmissionUnitTemp = value4.isNilValue() ? null : value4.asIntegerValue().asLong();
                    }

                    if (value5 != null && value5.isArrayValue()) {
                        if (value5.asArrayValue().get(0).isNilValue()) {
                            passwordTemp = null;
                        } else {
                            passwordTemp = value5.asArrayValue().get(0).asStringValue().asString();
                        }
                    }

                    if (value6 != null && value6.isArrayValue()) {
                        if (value6.asArrayValue().get(0).isNilValue()) {
                            passwordTemp = null;
                        } else {
                            passwordTemp = value6.asArrayValue().get(0).asStringValue().asString();
                        }
                    }

                    this.ackDataOk = new MessageData0ConnectionImpl(this.cache, serveOnTheSameConnectionTemp, clusterNameTemp, protocolVersionNumberTemp,
                            fragmentationSupportedTemp, fragmentTransmissionUnitTemp, passwordTemp);

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
                                nextValueType.toString(),
                                "Message Data",
                                ValueType.ARRAY.toString() + "/" + ValueType.MAP.toString()));
            } else {
                unpacker.unpackNil();
            }

            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());

        } else if (!nextValueType.isNilType()) {
            throw new ReadException(
                    String.format("Value type: %s not allowed here [%s]. Expected value type: %s.",
                            nextValueType.toString(),
                            "Message Data",
                            ValueType.ARRAY.toString()));
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData1ConnectionAckImpl that = (MessageData1ConnectionAckImpl) o;
        if (ackDataOk != null ? !ackDataOk.equals(that.ackDataOk) : that.ackDataOk != null) return false;
        if (ackDataUnauthorizedItems != null ? !ackDataUnauthorizedItems.equals(that.ackDataUnauthorizedItems) : that
                .ackDataUnauthorizedItems != null)
            return false;
        if (globalStatus != that.globalStatus) return false;
        return globalException != null ? globalException.equals(that.globalException) : that.globalException == null;
    }

    @Override
    public int hashCode() {
        int result = ackDataOk != null ? ackDataOk.hashCode() : 0;
        result = 31 * result + (ackDataUnauthorizedItems != null ? ackDataUnauthorizedItems.hashCode() : 0);
        result = 31 * result + (globalStatus != null ? globalStatus.hashCode() : 0);
        result = 31 * result + (globalException != null ? globalException.hashCode() : 0);
        return result;
    }
}
