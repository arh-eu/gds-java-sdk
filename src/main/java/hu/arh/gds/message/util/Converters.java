package hu.arh.gds.message.util;

import hu.arh.gds.message.data.QueryContextHolder;
import hu.arh.gds.message.data.impl.QueryContextHolderSerializableImpl;
import org.msgpack.value.Value;
import org.msgpack.value.impl.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    private Converters() {
    }

    /**
     * Converts a query context holder which has MessagePack Value types to plain Java values.
     * The client does not need to invoke this.
     *
     * @param queryContextHolder the original context holder.
     * @return the ContextHolder value containing only POJOs.
     */
    public static QueryContextHolderSerializableImpl getQueryContextDescriptorSerializable(QueryContextHolder queryContextHolder) {
        List<Object> fieldValues = new ArrayList<>();
        for (Value value : queryContextHolder.getFieldValues()) {
            fieldValues.add(Converters.convertToJavaObject(value));
        }
        return new QueryContextHolderSerializableImpl(queryContextHolder.getScrollId(),
                queryContextHolder.getQuery(), queryContextHolder.getDeliveredNumberOfHits(), queryContextHolder.getQueryStartTime(),
                queryContextHolder.getConsistencyType(), queryContextHolder.getLastBucketId(), queryContextHolder.getGDSHolder().getClusterName(),
                queryContextHolder.getGDSHolder().getGDSNodeName(), fieldValues, queryContextHolder.getPartitionNames());
    }

    /**
     * Converts a parameter from MessagePack Value format to plain Java type.
     *
     * @param messagePackValue the value in MessagePack Value format
     * @return the converted Java value.
     * @throws IllegalArgumentException if the type cannot be resolved
     */
    public static Object convertToJavaObject(Value messagePackValue) {
        if (messagePackValue.isArrayValue()) {
            List<Value> list = messagePackValue.asArrayValue().list();
            List<Object> result = new ArrayList<>(list.size());
            for (Value item : list) {
                result.add(convertToJavaObject(item));
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
            throw new IllegalArgumentException("NOT FLOAT NOR INTEGER TYPE VALUE");
        }

        if (messagePackValue.isStringValue()) {
            return new String(messagePackValue.asStringValue().asByteArray(), StandardCharsets.UTF_8);
        }

        if (messagePackValue.isRawValue()) {
            return messagePackValue.asRawValue().asByteArray();
        }

        throw new IllegalArgumentException("Unknown type: " + messagePackValue.getValueType().toString());
    }


    /**
     * Converts a plain Java value ({@link Object}) to a MessagePack formatted {@link Value} instance.
     *
     * @param object the Java object to be converted
     * @return the MessagePack representation of the given object.
     * @throws IllegalArgumentException if the given object cannot be represented natively by the MessagePack standard
     */
    public static Value convertToMessagePackValue(Object object) {

        if (object == null) {
            return ImmutableNilValueImpl.get();
        }

        if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            Value[] values = new Value[objects.length];
            for (int i = 0; i < objects.length; ++i) {
                values[i] = convertToMessagePackValue(objects[i]);
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

        throw new IllegalArgumentException(String.format("Could not convert the type '%1$s'!", object.getClass().getName()));
    }
}
