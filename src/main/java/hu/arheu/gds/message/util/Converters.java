package hu.arheu.gds.message.util;

import hu.arheu.gds.message.data.FieldHolder;
import hu.arheu.gds.message.data.MessageData;
import hu.arheu.gds.message.data.MessageData11QueryRequestAck;
import hu.arheu.gds.message.data.MessageData8EventDocument;
import hu.arheu.gds.message.errors.ValueConvertException;
import org.msgpack.value.Value;
import org.msgpack.value.impl.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converters {
    private Converters() {
    }

    /**
     * Converts a given string to hex formatted string by converting every character in it to the proper byte value.
     *
     * @param string the string to be converted
     * @return the converted, hex representation of the string
     */
    public static String stringToUTF8Hex(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte bb : bytes) {
            sb.append(String.format("%1$02x", 0xff & bb));
        }
        return sb.toString();
    }

    /**
     * Converts a parameter from MessagePack Value format to plain Java type.
     *
     * @param messagePackValue the value in MessagePack Value format
     * @return the converted Java value.
     * @throws IllegalArgumentException if the type cannot be resolved
     */
    public static Object convertToObject(Value messagePackValue) throws ValueConvertException {
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
            Map<Object, Object> values = new HashMap<>();
            for (Map.Entry<Value, Value> entry : messagePackValue.asMapValue().map().entrySet()) {
                values.put(convertToObject(entry.getKey()), convertToObject(entry.getValue()));
            }
            return values;
        }

        if (messagePackValue.isNilValue()) {
            return null;
        }

        if (messagePackValue.isNumberValue()) {
            throw new ValueConvertException("The given number value is neither FLOAT nor INTEGER type! Found: " + messagePackValue.getClass());
        }

        if (messagePackValue.isStringValue()) {
            return new String(messagePackValue.asStringValue().asByteArray(), StandardCharsets.UTF_8);
        }

        if (messagePackValue.isRawValue()) {
            return messagePackValue.asRawValue().asByteArray();
        }

        throw new ValueConvertException("Unknown type found: " + messagePackValue.getClass());
    }

    public static List<Map<String, Value>> getRecordsToMpack(MessageData data) throws ValueConvertException {
        switch (data.getMessageDataType()) {
            case QUERY_REQUEST_ACK_11:
                MessageData11QueryRequestAck data11 = data.asQueryRequestAckMessageData11();
                return getRecordsToMpack(data11.getQueryResponseHolder().getFieldHolders(),
                        data11.getQueryResponseHolder().getHits());
            case EVENT_DOCUMENT_8:
                MessageData8EventDocument data8 = data.asEventDocumentMessageData8();
                return getRecordsToMpack(data8.getFieldHolders(), data8.getRecords());
            default:
                throw new ValueConvertException(String.format("Converting from '%s' does not supported", data.getMessageDataType()));
        }
    }

    public static List<Map<String, Object>> getRecordsToObject(MessageData data) throws ValueConvertException {
        switch (data.getMessageDataType()) {
            case QUERY_REQUEST_ACK_11:
                MessageData11QueryRequestAck data11 = data.asQueryRequestAckMessageData11();
                return getRecordsToObject(data11.getQueryResponseHolder().getFieldHolders(),
                        data11.getQueryResponseHolder().getHits());
            case EVENT_DOCUMENT_8:
                MessageData8EventDocument data8 = data.asEventDocumentMessageData8();
                return getRecordsToObject(data8.getFieldHolders(), data8.getRecords());
            default:
                throw new ValueConvertException(String.format("Converting from '%s' does not supported", data.getMessageDataType()));
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

    /**
     * Converts a plain Java value ({@link Object}) to a MessagePack formatted {@link Value} instance.
     *
     * @param object the Java object to be converted
     * @return the MessagePack representation of the given object.
     * @throws IllegalArgumentException if the given object cannot be represented natively by the MessagePack standard
     */
    public static Value convertObjectToValue(Object object) throws IllegalStateException {
        if (object == null) {
            return ImmutableNilValueImpl.get();
        }

        if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            Value[] values = new Value[objects.length];
            for (int i = 0; i < objects.length; ++i) {
                values[i] = convertObjectToValue(objects[i]);
            }
            return new ImmutableArrayValueImpl(values);
        }

        if (object instanceof byte[]) {
            return new ImmutableBinaryValueImpl((byte[]) object);
        }

        if (object instanceof Boolean) {
            if ((Boolean) object) {
                return ImmutableBooleanValueImpl.TRUE;
            } else {
                return ImmutableBooleanValueImpl.FALSE;
            }
        }

        if (object instanceof Float) {
            return new ImmutableDoubleValueImpl((Float) object);
        }

        if (object instanceof Double) {
            return new ImmutableDoubleValueImpl((Double) object);
        }

        if (object instanceof Integer) {
            return new ImmutableLongValueImpl((Integer) object);
        }

        if (object instanceof Long) {
            return new ImmutableLongValueImpl((Long) object);
        }

        if (object instanceof String) {
            return new ImmutableStringValueImpl((String) object);
        }

        throw new IllegalStateException("Unknown type: " + object.getClass());
    }
}
