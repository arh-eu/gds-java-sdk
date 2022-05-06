package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.EventSubResultHolder;
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

import java.io.Externalizable;
import java.util.List;
import java.util.Objects;

public class EventSubResultHolderImpl extends MessagePart implements EventSubResultHolder {

    private AckStatus subStatus;
    private String id;
    private String tableName;
    private Boolean created;
    private String version;
    private List<Value> recordValues;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public EventSubResultHolderImpl() {
    }

    public EventSubResultHolderImpl(
            AckStatus subStatus, String id, String tableName, Boolean created, String version,
            List<Value> recordValues) {

        this.subStatus = subStatus;
        this.id = id;
        this.tableName = tableName;
        this.created = created;
        this.version = version;
        this.recordValues = recordValues;

        checkContent();
    }

    @Override
    public void checkContent() {

        Validator.requireNonNullValue(getSubStatus(), getClass().getSimpleName(), "sub status");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public AckStatus getSubStatus() {
        return this.subStatus;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public Boolean getCreated() {
        return this.created;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public List<Value> getRecordValues() {
        return this.recordValues;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException, ValidationException {

        WriterHelper.packArrayHeader(packer, 6);
        WriterHelper.packValue(packer, this.subStatus == null ? null : this.subStatus.getValue());
        WriterHelper.packValue(packer, this.id);
        WriterHelper.packValue(packer, this.tableName);
        WriterHelper.packValue(packer, this.created);
        WriterHelper.packValue(packer, version);
        WriterHelper.packValueCollection(packer, this.recordValues);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event sub result",
                EventSubResultHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 6, "event sub result",
                    EventSubResultHolderImpl.class.getSimpleName());

            subStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "sub status",
                    EventSubResultHolderImpl.class.getSimpleName()));

            id = ReaderHelper.unpackStringValue(unpacker, "id",
                    EventSubResultHolderImpl.class.getSimpleName());

            tableName = ReaderHelper.unpackStringValue(unpacker, "table name",
                    EventSubResultHolderImpl.class.getSimpleName());

            created = ReaderHelper.unpackBooleanValue(unpacker, "created",
                    EventSubResultHolderImpl.class.getSimpleName());

            version = ReaderHelper.unpackStringValue(unpacker, "version",
                    EventSubResultHolderImpl.class.getSimpleName());

            recordValues = ReaderHelper.unpackValueValues(unpacker,
                    null,
                    "returning record values",
                    "field value",
                    EventSubResultHolderImpl.class.getSimpleName());


            checkContent();

        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventSubResultHolderImpl that)) return false;
        return subStatus == that.subStatus &&
                Objects.equals(id, that.id) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(created, that.created) &&
                Objects.equals(version, that.version) &&
                Objects.equals(recordValues, that.recordValues);
    }

    @Override
    public int hashCode() {

        return Objects.hash(subStatus, id, tableName, created, version, recordValues);
    }
}
