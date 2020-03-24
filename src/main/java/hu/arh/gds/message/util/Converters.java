package hu.arh.gds.message.util;

import hu.arh.gds.message.data.*;
import hu.arh.gds.message.data.impl.QueryContextHolderSerializableImpl;
import org.msgpack.value.Value;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converters {

    public static QueryContextHolderSerializableImpl getQueryContextDescriptorSerializable(QueryContextHolder queryContextHolder) throws Exception {
        List<Object> fieldValues = new ArrayList<>();
        for(Value value: queryContextHolder.getFieldValues()) {
            fieldValues.add(Converters.convertToObject(value));
        }
        return new QueryContextHolderSerializableImpl(queryContextHolder.getScrollId(),
                queryContextHolder.getQuery(), queryContextHolder.getDeliveredNumberOfHits(), queryContextHolder.getQueryStartTime(),
                queryContextHolder.getConsistencyType(), queryContextHolder.getLastBucketId(), queryContextHolder.getGDSHolder().getClusterName(),
                queryContextHolder.getGDSHolder().getGDSNodeName(), fieldValues, queryContextHolder.getPartitionNames());
    }

    public static Object convertToObject(Value messagePackValue) throws Exception {
        if (messagePackValue.isArrayValue()) {
            List<Value> list = messagePackValue.asArrayValue().list();
            List<Object> result = new ArrayList<>(list.size());
            for (Value item : list) {
                result.add(convertToObject(item));
            }
            return result;
        }

        if (messagePackValue.isBinaryValue()) {
            return messagePackValue.asBinaryValue().asByteArray();
        }

        if (messagePackValue.isBooleanValue()) {
            return messagePackValue.asBooleanValue().getBoolean();
        }

        if (messagePackValue.isExtensionValue()) {
            return messagePackValue.asExtensionValue().getData();
        }

        if (messagePackValue.isFloatValue()) {
            return messagePackValue.asNumberValue().toDouble();
        }

        if (messagePackValue.isIntegerValue()) {
            return messagePackValue.asNumberValue().toLong();
        }

        if (messagePackValue.isMapValue()) {
            throw new IllegalStateException("Message pack map not allowed to convert!");
        }

        if (messagePackValue.isNilValue()) {
            return null;
        }

        if (messagePackValue.isNumberValue()) {
            throw new Exception("NOT FLOAT NOR INTEGER TYPE VALUE");
            //return hu.vrs.alyxia.value.Value.create(messagePackValue.asNumberValue().toDouble());
        }

        if (messagePackValue.isStringValue()) {
            return new String(messagePackValue.asStringValue().asByteArray(), StandardCharsets.UTF_8);
        }

        if (messagePackValue.isRawValue()) {
            return messagePackValue.asRawValue().asByteArray();
        }

        throw new Exception("Unknown type");
    }

    public static List<Map<String, Value>> getRecordsToMpack(MessageData data) throws ValueConvertException {
        switch (data.getTypeHelper().getMessageDataType()) {
            case QUERY_REQUEST_ACK_11:
                MessageData11QueryRequestAck data11 = data.getTypeHelper().asQueryRequestAckMessageData11();
                return getRecordsToMpack(data11.getQueryResponseHolder().getfFieldHolders(),
                        data11.getQueryResponseHolder().getHits());
            case EVENT_DOCUMENT_8:
                MessageData8EventDocument data8 = data.getTypeHelper().asEventDocumentMessageData8();
                return getRecordsToMpack(data8.getFieldHolders(), data8.getRecords());
            default:
                throw new ValueConvertException(String.format("Converting from '%s' does not supported", data.getTypeHelper().getMessageDataType()));
        }
    }

    public static List<Map<String, Object>> getRecordsToObject(MessageData data) throws ValueConvertException {
        switch (data.getTypeHelper().getMessageDataType()) {
            case QUERY_REQUEST_ACK_11:
                MessageData11QueryRequestAck data11 = data.getTypeHelper().asQueryRequestAckMessageData11();
                return getRecordsToObject(data11.getQueryResponseHolder().getfFieldHolders(),
                        data11.getQueryResponseHolder().getHits());
            case EVENT_DOCUMENT_8:
                MessageData8EventDocument data8 = data.getTypeHelper().asEventDocumentMessageData8();
                return getRecordsToObject(data8.getFieldHolders(), data8.getRecords());
            default:
                throw new ValueConvertException(String.format("Converting from '%s' does not supported", data.getTypeHelper().getMessageDataType()));
        }
    }

    private static List<Map<String, Object>> getRecordsToObject(List<FieldHolder> fieldHolders,
                                                              List<List<Value>> hits) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (List<Value> values : hits) {
            Map<String, Object> record = new HashMap<>();
            for (int i = 0; i < fieldHolders.size(); i++) {
                try {
                    record.put(fieldHolders.get(i).getFieldName(), convertToObject(values.get(i)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            result.add(record);
        }
        return result;
    }

    private static List<Map<String, Value>> getRecordsToMpack(List<FieldHolder> fieldHolders,
                                                              List<List<Value>> hits) {
        List<Map<String, Value>> result = new ArrayList<>();
        for (List<Value> values : hits) {
            Map<String, Value> record = new HashMap<>();
            for (int i = 0; i < fieldHolders.size(); i++) {
                try {
                    record.put(fieldHolders.get(i).getFieldName(), values.get(i));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            result.add(record);
        }
        return result;
    }
}
