/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/29
 */

package hu.arheu.gds.client;

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
