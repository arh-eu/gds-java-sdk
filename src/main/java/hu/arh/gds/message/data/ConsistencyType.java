package hu.arh.gds.message.data;

import java.util.HashMap;
import java.util.Map;

public enum ConsistencyType {

    PAGE(0),
    PAGES(1),
    NONE(2);

    private int value;
    private static final Map<Integer, ConsistencyType> map = new HashMap<>();

    ConsistencyType(int value) {
        this.value = value;
    }

    static {
        for (ConsistencyType consistency : ConsistencyType.values()) {
            map.put(consistency.value, consistency);
        }
    }

    public int getValue() {
        return this.value;
    }

    public static ConsistencyType valueOf(Integer value) {
        return map.get(value);
    }
}
