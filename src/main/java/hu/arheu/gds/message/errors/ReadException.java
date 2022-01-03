
package hu.arheu.gds.message.errors;


import java.io.IOException;

/**
 * Any IO exception that occurs during object deserialization from any kind of binary format or stream
 * is wrapped into this exception.
 * This includes pure Java hu.arheu.gds.message.errors as well as MessagePack exceptions.
 */
public class ReadException extends IOException {

    public ReadException(String message) {
        super(message);
    }

    public ReadException(Exception exc) {
        super(exc);
    }

    public ReadException(String message, Exception exc) {
        super(message, exc);
    }
}
