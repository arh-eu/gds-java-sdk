/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/29
 */

package hu.arheu.gds.client;

@SuppressWarnings({"unused", "UnusedReturnValue"}) //API class, not all methods are used across this project.
public class GDSTimeoutException extends RuntimeException {
    public GDSTimeoutException() {
    }

    public GDSTimeoutException(String message) {
        super(message);
    }

    public GDSTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GDSTimeoutException(Throwable cause) {
        super(cause);
    }
}
