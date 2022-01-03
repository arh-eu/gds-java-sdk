
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.EventHolder;
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
import java.util.Map;
import java.util.Objects;


public class EventHolderImpl extends MessagePart implements EventHolder {

    private String tableName;
    private Map<String, Value> fields;

    
    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
     public EventHolderImpl(){}
    
    public EventHolderImpl(String tableName,
                           Map<String, Value> fields) {

        this.tableName = tableName;
        this.fields = fields;

        checkContent();
    }

    @Override
    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(getTableName(), getClass().getSimpleName(), "tableName");

        Validator.requireNonNullValue(getFields(), getClass().getSimpleName(), "fields");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
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
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.tableName);
        WriterHelper.packMapStringValueValues(packer, this.fields);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event descriptor",
                EventHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "event descriptor",
                    EventHolderImpl.class.getSimpleName());

            tableName = ReaderHelper.unpackStringValue(unpacker, "table name",
                    EventHolderImpl.class.getSimpleName());

            fields = ReaderHelper.unpackMapStringValueValues(unpacker,
                    null,
                    "field names and values",
                    "field names and values map key (fieldname)",
                    "field names and values map value (fieldvalue)",
                    EventHolderImpl.class.getSimpleName());

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHolderImpl that = (EventHolderImpl) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, fields);
    }

    @Override
    public String toString() {
        return "EventHolderImpl{" +
                "tableName='" + tableName + '\'' +
                ", noOfFields=" + fields.size() +
                '}';
    }
}
