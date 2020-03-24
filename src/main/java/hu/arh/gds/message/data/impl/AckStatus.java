package hu.arh.gds.message.data.impl;

import java.util.HashMap;
import java.util.Map;

public enum AckStatus {
    OK(200, false),
    CREATED(201, false),
    ACCEPTED(202, false),
    NOT_ACCEPTABLE_304(304, true),
    BAD_REQUEST(400, true),
    UNAUTHORIZED(401, true),
    FORBIDDEN(403, true),
    NOT_FOUND(404, true),
    NOT_ACCEPTABLE_406(406, true),
    REQUEST_TIMEOUT(408, true),
    CONFLICT(409, true),
    PRECONDITION_FAILED(412, true),
    TOO_MANY_REQUESTS(429, true),
    INTERNAL_SERVER_ERROR(500, true),
    LIMIT_EXCEEDED(509, true),
    NOT_EXTENDED(510, true);

    private final int value;
    private final boolean errorStatus;
    private static final Map<Integer, AckStatus> map = new HashMap<>();

    private AckStatus(int value, boolean errorStatus) {
        this.value = value;
        this.errorStatus = errorStatus;
    }

    static {
        for (AckStatus ackStatus : AckStatus.values()) {
            map.put(ackStatus.value, ackStatus);
        }
    }

    public int getValue() {
        return this.value;
    }

    public boolean isErrorStatus() { return errorStatus; }

    public static AckStatus valueOf(Integer value) {
        return map.get(value);
    }
}
