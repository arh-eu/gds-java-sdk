
package hu.arheu.gds.message.util;

import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "UnusedReturnValue"}) //API class, not all methods are used across the project.
public class ReaderHelper {

    public static int unpackArrayHeader(MessageUnpacker unpacker) throws ReadException {
        try {
            return unpacker.unpackArrayHeader();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }

    public static int unpackMapHeader(MessageUnpacker unpacker) throws ReadException {
        try {
            return unpacker.unpackMapHeader();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }

    public static int unpackArrayHeader(MessageUnpacker unpacker,
                                        Integer expected,
                                        String arrayName,
                                        String className) throws ReadException {

        int headerSize = ReaderHelper.unpackArrayHeader(unpacker);
        if (expected == null) {
            return headerSize;
        } else {
            if (headerSize < expected) {
                throw new ReadException(
                        String.format("%s: Array header size (%s) does not match the expected minimum value (%s). Array name: %s.",
                                className,
                                headerSize,
                                expected,
                                arrayName));
            } else {
                return headerSize;
            }
        }
    }

    public static int unpackArrayHeaderStrictly(MessageUnpacker unpacker,
                                                int expected1,
                                                int expected2,
                                                String arrayName,
                                                String className) throws ReadException {

        int headerSize = ReaderHelper.unpackArrayHeader(unpacker);
        if (headerSize != expected1 && headerSize != expected2) {
            throw new ReadException(
                    String.format("%s: Array header size (%s) does not match expected value (%s or %s). Array name: " +
                                    "%s.",
                            className,
                            headerSize,
                            expected1,
                            expected2,
                            arrayName));
        } else {
            return headerSize;
        }
    }

    public static int unpackMapHeader(MessageUnpacker unpacker,
                                      Integer expected,
                                      String mapName,
                                      String className) throws ReadException {

        int headerSize = ReaderHelper.unpackMapHeader(unpacker);
        if (expected == null) {
            return headerSize;
        } else {
            if (headerSize != expected) {
                throw new ReadException(
                        String.format("%s: Map header size (%s) does not match expected value (%s). Map name: %s",
                                className,
                                headerSize,
                                expected,
                                mapName));
            } else {
                return headerSize;
            }
        }
    }

    public static String unpackStringValue(MessageUnpacker unpacker,
                                           String fieldName,
                                           String className) throws ReadException {

        Value unpackedValue = ReaderHelper.unpackValue(unpacker);
        if (unpackedValue.isNilValue()) {
            return null;
        } else if (unpackedValue.getValueType() != ValueType.STRING) {
            throw new ReadException(
                    String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            className,
                            unpackedValue.getValueType(),
                            ValueType.STRING,
                            fieldName));
        } else {
            return unpackedValue.asStringValue().asString();
        }
    }

    public static <T extends Enum<T>> T unpackEnumValueAsString(MessageUnpacker unpacker,
                                                                Class<T> enumType,
                                                                String fieldName,
                                                                String className) throws ReadException {

        Value unpackedValue = ReaderHelper.unpackValue(unpacker);
        if (unpackedValue.isNilValue()) {
            return null;
        } else if (unpackedValue.getValueType() != ValueType.STRING) {
            throw new ReadException(
                    String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            className,
                            unpackedValue.getValueType(),
                            ValueType.STRING,
                            fieldName));
        } else {
            try {
                return Enum.valueOf(enumType, unpackedValue.asStringValue().asString());
            } catch (IllegalArgumentException e) {
                throw new ReadException(
                        String.format("Cannot convert field value (%s) to enum. Location: Field name: %s.",
                                unpackedValue.asStringValue().asStringValue(),
                                fieldName));
            }
        }
    }

    public static Boolean unpackBooleanValue(MessageUnpacker unpacker,
                                             String fieldName,
                                             String className) throws ReadException {

        Value unpackedValue = ReaderHelper.unpackValue(unpacker);
        if (unpackedValue.isNilValue()) {
            return null;
        } else if (unpackedValue.getValueType() != ValueType.BOOLEAN) {
            throw new ReadException(
                    String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            className,
                            unpackedValue.getValueType(),
                            ValueType.BOOLEAN,
                            fieldName));
        } else {
            return unpackedValue.asBooleanValue().getBoolean();
        }
    }

    public static Integer unpackIntegerValue(MessageUnpacker unpacker,
                                             String fieldName,
                                             String className) throws ReadException {

        Value unpackedValue = ReaderHelper.unpackValue(unpacker);
        if (unpackedValue.isNilValue()) {
            return null;
        } else if (unpackedValue.getValueType() != ValueType.INTEGER) {
            throw new ReadException(
                    String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            className,
                            unpackedValue.getValueType(),
                            ValueType.INTEGER,
                            fieldName));
        } else {
            return unpackedValue.asIntegerValue().asInt();
        }
    }

    public static Long unpackLongValue(MessageUnpacker unpacker,
                                       String fieldName,
                                       String className) throws ReadException {

        Value unpackedValue = ReaderHelper.unpackValue(unpacker);
        if (unpackedValue.isNilValue()) {
            return null;
        } else if (!unpackedValue.getValueType().isNumberType()) {
            throw new ReadException(
                    String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            className,
                            unpackedValue.getValueType(),
                            "NUMBER",
                            fieldName));
        } else {
            return unpackedValue.asNumberValue().toLong();
        }
    }
    public static long unpackNotNullLongValue(MessageUnpacker unpacker,
                                       String fieldName,
                                       String className) throws ReadException, ValidationException {

        Long value = unpackLongValue(unpacker, fieldName, className);
        if(value == null){
            throw new ValidationException(String.format("Long value for field %1$s cannot be null!", fieldName));
        }
        return value;
    }

    public static byte[] unpackBinary(MessageUnpacker unpacker,
                                      String fieldName,
                                      String className) throws ReadException {

        Value unpackedValue = ReaderHelper.unpackValue(unpacker);
        if (unpackedValue.isNilValue()) {
            return null;
        } else if (unpackedValue.getValueType() != ValueType.BINARY) {
            throw new ReadException(
                    String.format("%s: Field type (%s) does not match expected type (%s). Field name: %s.",
                            className,
                            unpackedValue.getValueType(),
                            ValueType.BINARY,
                            fieldName));
        } else {
            return unpackedValue.asBinaryValue().asByteArray();
        }
    }

    public static Value unpackValue(MessageUnpacker unpacker,
                                    String fieldName,
                                    String className) throws ReadException {

        try {
            return ReaderHelper.unpackValue(unpacker);
        } catch (IOException e) {
            throw new ReadException(
                    String.format("%s: Exception message: %s. Field name: %s", className, e, fieldName));
        }
    }

    public static boolean nextExpectedValueTypeIsNil(MessageUnpacker unpacker,
                                                     ValueType expectedValueType,
                                                     String valueName,
                                                     String className) throws ReadException {

        ValueType nextValueType = ReaderHelper.getNextValueType(unpacker);
        if (nextValueType.equals(expectedValueType)) {
            return false;
        } else if (!nextValueType.isNilType()) {
            throw new ReadException(String.format("%s: Value type (%s) does not match expected type (%s). Value name: %s.",
                    className,
                    nextValueType,
                    expectedValueType.toString(),
                    valueName));
        } else {
            return true;
        }
    }

    public static ValueType getNextValueType(MessageUnpacker unpacker) throws ReadException {
        try {
            return unpacker.getNextFormat().getValueType();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }

    public static List<String> unpackStringValues(MessageUnpacker unpacker,
                                                  Integer expectedArrayHeaderSize,
                                                  String arrayName,
                                                  String fieldName,
                                                  String className) throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, arrayName, className)) {
            List<String> temp = new ArrayList<>();
            int arrayHeaderSize = unpackArrayHeader(unpacker, expectedArrayHeaderSize, arrayName, className);
            for (int i = 0; i < arrayHeaderSize; i++) {
                temp.add(unpackStringValue(unpacker, fieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static List<Integer> unpackIntegerValues(MessageUnpacker unpacker,
                                                    Integer expectedArrayHeaderSize,
                                                    String arrayName,
                                                    String fieldName,
                                                    String className) throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, arrayName, className)) {
            List<Integer> temp = new ArrayList<>();
            int arrayHeaderSize = unpackArrayHeader(unpacker, expectedArrayHeaderSize, arrayName, className);
            for (int i = 0; i < arrayHeaderSize; i++) {
                temp.add(unpackIntegerValue(unpacker, fieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static List<Value> unpackValueValues(MessageUnpacker unpacker,
                                                Integer expectedArrayHeaderSize,
                                                String arrayName,
                                                String fieldName,
                                                String className) throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, arrayName, className)) {
            List<Value> temp = new ArrayList<>();
            int arrayHeaderSize = unpackArrayHeader(unpacker, expectedArrayHeaderSize, arrayName, className);
            for (int i = 0; i < arrayHeaderSize; i++) {
                temp.add(unpackValue(unpacker, fieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }


    public static List<List<Value>> unpackValueListListValues(MessageUnpacker unpacker,
                                                              Integer expectedExternalArrayHeaderSize,
                                                              Integer expectedInternalArrayHeaderSize,
                                                              String externalArrayName,
                                                              String internalArrayName,
                                                              String fieldName,
                                                              String className) throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, externalArrayName, className)) {
            List<List<Value>> temp = new ArrayList<>();
            int externalArrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, expectedExternalArrayHeaderSize,
                    internalArrayName, className);
            for (int i = 0; i < externalArrayHeaderSize; i++) {
                temp.add(unpackValueValues(unpacker, expectedInternalArrayHeaderSize, fieldName, internalArrayName,
                        className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static Map<String, Value> unpackMapStringValueValues(MessageUnpacker unpacker,
                                                                Integer expectedMapHeaderSize,
                                                                String mapName,
                                                                String keyFieldName,
                                                                String valueFieldName,
                                                                String className) throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, mapName, className)) {
            Map<String, Value> temp = new HashMap<>();
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, expectedMapHeaderSize, mapName, className);
            for (int i = 0; i < mapHeaderSize; i++) {
                temp.put(unpackStringValue(unpacker, keyFieldName, className),
                        unpackValue(unpacker, valueFieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }


    public static Map<Integer, String> unpackMapIntegerStringValues(MessageUnpacker unpacker,
                                                                    Integer expectedMapHeaderSize,
                                                                    String mapName,
                                                                    String keyFieldName,
                                                                    String valueFieldName,
                                                                    String className)
            throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, mapName, className)) {
            Map<Integer, String> temp = new HashMap<>();
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, expectedMapHeaderSize, mapName, className);
            for (int i = 0; i < mapHeaderSize; i++) {
                temp.put(unpackIntegerValue(unpacker, keyFieldName, className),
                        unpackStringValue(unpacker, valueFieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static Map<Integer, List<String>> unpackMapIntegerStringListValues(MessageUnpacker unpacker,
                                                                              Integer expectedMapHeaderSize,
                                                                              Integer expectedArrayHeaderSize,
                                                                              String mapName,
                                                                              String keyFieldName,
                                                                              String valueFieldName,
                                                                              String fieldName,
                                                                              String className)
            throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, mapName, className)) {
            Map<Integer, List<String>> temp = new HashMap<>();
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, expectedMapHeaderSize, mapName, className);
            for (int i = 0; i < mapHeaderSize; i++) {
                temp.put(unpackIntegerValue(unpacker, keyFieldName, className),
                        unpackStringValues(unpacker, expectedArrayHeaderSize, valueFieldName, fieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static Map<String, byte[]> unpackMapStringByteArrayValues(MessageUnpacker unpacker,
                                                                     Integer expectedMapHeaderSize,
                                                                     String mapName,
                                                                     String keyFieldName,
                                                                     String valueFieldName,
                                                                     String className)
            throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, mapName, className)) {
            Map<String, byte[]> temp = new HashMap<>();
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, expectedMapHeaderSize, mapName, className);
            for (int i = 0; i < mapHeaderSize; i++) {
                temp.put(unpackStringValue(unpacker, keyFieldName, className),
                        unpackBinary(unpacker, valueFieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static Map<Integer, Boolean> unpackMapIntegerBooleanValues(MessageUnpacker unpacker,
                                                                      Integer expectedMapHeaderSize,
                                                                      String mapName,
                                                                      String keyFieldName,
                                                                      String valueFieldName,
                                                                      String className)
            throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, mapName, className)) {
            Map<Integer, Boolean> temp = new HashMap<>();
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, expectedMapHeaderSize, mapName, className);
            for (int i = 0; i < mapHeaderSize; i++) {
                temp.put(unpackIntegerValue(unpacker, keyFieldName, className),
                        unpackBooleanValue(unpacker, valueFieldName, className));
            }
            return temp;
        } else {
            unpackNil(unpacker);
        }
        return null;
    }

    public static boolean isNextNil(MessageUnpacker unpacker) throws ReadException {
        try {
            return unpacker.getNextFormat().getValueType().isNilType();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }

    public static void unpackNil(MessageUnpacker unpacker) throws ReadException {
        try {
            unpacker.unpackNil();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }

    public static Value unpackValue(MessageUnpacker unpacker) throws ReadException {
        try {
            return unpacker.unpackValue();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }

}
