package hu.arh.gds.message.util;

import hu.arh.gds.message.data.impl.AckStatus;

public class ResponseException extends Exception {
    private final AckStatus status;

    public ResponseException(AckStatus status, String message) {
        super(message);
        if (null == status) {
            throw new NullPointerException("status");
        }
        this.status = status;
    }

    public ResponseException(AckStatus status, String message, Throwable cause) {
        super(message, cause);
        if (null == status) {
            throw new NullPointerException("status");
        }
        this.status = status;
    }

    public ResponseException(AckStatus status, Throwable cause) {
        super(cause);
        if (null == status) {
            throw new NullPointerException("status");
        }
        this.status = status;
    }

    public AckStatus getStatus() {
        return status;
    }
}
