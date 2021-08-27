/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/22
 */

package hu.arheu.gds.client;

import hu.arheu.gds.message.data.*;
import hu.arheu.gds.message.header.MessageHeaderBase;
import io.netty.channel.Channel;


/**
 * Interface used for callbacks on messages sent by the GDS.
 * <p>
 * By default, the {@link GDSMessageListener#onConnectionSuccess(Channel, MessageHeaderBase, MessageData1ConnectionAck)}
 * method will have empty body, meaning they do not need additional code if the client does not want to specify it.
 * <p>
 * The rest of the methods, however if called without an active override will throw {@link AbstractMethodError} as
 * they were not handled by the client code.
 * <p>
 * This is useful if the client only wants to send and receive specific messages (i.e. only interested in SELECT
 * queries, therefore it only overrides (implements) the {@link GDSMessageListener#onQueryRequestAck11(MessageHeaderBase, MessageData11QueryRequestAck)}
 * method.
 * <p>
 * Any other is not expected by them, meaning they do not want to give any method definitions for those.
 */
public interface GDSMessageListener {
    /**
     * Called when the underlying WebSocket (TCP) connection is successfully established and the login is ACKd by the
     * GDS with OK status.
     *
     * @param ch       the {@link Channel} object associated with the current connection
     * @param header   the MessageHeader that was given with the reply
     * @param response the ACK data of the GDS.
     */
    default void onConnectionSuccess(Channel ch, MessageHeaderBase header, MessageData1ConnectionAck response) {

    }

    /**
     * Called if for any reason the WebSocket (TCP) connection fails to set up or the login is unsuccessful.
     * If the connection setup process fails for any reason the {@link Throwable} component ({@link Either#getLeft()})
     * can be used to get why it happened.
     * If the connection was successfully established but the login process failed, the {@link Either#getRight()} will
     * contain the response from the GDS with the details.
     *
     * @param channel the {@link Channel} object associated with the current connection
     * @param reason  why the connection could not be established.
     */
    default void onConnectionFailure(Channel channel, Either<Throwable, Pair<MessageHeaderBase, MessageData1ConnectionAck>> reason) {
        throw new AbstractMethodError();
    }

    /**
     * Called when the WebSocket connection is closed (from either side).
     * This method is not called if the connection or the login process fails, only if the login was successful.
     *
     * @param channel the {@link Channel} object that was associated with the connection
     */
    default void onDisconnect(Channel channel) {

    }

    /**
     * Called upon receiving an EventACK from the GDS.
     *
     * @param header   the associated Message Header
     * @param response the response containing the result of the Event
     */
    default void onEventAck3(MessageHeaderBase header, MessageData3EventAck response) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving an AttachmentRequest from the GDS.
     *
     * @param header  the associated Message Header
     * @param request the request received
     */
    default void onAttachmentRequest4(MessageHeaderBase header, MessageData4AttachmentRequest request) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving an AttachmentRequestACK from the GDS.
     *
     * @param header     the associated Message Header
     * @param requestAck the requestACK received
     */
    default void onAttachmentRequestAck5(MessageHeaderBase header, MessageData5AttachmentRequestAck requestAck) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving an AttachmentResponse from the GDS.
     *
     * @param header   the associated Message Header
     * @param response the response received
     */
    default void onAttachmentResponse6(MessageHeaderBase header, MessageData6AttachmentResponse response) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving an AttachmentResponseACK from the GDS.
     *
     * @param header      the associated Message Header
     * @param responseAck the requestACK received
     */
    default void onAttachmentResponseAck7(MessageHeaderBase header, MessageData7AttachmentResponseAck responseAck) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving an EventDocument from the GDS.
     *
     * @param header        the associated Message Header
     * @param eventDocument the requestACK received
     */
    default void onEventDocument8(MessageHeaderBase header, MessageData8EventDocument eventDocument) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving an EventDocumentACK from the GDS.
     *
     * @param header           the associated Message Header
     * @param eventDocumentAck the requestACK received
     */
    default void onEventDocumentAck9(MessageHeaderBase header, MessageData9EventDocumentAck eventDocumentAck) {
        throw new AbstractMethodError();
    }

    /**
     * Called upon receiving a QueryRequestACK message from the GDS.
     *
     * @param header   the associated Message Header
     * @param response the GDS response containing the query result.
     */
    default void onQueryRequestAck11(MessageHeaderBase header, MessageData11QueryRequestAck response) {
        throw new AbstractMethodError();
    }
}
