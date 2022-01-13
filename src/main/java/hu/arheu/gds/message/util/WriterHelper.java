
package hu.arheu.gds.message.util;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.errors.WriteException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableBinaryValueImpl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "UnusedReturnValue"}) //API class, not all methods are used across the project.
public class WriterHelper {

    public static void packArrayHeader(MessageBufferPacker packer, int size) throws WriteException {
        try {
            packer.packArrayHeader(size);
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapHeader(MessageBufferPacker packer, int size) throws WriteException {
        try {
            packer.packMapHeader(size);
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, String value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packString(value);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, Boolean value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packBoolean(value);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, Integer value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packInt(value);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, Long value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packLong(value);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, Double value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packDouble(value);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, byte[] values) throws WriteException {
        try {
            if (values == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packValue(new ImmutableBinaryValueImpl(values));
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValue(MessageBufferPacker packer, Value value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                packer.packValue(value);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packStringCollection(MessageBufferPacker packer, Collection<String> values) throws WriteException {
        try {
            if (values != null) {
                packArrayHeader(packer, values.size());
                for (String value : values) {
                    packValue(packer, value);
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packIntegerCollection(MessageBufferPacker packer, Collection<Integer> values) throws WriteException {
        try {
            if (values != null) {
                packArrayHeader(packer, values.size());
                for (Integer value : values) {
                    packValue(packer, value);
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValueCollection(MessageBufferPacker packer, Collection<Value> values) throws WriteException {
        try {
            if (values != null) {
                packArrayHeader(packer, values.size());
                for (Value value : values) {
                    packValue(packer, value);
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packValueListListValues(MessageBufferPacker packer, List<List<Value>> values)
            throws WriteException {
        try {
            if (values != null) {
                packArrayHeader(packer, values.size());
                for (List<Value> value : values) {
                    packValueCollection(packer, value);
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapStringValueValues(MessageBufferPacker packer, Map<String, Value> values)
            throws WriteException {

        try {
            if (values != null) {
                packMapHeader(packer, values.size());
                for (Map.Entry<String, Value> entry : values.entrySet()) {
                    packValue(packer, entry.getKey());
                    packValue(packer, entry.getValue());
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapIntegerStringValues(MessageBufferPacker packer, Map<Integer, String> values)
            throws WriteException {
        try {
            if (values != null) {
                packMapHeader(packer, values.size());
                for (Map.Entry<Integer, String> entry : values.entrySet()) {
                    packValue(packer, entry.getKey());
                    packValue(packer, entry.getValue());
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapIntegerStringListValues(MessageBufferPacker packer, Map<Integer, List<String>> values)
            throws WriteException {
        try {
            if (values != null) {
                packMapHeader(packer, values.size());
                for (Map.Entry<Integer, List<String>> entry : values.entrySet()) {
                    packValue(packer, entry.getKey());
                    packStringCollection(packer, entry.getValue());
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapStringByteArrayValues(MessageBufferPacker packer, Map<String, byte[]> values)
            throws WriteException {
        try {
            if (values != null) {
                packMapHeader(packer, values.size());
                for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                    packValue(packer, entry.getKey());
                    packValue(packer, entry.getValue());
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapStringPackableValues(MessageBufferPacker packer, Map<String, ? extends MessagePart> values)
            throws WriteException {
        try {
            if (values != null) {
                packMapHeader(packer, values.size());
                for (Map.Entry<String, ? extends MessagePart> entry : values.entrySet()) {
                    packValue(packer, entry.getKey());
                    packMessagePart(packer, entry.getValue());
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMapIntegerBooleanValues(MessageBufferPacker packer, Map<Integer, Boolean> values)
            throws WriteException {
        try {
            if (values != null) {
                packMapHeader(packer, values.size());
                for (Map.Entry<Integer, Boolean> entry : values.entrySet()) {
                    packValue(packer, entry.getKey());
                    packValue(packer, entry.getValue());
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMessagePart(MessageBufferPacker packer, GdsMessagePart value) throws WriteException {
        try {
            if (value == null) {
                WriterHelper.packNil(packer);
            } else {
                value.packContentTo(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packMessagePartCollection(MessageBufferPacker packer, Collection<? extends GdsMessagePart> values) throws WriteException {
        try {
            if (values != null) {
                packArrayHeader(packer, values.size());
                for (GdsMessagePart value : values) {
                    packMessagePart(packer, value);
                }
            } else {
                WriterHelper.packNil(packer);
            }
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packNil(MessageBufferPacker packer) throws WriteException {
        try {
            packer.packNil();
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }

    public static void packEmptyMap(MessageBufferPacker packer) throws WriteException {
        try {
            packer.packMapHeader(0);
        } catch (IOException e) {
            throw new WriteException(e);
        }
    }
}
