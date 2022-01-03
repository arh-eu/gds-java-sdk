
package hu.arheu.gds.message.data;

import java.util.HashMap;
import java.util.Map;


public enum ConsistencyType {
    /**
     * Absolute consistency (one page consistency). Returns the result specified by the condition,
     * but the result set must fit into one page. This type runs on a consistent snapshot of the internal store.
     * Therefore if the result cannot fit into one page it raises an error as the consistency between queries
     * cannot be guaranteed (records might be modified).
     */
    PAGE(0),
    /**
     * Consistent result over time (through pages). Only records updated before the start of the query will be returned,
     * leading to a partially consistent result. If records are modified between queries, they will not be present,
     * but duplicates will not be returned.
     */
    PAGES(1),
    /**
     * Non-consistent query, without any restrictions. Since records can be updated between query requests,
     * duplicate records (or records skipped) may occur between query pages.
     */
    NONE(2);

    private final int value;
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
