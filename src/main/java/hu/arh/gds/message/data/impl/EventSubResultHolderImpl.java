package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.EventSubResultHolder;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class EventSubResultHolderImpl implements EventSubResultHolder {
    private AckStatus subStatus;
    private String id;
    private String tableName;
    private Boolean created;
    private Long version;
    private List<Value> recordValues;

    public EventSubResultHolderImpl(AckStatus subStatus,
                                    String id,
                                    String tableName,
                                    Boolean created,
                                    Long version,
                                    List<Value> recordValues) {
        this.subStatus = subStatus;
        this.id = id;
        this.tableName = tableName;
        this.created = created;
        this.version = version;
        this.recordValues = recordValues;
        checkContent(this);
    }

    private static void checkContent(EventSubResultHolderImpl eventSubResult) {
        ExceptionHelper.requireNonNullValue(eventSubResult.getSubStatus(), eventSubResult.getClass().getSimpleName(),
                "sub status");
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
    public Long getVersion() {
        return this.version;
    }

    @Override
    public List<Value> getRecordValues() {
        return this.recordValues;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, 6);
        WriterHelper.packValue(packer, this.subStatus == null ? null : this.subStatus.getValue());
        WriterHelper.packValue(packer, this.id);
        WriterHelper.packValue(packer, this.tableName);
        WriterHelper.packValue(packer, this.created);
        WriterHelper.packValue(packer, version);
        WriterHelper.packValueValues(packer, this.recordValues);
    }

    public static EventSubResultHolderImpl unpackContent(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event sub result",
                EventSubResultHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 6, "event sub result",
                    EventSubResultHolderImpl.class.getSimpleName());

            AckStatus subStatusTemp = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "sub status",
                    EventSubResultHolderImpl.class.getSimpleName()));

            String idTemp = ReaderHelper.unpackStringValue(unpacker, "id",
                    EventSubResultHolderImpl.class.getSimpleName());

            String tableNameTemp = ReaderHelper.unpackStringValue(unpacker, "table name",
                    EventSubResultHolderImpl.class.getSimpleName());

            Boolean createdTemp = ReaderHelper.unpackBooleanValue(unpacker, "created",
                    EventSubResultHolderImpl.class.getSimpleName());

            Long versionTemp = ReaderHelper.unpackLongValue(unpacker, "version",
                    EventSubResultHolderImpl.class.getSimpleName());

            List<Value> recordValuesTemp = ReaderHelper.unpackValueValues(unpacker,
                    null,
                    "returning record values",
                    "field value",
                    EventSubResultHolderImpl.class.getSimpleName());

            EventSubResultHolderImpl eventSubResultHolderTemp = new EventSubResultHolderImpl(subStatusTemp,
                    idTemp,
                    tableNameTemp,
                    createdTemp,
                    versionTemp,
                    recordValuesTemp);

            checkContent(eventSubResultHolderTemp);
            return eventSubResultHolderTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventSubResultHolderImpl)) return false;
        EventSubResultHolderImpl that = (EventSubResultHolderImpl) o;
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
