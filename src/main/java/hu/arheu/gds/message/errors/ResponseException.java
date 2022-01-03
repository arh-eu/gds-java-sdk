package hu.arheu.gds.message.errors;

import hu.arheu.gds.message.data.impl.AckStatus;

import java.util.Objects;

public class ResponseException extends Exception {
    private final AckStatus status;

    public ResponseException(AckStatus status, String message) {
        super(message);
        Objects.requireNonNull(status, "AckStatus cannot be null!");
        this.status = status;
    }

    public ResponseException(AckStatus status, Throwable cause) {
        super(cause);
        Objects.requireNonNull(status, "AckStatus cannot be null!");
        this.status = status;
    }

    public ResponseException(AckStatus status, String message, Throwable cause) {
        super(message, cause);
        Objects.requireNonNull(status, "AckStatus cannot be null!");
        this.status = status;
    }

    public AckStatus getStatus() {
        return status;
    }
}
