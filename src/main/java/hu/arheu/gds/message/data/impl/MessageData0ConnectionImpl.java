
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageData0Connection;
import hu.arheu.gds.message.data.MessageDataType;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;
import org.msgpack.value.impl.ImmutableNilValueImpl;

import java.util.Objects;


public class MessageData0ConnectionImpl extends MessagePart implements MessageData0Connection {

    private Boolean serveOnTheSameConnection;
    private String clusterName;
    private Integer protocolVersionNumber;
    private Boolean fragmentationSupported;
    private Long fragmentTransmissionUnit;
    private String password;

    /**
     * Do not remove, as it's needed for the serialization through {@link java.io.Externalizable}
     */
    public MessageData0ConnectionImpl() {
    }

    public MessageData0ConnectionImpl(Boolean serveOnTheSameConnection,
                                      String clusterName,
                                      Integer protocolVersionNumber,
                                      Boolean fragmentationSupported,
                                      Long fragmentTransmissionUnit,
                                      String password) throws ValidationException {

        this.serveOnTheSameConnection = serveOnTheSameConnection;
        this.clusterName = clusterName;
        this.protocolVersionNumber = protocolVersionNumber;
        this.fragmentationSupported = fragmentationSupported;
        this.fragmentTransmissionUnit = fragmentTransmissionUnit;
        this.password = password;

        checkContent();
    }

    public MessageData0ConnectionImpl(Boolean serveOnTheSameConnection,
                                      String clusterName,
                                      Integer protocolVersionNumber,
                                      Boolean fragmentationSupported,
                                      Long fragmentTransmissionUnit) throws ValidationException {

        this(serveOnTheSameConnection, clusterName, protocolVersionNumber, fragmentationSupported, fragmentTransmissionUnit, null);
    }


    public MessageData0ConnectionImpl(Boolean serveOnTheSameConnection,
                                      Integer protocolVersionNumber,
                                      Boolean fragmentationSupported,
                                      Long fragmentTransmissionUnit) throws ValidationException {
        this(serveOnTheSameConnection, null, protocolVersionNumber, fragmentationSupported, fragmentTransmissionUnit);
    }

    public MessageData0ConnectionImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData0ConnectionImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

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

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(this.serveOnTheSameConnection, this.getClass().getSimpleName(),
                "serveOnTheSameConnection");
        Validator.requireNonNullValue(this.protocolVersionNumber, this.getClass().getSimpleName(),
                "protocolVersionNumber");
        Validator.requireNonNullValue(this.fragmentationSupported, this.getClass().getSimpleName(),
                "fragmentationSupported");

        if (this.fragmentationSupported) {
            Validator.requireNonNullValue(this.fragmentTransmissionUnit, this.getClass().getSimpleName(),
                    "fragmentTransmissionUnit");
        }
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

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
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "connection data",
                this.getClass().getSimpleName())) {

            int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "connection data",
                    this.getClass().getSimpleName());

            if (arrayHeaderSize != 4 && arrayHeaderSize != 5 && arrayHeaderSize != 6) {
                throw new ReadException(String.format(
                        "Array headersize [%s] does not match expected headersize [%s]", arrayHeaderSize, "4 or 5 or 6"));
            }

            Value value1 = ReaderHelper.unpackValue(unpacker);
            Value value2 = ReaderHelper.unpackValue(unpacker);
            Value value3 = ReaderHelper.unpackValue(unpacker);
            Value value4 = ReaderHelper.unpackValue(unpacker);
            Value value5 = ImmutableNilValueImpl.get();
            Value value6 = null;

            if (arrayHeaderSize == 5) {
                value5 = ReaderHelper.unpackValue(unpacker);
            } else if (arrayHeaderSize == 6) {
                value5 = ReaderHelper.unpackValue(unpacker);
                value6 = ReaderHelper.unpackValue(unpacker);
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
            ReaderHelper.unpackNil(unpacker);
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
}
