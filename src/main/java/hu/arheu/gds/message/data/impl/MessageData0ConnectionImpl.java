package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.MessageData0Connection;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Objects;

public class MessageData0ConnectionImpl extends MessageData0Connection {
    private Boolean serveOnTheSameConnection;
    private String clusterName;
    private Integer protocolVersionNumber;
    private Boolean fragmentationSupported;
    private Long fragmentTransmissionUnit;
    private String password;

    public MessageData0ConnectionImpl(boolean cache,
                                      Boolean serveOnTheSameConnection,
                                      String clusterName,
                                      Integer protocolVersionNumber,
                                      Boolean fragmentationSupported,
                                      Long fragmentTransmissionUnit,
                                      String password) throws IOException, ValidationException {
        this.serveOnTheSameConnection = serveOnTheSameConnection;
        this.clusterName = clusterName;
        this.protocolVersionNumber = protocolVersionNumber;
        this.fragmentationSupported = fragmentationSupported;
        this.fragmentTransmissionUnit = fragmentTransmissionUnit;
        this.password = password;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
        init();
    }

    public MessageData0ConnectionImpl(boolean cache,
                                      Boolean serveOnTheSameConnection,
                                      String clusterName,
                                      Integer protocolVersionNumber,
                                      Boolean fragmentationSupported,
                                      Long fragmentTransmissionUnit) throws IOException, ValidationException {
        this(cache, serveOnTheSameConnection, clusterName, protocolVersionNumber, fragmentationSupported,
                fragmentTransmissionUnit, null);
    }


    public MessageData0ConnectionImpl(boolean cache,
                                      Boolean serveOnTheSameConnection,
                                      Integer protocolVersionNumber,
                                      Boolean fragmentationSupported,
                                      Long fragmentTransmissionUnit) throws IOException, ValidationException {
        this(cache, serveOnTheSameConnection, null, protocolVersionNumber, fragmentationSupported,
                fragmentTransmissionUnit, null);
    }

    public MessageData0ConnectionImpl(byte[] binary,
                                      boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData0ConnectionImpl(byte[] binary,
                                      boolean cache,
                                      boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }


    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.CONNECTION_0;
            }
            @Override
            public MessageData0ConnectionImpl asConnectionMessageData0() {
                return MessageData0ConnectionImpl.this;
            }
            @Override
            public boolean isConnectionMessageData0() {
                return true;
            }
        };
    }

    @Override
    public Boolean getServeOnTheSameConnection() {
        return serveOnTheSameConnection;
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public Integer getProtocolVersionNumber() {
        return protocolVersionNumber;
    }

    @Override
    public Boolean getFragmentationSupported() {
        return fragmentationSupported;
    }

    @Override
    public Long getFragmentTransmissionUnit() {
        return fragmentTransmissionUnit;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.serveOnTheSameConnection, this.getClass().getSimpleName(),
                "serveOnTheSameConnection");
        ExceptionHelper.requireNonNullValue(this.protocolVersionNumber, this.getClass().getSimpleName(),
                "protocolVersionNumber");
        ExceptionHelper.requireNonNullValue(this.fragmentationSupported, this.getClass().getSimpleName(),
                "fragmentationSupported");
        if (this.fragmentationSupported) {
            ExceptionHelper.requireNonNullValue(this.fragmentTransmissionUnit, this.getClass().getSimpleName(),
                    "fragmentTransmissionUnit");
        }
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException {
        if (this.clusterName != null) {
            if (this.password != null) {
                WriterHelper.packArrayHeader(packer, 6);
            } else {
                WriterHelper.packArrayHeader(packer, 5);
            }
            WriterHelper.packValue(packer, this.clusterName);
        } else {
            if (this.password != null) {
                WriterHelper.packArrayHeader(packer, 5);
            } else {
                WriterHelper.packArrayHeader(packer, 4);
            }
        }
        WriterHelper.packValue(packer, this.serveOnTheSameConnection);
        WriterHelper.packValue(packer, this.protocolVersionNumber);
        WriterHelper.packValue(packer, this.fragmentationSupported);
        WriterHelper.packValue(packer, this.fragmentTransmissionUnit);
        if (this.password != null) {
            WriterHelper.packArrayHeader(packer, 1);
            WriterHelper.packValue(packer, this.password);
        }
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "connection data",
                this.getClass().getSimpleName())) {

            int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "connection data",
                    this.getClass().getSimpleName());

            if (arrayHeaderSize != 4 && arrayHeaderSize != 5 && arrayHeaderSize != 6) {
                throw new ReadException(String.format(
                        "Array header size [%s] does not match expected header size [%s]", arrayHeaderSize, "4 or 5 or 6"));
            }

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
                    this.clusterName = value1.asStringValue().asString();
                } else {
                    this.clusterName = null;
                }
                if (!value2.isBooleanValue()) {
                    throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            this.getClass().getSimpleName(),
                            value2.getValueType(),
                            ValueType.BOOLEAN,
                            "serveOnTheSameConnection"));
                }
                this.serveOnTheSameConnection = value2.asBooleanValue().getBoolean();
                if (!value3.isIntegerValue()) {
                    throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            this.getClass().getSimpleName(),
                            value3.getValueType(),
                            ValueType.INTEGER,
                            "protocolVersionNumber"));
                }
                this.protocolVersionNumber = value3.asIntegerValue().asInt();
                if (!value4.isBooleanValue()) {
                    throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            this.getClass().getSimpleName(),
                            value4.getValueType(),
                            ValueType.BOOLEAN,
                            "fragmentationSupported"));
                }
                this.fragmentationSupported = value4.asBooleanValue().getBoolean();
                this.fragmentTransmissionUnit = value5.isNilValue() ? null : value5.asIntegerValue().asLong();
            } else {
                if (!value1.isBooleanValue()) {
                    throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            this.getClass().getSimpleName(),
                            value1.getValueType(),
                            ValueType.BOOLEAN,
                            "serveOnTheSameConnection"));
                }
                this.serveOnTheSameConnection = value1.asBooleanValue().getBoolean();
                if (!value2.isIntegerValue()) {
                    throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            this.getClass().getSimpleName(),
                            value2.getValueType(),
                            ValueType.INTEGER,
                            "protocolVersionNumber"));
                }
                this.protocolVersionNumber = value2.asIntegerValue().asInt();
                if (!value3.isBooleanValue()) {
                    throw new ReadException(String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            this.getClass().getSimpleName(),
                            value3.getValueType(),
                            ValueType.INTEGER,
                            "fragmentationSupported"));
                }
                this.fragmentationSupported = value3.asBooleanValue().getBoolean();
                this.fragmentTransmissionUnit = value4.isNilValue() ? null : value4.asIntegerValue().asLong();
            }

            if (value5 != null && value5.isArrayValue()) {
                if (value5.asArrayValue().get(0).isNilValue()) {
                    this.password = null;
                } else {
                    this.password = value5.asArrayValue().get(0).asStringValue().asString();
                }
            }

            if (value6 != null && value6.isArrayValue()) {
                if (value6.asArrayValue().get(0).isNilValue()) {
                    this.password = null;
                } else {
                    this.password = value6.asArrayValue().get(0).asStringValue().asString();
                }
            }
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData0ConnectionImpl that = (MessageData0ConnectionImpl) o;
        return serveOnTheSameConnection.equals(that.serveOnTheSameConnection) &&
                Objects.equals(clusterName, that.clusterName) &&
                protocolVersionNumber.equals(that.protocolVersionNumber) &&
                fragmentationSupported.equals(that.fragmentationSupported) &&
                Objects.equals(fragmentTransmissionUnit, that.fragmentTransmissionUnit) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serveOnTheSameConnection, clusterName, protocolVersionNumber, fragmentationSupported, fragmentTransmissionUnit, password);
    }

    @Override
    public String toString() {
        return "MessageData0ConnectionImpl{" +
                "serveOnTheSameConnection=" + serveOnTheSameConnection +
                ", clusterName='" + clusterName + '\'' +
                ", protocolVersionNumber=" + protocolVersionNumber +
                ", fragmentationSupported=" + fragmentationSupported +
                ", fragmentTransmissionUnit=" + fragmentTransmissionUnit +
                ", password='" + password + '\'' +
                '}';
    }
}
