/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arheu.gds.message.util;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableBinaryValueImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author oliver.nagy
 */
public class WriterHelper {

    public static void packArrayHeader(MessageBufferPacker packer, int size) throws IOException {
        packer.packArrayHeader(size);
    }

    public static void packMapHeader(MessageBufferPacker packer, int size) throws IOException {
        packer.packMapHeader(size);
    }

    public static void packValue(MessageBufferPacker packer, String value) throws IOException {
        if (value == null) {
            packer.packNil();
        } else {
            packer.packString(value);
        }
    }

    public static void packValue(MessageBufferPacker packer, Boolean value) throws IOException {
        if (value == null) {
            packer.packNil();
        } else {
            packer.packBoolean(value);
        }
    }

    public static void packValue(MessageBufferPacker packer, Integer value) throws IOException {
        if (value == null) {
            packer.packNil();
        } else {
            packer.packInt(value);
        }
    }

    public static void packValue(MessageBufferPacker packer, Long value) throws IOException {
        if (value == null) {
            packer.packNil();
        } else {
            packer.packLong(value);
        }
    }

    public static void packValue(MessageBufferPacker packer, Double value) throws IOException {
        if (value == null) {
            packer.packNil();
        } else {
            packer.packDouble(value);
        }
    }

    public static void packValue(MessageBufferPacker packer, byte[] values) throws IOException {
        if (values == null) {
            packer.packNil();
        } else {
            packer.packValue(new ImmutableBinaryValueImpl(values));
        }
    }

    public static void packValue(MessageBufferPacker packer, Value value) throws IOException {
        if (value == null) {
            packer.packNil();
        } else {
            packer.packValue(value);
        }
    }

    public static void packStringValues(MessageBufferPacker packer, List<String> values) throws IOException {
        if (values != null) {
            packArrayHeader(packer, values.size());
            for (String value : values) {
                packValue(packer, value);
            }
        } else {
            packer.packNil();
        }
    }

    public static void packIntegerValues(MessageBufferPacker packer, List<Integer> values) throws IOException {
        if (values != null) {
            packArrayHeader(packer, values.size());
            for (Integer value : values) {
                packValue(packer, value);
            }
        } else {
            packer.packNil();
        }
    }

    public static void packValueValues(MessageBufferPacker packer, List<Value> values) throws IOException {
        if (values != null) {
            packArrayHeader(packer, values.size());
            for (Value value : values) {
                packValue(packer, value);
            }
        } else {
            packer.packNil();
        }
    }

    public static void packValueListListValues(MessageBufferPacker packer, List<List<Value>> values)
            throws IOException {

        if (values != null) {
            packArrayHeader(packer, values.size());
            for (List<Value> value : values) {
                packValueValues(packer, value);
            }
        } else {
            packer.packNil();
        }
    }

    public static void packMapStringValueValues(MessageBufferPacker packer, Map<String, Value> values)
            throws IOException {

        if (values != null) {
            packMapHeader(packer, values.size());
            for (Map.Entry<String, Value> entry : values.entrySet()) {
                packValue(packer, entry.getKey());
                packValue(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static void packMapIntegerStringValues(MessageBufferPacker packer, Map<Integer, String> values)
            throws IOException {

        if (values != null) {
            packMapHeader(packer, values.size());
            for (Map.Entry<Integer, String> entry : values.entrySet()) {
                packValue(packer, entry.getKey());
                packValue(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static void packMapIntegerStringListValues(MessageBufferPacker packer, Map<Integer, List<String>> values)
            throws IOException {

        if (values != null) {
            packMapHeader(packer, values.size());
            for (Map.Entry<Integer, List<String>> entry : values.entrySet()) {
                packValue(packer, entry.getKey());
                packStringValues(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static void packMapStringByteArrayValues(MessageBufferPacker packer, Map<String, byte[]> values)
            throws IOException {

        if (values != null) {
            packMapHeader(packer, values.size());
            for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                packValue(packer, entry.getKey());
                packValue(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static void packMapStringPackableValues(MessageBufferPacker packer, Map<String, ? extends Packable> values)
            throws IOException, ValidationException {

        if (values != null) {
            packMapHeader(packer, values.size());
            for (Map.Entry<String, ? extends Packable> entry : values.entrySet()) {
                packValue(packer, entry.getKey());
                packPackable(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static void packMapIntegerBooleanValues(MessageBufferPacker packer, Map<Integer, Boolean> values)
            throws IOException {

        if (values != null) {
            packMapHeader(packer, values.size());
            for (Map.Entry<Integer, Boolean> entry : values.entrySet()) {
                packValue(packer, entry.getKey());
                packValue(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static void packPackable(MessageBufferPacker packer, Packable value) throws IOException, ValidationException {
        if (value == null) {
            packer.packNil();
        } else {
            value.packContent(packer);
        }
    }

    public static void packPackables(MessageBufferPacker packer, List<? extends Packable> values) throws IOException, ValidationException {
        if (values != null) {
            packArrayHeader(packer, values.size());
            for (Packable value : values) {
                packPackable(packer, value);
            }
        } else {
            packer.packNil();
        }
    }
}
