package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.EventHolder;
import hu.arh.gds.message.util.ExceptionHelper;
import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ReaderHelper;
import hu.arh.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Map;

public class EventHolderImpl implements EventHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 2;

    private final String tableName;
    private final Map<String, Value> fields;

    public EventHolderImpl(String tableName,
                           Map<String, Value> fields) {
        this.tableName = tableName;
        this.fields = fields;
        checkContent(this);
    }

    private static void checkContent(EventHolder descriptorEvent) {
        ExceptionHelper.requireNonNullValue(descriptorEvent.getTableName(), descriptorEvent.getClass().getSimpleName(),
                "tableName");
        ExceptionHelper.requireNonNullValue(descriptorEvent.getFields(), descriptorEvent.getClass().getSimpleName(),
                "fields");
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public Map<String, Value> getFields() {
        return this.fields;
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.tableName);
        WriterHelper.packMapStringValueValues(packer, this.fields);
    }

    public static EventHolder unpackContent(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event descriptor",
                EventHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "event descriptor",
                    EventHolderImpl.class.getSimpleName());

            String tableNameTemp = ReaderHelper.unpackStringValue(unpacker, "table name",
                    EventHolderImpl.class.getSimpleName());

            Map<String, Value> fieldsTemp = ReaderHelper.unpackMapStringValueValues(unpacker,
                    null,
                    "field names and values",
                    "field names and values map key (fieldname)",
                    "field names and values map value (fieldvalue)",
                    EventHolderImpl.class.getSimpleName());

            EventHolderImpl eventHolderTemp = new EventHolderImpl(tableNameTemp, fieldsTemp);
            checkContent(eventHolderTemp);
            return eventHolderTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHolderImpl that = (EventHolderImpl) o;
        if (tableName != null ? !tableName.equals(that.tableName) : that.tableName != null) return false;
        return fields != null ? fields.equals(that.fields) : that.fields == null;
    }

    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventHolderImpl{" +
                "tableName='" + tableName + '\'' +
                ", fields=" + fields +
                '}';
    }
}
