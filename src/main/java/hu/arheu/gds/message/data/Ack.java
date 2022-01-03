
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;


/**
 * Interface used to denote an ACK message from the GDS.
 * ACK messages all have a global status code ({@link Ack#getGlobalStatus()}) and an exception message ({@link Ack#getGlobalException()}).
 */
public interface Ack {

    /**
     * The status code associated with this message.
     *
     * @return the status code
     */
    AckStatus getGlobalStatus();

    /**
     * The global exception used to describe the error if any happened during the request.
     * Value contains english text about the error, helping the user. Value can be {@code null} or empty string as well
     * if no error happened.
     *
     * @return the exception in string format.
     */
    String getGlobalException();
}