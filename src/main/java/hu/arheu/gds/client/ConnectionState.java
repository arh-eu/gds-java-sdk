/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/22
 */

package hu.arheu.gds.client;

/**
 * Enum describing the possible states of the GDS client.
 */
public enum ConnectionState {
    /**
     * The client is instantiated but {@link AsyncGDSClient#connect()} was not yet called.
     */
    NOT_CONNECTED,

    /**
     * The {@link AsyncGDSClient#connect()} method was called on the client, the underlying Netty channels are being created
     */
    INITIALIZING,

    /**
     * Netty got successfully initialized, trying to establish the TCP/WebSocket Connection
     */
    CONNECTING,

    /**
     * The WebSocket connection (and TLS) is successfully established
     */
    CONNECTED,

    /**
     * The login message was successfully sent to the GDS
     */
    LOGGING_IN,

    /**
     * The login was successful. Client is ready to use
     */
    LOGGED_IN,

    /**
     * The connection was closed after a successful login (from either the client or the GDS side).
     */
    DISCONNECTED,

    /**
     * Error happened during the initialization of the client.
     */
    FAILED
}
