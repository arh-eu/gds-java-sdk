package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.FieldHolder;
import hu.arheu.gds.message.data.MessageData8EventDocument;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageData8EventDocumentImpl extends MessageData8EventDocument {
    private String tableName;
    private List<FieldHolder> fieldHolders;
    private List<List<Value>> records;
    private List<List<Object>> recordsObject = null;
    private List<Map<String,  Value>> recordsMap = null;
    private List<Map<String,  Object>> recordsObjectMap = null;
    private Map<Integer, List<String>> returningOptions;

    public MessageData8EventDocumentImpl(boolean cache,
                                         String tableName,
                                         List<FieldHolder> fieldHolders,
                                         List<List<Value>> records,
                                         Map<Integer, List<String>> returningOptions) throws IOException, ValidationException {
        this.tableName = tableName;
        this.fieldHolders = fieldHolders;
        this.records = records;
        this.returningOptions = returningOptions;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.tableName, this.getClass().getSimpleName(),
                "tableName");
        ExceptionHelper.requireNonNullValue(this.fieldHolders, this.getClass().getSimpleName(),
                "fieldHolders");
        ExceptionHelper.requireNonNullValue(this.records, this.getClass().getSimpleName(),
                "records");
        ExceptionHelper.requireNonNullValue(this.returningOptions, this.getClass().getSimpleName(),
                "returningOptions");
    }

    public MessageData8EventDocumentImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData8EventDocumentImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.EVENT_DOCUMENT_8;
            }
            @Override
            public MessageData8EventDocumentImpl asEventDocumentMessageData8() {
                return MessageData8EventDocumentImpl.this;
            }
            @Override
            public boolean isEventDocumentMessageData8() {
                return true;
            }
        };
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
        if(recordsObject == null) {
            recordsObject = new ArrayList<>();
            for(List<Value> o: records) {
                recordsObject.add(new ArrayList<>(o));
            }
        }
        return recordsObject;
    }

    @Override
    public List<Map<String, Value>> getRecordsMap() {
        if(recordsMap == null) {
            recordsMap = new ArrayList<>();
            for(List<Value> values: records) {
                Map<String, Value> record = new HashMap<>();
                for(int i = 0; i < fieldHolders.size(); i++) {
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

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, 4);
        WriterHelper.packValue(packer, this.tableName);
        WriterHelper.packPackables(packer, this.fieldHolders);
        WriterHelper.packValueListListValues(packer, this.records);
        WriterHelper.packMapIntegerStringListValues(packer, this.returningOptions);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
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
                    this.fieldHolders.add(FieldHolderImpl.unpackContent(unpacker));
                }
            } else {
                unpacker.unpackNil();
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
                    null, "returnning options",
                    "returning options map key",
                    "returning options map value (fieldnames)",
                    "fieldname",
                    this.getClass().getSimpleName());
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData8EventDocumentImpl that = (MessageData8EventDocumentImpl) o;
        if (tableName != null ? !tableName.equals(that.tableName) : that.tableName != null) return false;
        if (fieldHolders != null ? !fieldHolders.equals(that.fieldHolders) : that.fieldHolders != null) return false;
        if (records != null ? !records.equals(that.records) : that.records != null) return false;
        return returningOptions != null ? returningOptions.equals(that.returningOptions) : that.returningOptions == null;
    }

    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + (fieldHolders != null ? fieldHolders.hashCode() : 0);
        result = 31 * result + (records != null ? records.hashCode() : 0);
        result = 31 * result + (returningOptions != null ? returningOptions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageData8EventDocumentImpl{" +
                "tableName='" + tableName + '\'' +
                ", fieldHolders=" + fieldHolders +
                ", records=" + records +
                ", recordsObject=" + recordsObject +
                ", recordsMap=" + recordsMap +
                ", recordsObjectMap=" + recordsObjectMap +
                ", returningOptions=" + returningOptions +
                '}';
    }
}
