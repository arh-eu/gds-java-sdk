
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.FieldHolder;
import hu.arheu.gds.message.data.MessageData8EventDocument;
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
import java.util.*;


public class MessageData8EventDocumentImpl extends MessagePart implements MessageData8EventDocument {

    private String tableName;
    private List<FieldHolder> fieldHolders;
    private List<List<Value>> records;
    private Map<Integer, List<String>> returningOptions;

    //not serialized
    private List<List<Object>> recordsObject;
    private List<Map<String, Value>> recordsMap;
    private List<Map<String, Object>> recordsObjectMap;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData8EventDocumentImpl() {
    }

    public MessageData8EventDocumentImpl(String tableName,
                                         List<FieldHolder> fieldHolders,
                                         List<List<Value>> records,
                                         Map<Integer, List<String>> returningOptions) throws ValidationException {

        this.tableName = tableName;
        this.fieldHolders = fieldHolders;
        this.records = records;
        this.returningOptions = returningOptions;
        checkContent();
    }

    public MessageData8EventDocumentImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData8EventDocumentImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }


    @Override
    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(this.tableName, this.getClass().getSimpleName(),
                "tableName");
        Validator.requireNonNullValue(this.fieldHolders, this.getClass().getSimpleName(),
                "fieldHolders");
        Validator.requireNonNullValue(this.records, this.getClass().getSimpleName(),
                "records");
        Validator.requireNonNullValue(this.returningOptions, this.getClass().getSimpleName(),
                "returningOptions");
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public List<FieldHolder> getFieldHolders() {
        return this.fieldHolders;
    }

    @Override
    public List<List<Value>> getRecords() {
        return this.records;
    }

    @Override
    public List<List<Object>> getRecordsObject() {
        if (recordsObject == null) {
            recordsObject = new ArrayList<>();
            for (List<Value> o : records) {
                recordsObject.add(new ArrayList<>(o));
            }
        }
        return recordsObject;
    }

    @Override
    public List<Map<String, Value>> getRecordsMap() {
        if (recordsMap == null) {
            recordsMap = new ArrayList<>();
            for (List<Value> values : records) {
                Map<String, Value> record = new HashMap<>();
                for (int i = 0; i < fieldHolders.size(); i++) {
                    record.put(fieldHolders.get(i).getFieldName(), values.get(i));
                }
                recordsMap.add(record);
            }
        }
        return recordsMap;
    }

    @Override
    public List<Map<String, Object>> getRecordsObjectMap() {
        if (recordsObjectMap == null) {
            recordsObjectMap = new ArrayList<>();
            for (Map<String, Value> o : getRecordsMap()) {
                recordsObjectMap.add(new HashMap<>(o));
            }
        }
        return recordsObjectMap;
    }

    @Override
    public Map<Integer, List<String>> getReturningOptions() {
        return this.returningOptions;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, 4);
        WriterHelper.packValue(packer, this.tableName);
        WriterHelper.packMessagePartCollection(packer, this.fieldHolders);
        WriterHelper.packValueListListValues(packer, this.records);
        WriterHelper.packEmptyMap(packer);
        //WriterHelper.packMapIntegerStringListValues(packer, this.returningOptions);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event document data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, null, "event document data",
                    this.getClass().getSimpleName());

            this.tableName = ReaderHelper.unpackStringValue(unpacker, "table name",
                    this.getClass().getSimpleName());

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                    this.getClass().getSimpleName())) {

                this.fieldHolders = new ArrayList<>();
                int fieldHoldersSize = ReaderHelper.unpackArrayHeader(unpacker, null,
                        "field descriptors",
                        this.getClass().getSimpleName());

                for (int i = 0; i < fieldHoldersSize; i++) {
                    FieldHolderImpl holder = new FieldHolderImpl();
                    holder.unpackContentFrom(unpacker);
                    this.fieldHolders.add(holder);
                }
            } else {
                ReaderHelper.unpackNil(unpacker);
            }

            this.records = ReaderHelper.unpackValueListListValues(unpacker,
                    null,
                    null,
                    "records",
                    "record",
                    "fieldvalue",
                    this.getClass().getSimpleName());

            this.returningOptions = ReaderHelper.unpackMapIntegerStringListValues(unpacker,
                    null,
                    null, "returning options",
                    "returning options map key",
                    "returning options map value (fieldnames)",
                    "fieldname",
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
        MessageData8EventDocumentImpl that = (MessageData8EventDocumentImpl) o;
        return Objects.equals(tableName, that.tableName)
                && Objects.equals(fieldHolders, that.fieldHolders)
                && Objects.equals(records, that.records)
                && Objects.equals(returningOptions, that.returningOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, fieldHolders, records, returningOptions);
    }

    @Override
    public String toString() {
        return "MessageData8EventDocumentImpl{" +
                "tableName='" + tableName + '\'' +
                ", fieldHoldersLen=" + fieldHolders.size() +
                ", recordsLen=" + records.size() +
                ", returningOptionsLen=" + returningOptions.size() +
                '}';
    }
}
