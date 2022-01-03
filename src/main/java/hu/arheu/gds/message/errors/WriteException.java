
package hu.arheu.gds.message.errors;


import java.io.IOException;

/**
 * Any IO exception that occurs during object serialization to any kind of binary format or stream
 * is wrapped into this exception.
 * This includes pure Java hu.arheu.gds.message.errors as well as MessagePack exceptions.
 */
public class WriteException extends IOException {

    public WriteException(String message) {
        super(message);
    }

    public WriteException(Exception exc) {
        super(exc);
    }

    public WriteException(String message, Exception exc) {
        super(message, exc);
    }


}
