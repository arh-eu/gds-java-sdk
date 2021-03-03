package hu.arheu.gds.message.data;

import org.msgpack.value.Value;
import org.msgpack.value.impl.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum FieldValueType {

    KEYWORD(0) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            return new ImmutableStringValueImpl(o.toString());
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    KEYWORD_ARRAY(1) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            Object[] original = objectToArray(o);
            Value[] data = new Value[original.length];
            for (int i = 0; i < original.length; ++i) {
                if (null == original[i]) {
                    data[i] = ImmutableNilValueImpl.get();
                } else {
                    data[i] = new ImmutableStringValueImpl((String) original[i]);
                }
            }
            return new ImmutableArrayValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return KEYWORD;
        }
    },
    TEXT(2) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            return new ImmutableStringValueImpl(o.toString());
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    BOOLEAN(3) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            return ((boolean) o) ? ImmutableBooleanValueImpl.TRUE : ImmutableBooleanValueImpl.FALSE;
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    DOUBLE(4) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            return new ImmutableDoubleValueImpl((double) o);
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    DOUBLE_ARRAY(5) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            Object[] original = objectToArray(o);
            Value[] data = new Value[original.length];
            for (int i = 0; i < original.length; ++i) {
                if (null == original[i]) {
                    data[i] = ImmutableNilValueImpl.get();
                } else {
                    data[i] = new ImmutableDoubleValueImpl((double) original[i]);
                }
            }
            return new ImmutableArrayValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return DOUBLE;
        }
    },
    INTEGER(6) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            return new ImmutableLongValueImpl((int) o);
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    INTEGER_ARRAY(7) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            Object[] original = objectToArray(o);
            Value[] data = new Value[original.length];
            for (int i = 0; i < original.length; ++i) {
                if (null == original[i]) {
                    data[i] = ImmutableNilValueImpl.get();
                } else {
                    data[i] = new ImmutableLongValueImpl((int) original[i]);
                }
            }
            return new ImmutableArrayValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return INTEGER;
        }
    },
    LONG(8) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            if (o instanceof Integer) {
                return new ImmutableLongValueImpl((int) o);
            } else {
                return new ImmutableLongValueImpl((long) o);
            }
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    BINARY(9) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            return new ImmutableBinaryValueImpl((byte[]) o);
        }

        @Override
        public FieldValueType getBaseType() {
            return this;
        }
    },
    BINARY_ARRAY(10) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            Object[] original = objectToArray(o);
            Value[] data = new Value[original.length];
            for (int i = 0; i < original.length; ++i) {
                if (null == original[i]) {
                    data[i] = ImmutableNilValueImpl.get();
                } else {
                    data[i] = new ImmutableBinaryValueImpl((byte[]) original[i]);
                }
            }
            return new ImmutableArrayValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return BINARY;
        }
    },
    TEXT_ARRAY(11) {
        @Override
        public Value valueFromObject(Object o) {
            //code duplication removed, as we need String array in both cases. SzM.
            return KEYWORD_ARRAY.valueFromObject(o);
        }

        @Override
        public FieldValueType getBaseType() {
            return TEXT;
        }
    },
    BOOLEAN_ARRAY(12) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            Object[] original = objectToArray(o);
            Value[] data = new Value[original.length];
            for (int i = 0; i < original.length; ++i) {
                if (null == original[i]) {
                    data[i] = ImmutableNilValueImpl.get();
                } else {
                    data[i] = (boolean) original[i] ? ImmutableBooleanValueImpl.TRUE : ImmutableBooleanValueImpl.FALSE;
                }
            }
            return new ImmutableArrayValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return BOOLEAN;
        }
    },
    LONG_ARRAY(13) {
        @Override
        public Value valueFromObject(Object o) {
            if (o == null) {
                return ImmutableNilValueImpl.get();
            }
            Object[] original = objectToArray(o);
            Value[] data = new Value[original.length];
            for (int i = 0; i < original.length; ++i) {
                if (null == original[i]) {
                    data[i] = ImmutableNilValueImpl.get();
                } else {
                    if (original[i] instanceof Integer) {
                        data[i] = new ImmutableLongValueImpl((int) original[i]);
                    } else {
                        data[i] = new ImmutableLongValueImpl((long) original[i]);
                    }
                }
            }
            return new ImmutableArrayValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return LONG;
        }
    },
    STRING_MAP(14) {
        @Override
        public Value valueFromObject(Object o) {
            if (!(o instanceof Map)) {
                //also checks for null
                return ImmutableNilValueImpl.get();
            }
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) o;
            Value[] data = new Value[map.size() * 2];
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                data[i] = new ImmutableStringValueImpl(entry.getKey());
                data[i + 1] = new ImmutableStringValueImpl(entry.getValue());
                i += 2;
            }

            return new ImmutableMapValueImpl(data);
        }

        @Override
        public FieldValueType getBaseType() {
            return TEXT;
        }
    };


    private final int value;
    private static final Map<Integer, FieldValueType> map = new HashMap<>();

    FieldValueType(int value) {
        this.value = value;
    }

    static {
        for (FieldValueType fieldValueType : FieldValueType.values()) {
            map.put(fieldValueType.value, fieldValueType);
        }
    }
    private static Object[] objectToArray(Object o) {
        if (o instanceof Collection) {
            //noinspection rawtypes
            return ((Collection) o).toArray();
        } else {
            return (Object[]) o;
        }
    }

    public abstract Value valueFromObject(Object o);

    /**
     * Returns the base type of a type (e.q. TEXT_ARRAY ~> TEXT, INTEGER ~> INTEGER).
     *
     * @return The base type.
     */
    public abstract FieldValueType getBaseType();

    public int getValue() {
        return this.value;
    }

    public static FieldValueType valueOf(Integer value) {
        return map.get(value);
    }
}
