package hu.arheu.gds.message.errors;

/**
 * If any of the messages that got created or unpacked from the buffers are invalid,
 * this exception will be thrown to avoid and prohibit illegal messages.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
