package hu.arheu.gds.message.util;

import hu.arheu.gds.message.data.*;
import hu.arheu.gds.message.data.impl.*;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.header.MessageHeader;
import hu.arheu.gds.message.header.MessageHeaderBase;
import hu.arheu.gds.message.header.MessageHeaderExtended;
import hu.arheu.gds.message.header.impl.MessageHeaderBaseImpl;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Used to validate the messages that are created to/from the GDS, raising exceptions is any constraint gets violated.
 */
public class MessageManager {
    public static final int DATA_FIELD_COUNT = 1;

    private static byte[] packMessageWrapper(int arraySize) throws IOException {

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(arraySize);
        return packer.toByteArray();
    }

    /**
     * Packs the given headerand data to a full message by MessagePack, returning the raw bytes from the created message.
     *
     * @param header The headerof the message.
     * @param data   The data (content) of the message.
     * @return the binary array containing the message packed by {@code MessagePack}
     * @throws IOException         if any of the headerfields contain illegal value(type)s
     * @throws ValidationException if the contents of the headerviolate the class invariant (ie. if {@code is_fragmented}
     *                             is given, {@code first_fragment} cannot be {@code null} and so.)
     */
    public static byte[] createMessage(MessageHeader header, MessageData data) throws IOException, ValidationException {

        Validator.requireNonNullValue(header, MessageManager.class.getSimpleName(), "header");
        Validator.requireNonNullValue(data, MessageManager.class.getSimpleName(), "data");

        ByteArrayOutputStream binary = new ByteArrayOutputStream();
        switch (header.getMessageHeaderType()) {
            case BASE:
                binary.write(packMessageWrapper(
                        (MessageHeaderBase.NUMBER_OF_FIELDS + DATA_FIELD_COUNT)));
                break;
            case EXTENDED:
                binary.write(packMessageWrapper(
                        (MessageHeaderBase.NUMBER_OF_FIELDS + MessageHeaderExtended.NUMBER_OF_FIELDS + DATA_FIELD_COUNT)));
                break;
            default:
                throw new ValidationException(String.format("%s: Unknown message headertype (%s)",
                        MessageManager.class.getSimpleName(),
                        header.getMessageHeaderType()));
        }

        binary.write(header.getBinary());
        binary.write(data.getBinary());

        return binary.toByteArray();
    }

    /**
     * Returns the data type from the binary message, parsing it as a hu.arheu.gds.message.header
     *
     * @param binary the binary content
     * @return the data type for the binary message
     * @throws ReadException       if IO exception occurs
     * @throws ValidationException if the headeris invalid
     */
    public static MessageDataType getMessageDataType(byte[] binary) throws ReadException, ValidationException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(binary)) {
            MessageHeaderBaseImpl header = new MessageHeaderBaseImpl();
            ReaderHelper.unpackArrayHeader(unpacker);
            header.unpackContentFrom(unpacker);
            return header.getDataType();
        } catch (IOException e) {
            throw new ReadException(e);
        }
    }


    /**
     * Creates a headerfor a message based on the username, and data type.
     * The message ID will be randomly generated.
     * Throws exception if any value(type) is illegal or if the validating fails.
     *
     * @param userName the name of the user
     * @param dataType the type of the message body
     * @return the created {@link MessageHeaderBase} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     *                             (ie. {@code userName} cannot be null)
     */
    public static MessageHeaderBase createMessageHeaderBase(
            String userName,
            MessageDataType dataType) throws ValidationException {

        return new MessageHeaderBaseImpl(
                userName,
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                false,
                null,
                null,
                null,
                null,
                dataType);
    }

    /**
     * Creates a headerfor a message based on the username, messageID and data type.
     * Throws exception if any value(type) is illegal or if the validating fails.
     *
     * @param userName  the name of the user
     * @param messageId the messageID used to identify the message
     * @param dataType  the type of the message body
     * @return the created {@link MessageHeaderBase} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     *                             (ie. {@code userName} cannot be null)
     */
    public static MessageHeaderBase createMessageHeaderBase(
            String userName,
            String messageId,
            MessageDataType dataType) throws ValidationException {

        return new MessageHeaderBaseImpl(
                userName,
                messageId,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                false,
                null,
                null,
                null,
                null,
                dataType);
    }

    /**
     * Creates a message hu.arheu.gds.message.header, based on the field values given.
     * Validates the format (types and constraints) and throws exception on any error found.
     *
     * @param userName      the name of the user
     * @param messageId     the messageID used to identify the message
     * @param createTime    the time when the message was created.
     * @param requestTime   the time when the request was sent
     * @param isFragmented  whether the message is fragmented or not.
     * @param firstFragment indicates if this is the first fragment of a fragmented message. If {@code isFragmented} is false, should be {@code null}.
     * @param lastFragment  indicates if this is the last fragment of a fragmented message. If {@code isFragmented} is false, should be {@code null}.
     * @param offset        indicates how many bytes were successfully received so far. If {@code isFragmented} is false, should be {@code null}.
     * @param fullDataSize  indicates the full data size in a  fragmented message. If {@code isFragmented} is false, should be {@code null}.
     * @param dataType      the type of the message body
     * @return the created {@link MessageHeaderBase} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     *                             (ie. {@code userName} cannot be null)
     */

    public static MessageHeaderBase createMessageHeaderBase(
            String userName,
            String messageId,
            Long createTime,
            Long requestTime,
            Boolean isFragmented,
            Boolean firstFragment,
            Boolean lastFragment,
            Long offset,
            Long fullDataSize,
            MessageDataType dataType) throws ValidationException {

        return new MessageHeaderBaseImpl(
                userName,
                messageId,
                createTime,
                requestTime,
                isFragmented,
                firstFragment,
                lastFragment,
                offset,
                fullDataSize,
                dataType);
    }

    /**
     * Creates a message hu.arheu.gds.message.header, based on the field values given.
     * Validates the format (types and constraints) and throws exception on any error found.
     *
     * @param userName      the name of the user
     * @param messageId     the messageID used to identify the message
     * @param isFragmented  whether the message is fragmented or not.
     * @param firstFragment indicates if this is the first fragment of a fragmented message. If {@code isFragmented} is false, should be {@code null}.
     * @param lastFragment  indicates if this is the last fragment of a fragmented message. If {@code isFragmented} is false, should be {@code null}.
     * @param offset        indicates how many bytes were successfully received so far. If {@code isFragmented} is false, should be {@code null}.
     * @param fullDataSize  indicates the full data size in a  fragmented message. If {@code isFragmented} is false, should be {@code null}.
     * @param dataType      the type of the message body
     * @return the created {@link MessageHeaderBase} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     *                             (ie. {@code userName} cannot be null)
     */
    public static MessageHeaderBase createMessageHeaderBase(
            String userName,
            String messageId,
            Boolean isFragmented,
            Boolean firstFragment,
            Boolean lastFragment,
            Long offset,
            Long fullDataSize,
            MessageDataType dataType) throws ValidationException {

        return new MessageHeaderBaseImpl(
                userName,
                messageId,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                isFragmented,
                firstFragment,
                lastFragment,
                offset,
                fullDataSize,
                dataType);
    }

    /**
     * Creates a connection message with the given values.
     * Validates the contents, throwing any exception on failure
     *
     * @param serveOnTheSameConnection Whether the replies for the requests should be sent on this connection
     * @param protocolVersionNumber    The protocol used for the communication. The value is not yet checked.
     * @param fragmentationSupported   Whether the client supports fragmentation on the messages
     * @param fragmentTransmissionUnit If {@code fragmentationSupported} is true, indicates the maximal chunk size of
     *                                 the fragmented messages the client can accept.
     * @return The created {@link MessageData0Connection} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit) throws ValidationException {

        return new MessageData0ConnectionImpl(
                serveOnTheSameConnection,
                null,
                protocolVersionNumber,
                fragmentationSupported,
                fragmentTransmissionUnit,
                null);
    }

    /**
     * Creates a connection message with the given values.
     * Validates the contents, throwing any exception on failure
     *
     * @param serveOnTheSameConnection Whether the replies for the requests should be sent on this connection
     * @param protocolVersionNumber    The protocol used for the communication. The value is not yet checked.
     * @param fragmentationSupported   Whether the client supports fragmentation on the messages
     * @param fragmentTransmissionUnit If {@code fragmentationSupported} is true, indicates the maximal chunk size of
     *                                 the fragmented messages the client can accept.
     * @param password                 The password used for password authentication.
     * @return The created {@link MessageData0Connection} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit,
            String password) throws ValidationException {

        return createMessageData0Connection(
                serveOnTheSameConnection,
                null,
                protocolVersionNumber,
                fragmentationSupported,
                fragmentTransmissionUnit,
                password);
    }

    /**
     * Creates a connection message with the given values.
     * Validates the contents, throwing any exception on failure
     *
     * @param serveOnTheSameConnection Whether the replies for the requests should be sent on this connection
     * @param clusterName              The name of the cluster the GDS belongs to.
     * @param protocolVersionNumber    The protocol used for the communication. The value is not yet checked.
     * @param fragmentationSupported   Whether the client supports fragmentation on the messages
     * @param fragmentTransmissionUnit If {@code fragmentationSupported} is true, indicates the maximal chunk size of
     *                                 the fragmented messages the client can accept.
     * @param password                 The password used for password authentication.
     * @return The created {@link MessageData0Connection} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            String clusterName,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit,
            String password) throws ValidationException {

        return new MessageData0ConnectionImpl(
                serveOnTheSameConnection,
                clusterName,
                protocolVersionNumber,
                fragmentationSupported,
                fragmentTransmissionUnit,
                password);
    }

    /**
     * Creates a connection message with the given values.
     * Validates the contents, throwing any exception on failure
     *
     * @param serveOnTheSameConnection Whether the replies for the requests should be sent on this connection
     * @param clusterName              The name of the cluster the GDS belongs to.
     * @param protocolVersionNumber    The protocol used for the communication. The value is not yet checked.
     * @param fragmentationSupported   Whether the client supports fragmentation on the messages
     * @param fragmentTransmissionUnit If {@code fragmentationSupported} is true, indicates the maximal chunk size of
     *                                 the fragmented messages the client can accept.
     * @return The created {@link MessageData0Connection} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            String clusterName,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit) throws ValidationException {

        return new MessageData0ConnectionImpl(
                serveOnTheSameConnection,
                clusterName,
                protocolVersionNumber,
                fragmentationSupported,
                fragmentTransmissionUnit,
                null);
    }

    /**
     * Creates a connection ACK message. This should be only used if the client is a GDS instance that can be connected to.
     *
     * @param ackDataOk                the data containing the ACK values if the login is successful.
     * @param ackDataUnauthorizedItems the map containing the illegal values if the login is unsuccessful
     * @param globalStatus             the global status code for the ACK message.
     * @param globalException          the global exception message (in plain, english text) if any hu.arheu.gds.message.errors happened.
     * @return The created {@link MessageData1ConnectionAck} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData1ConnectionAck createMessageData1ConnectionAck(
            MessageData0Connection ackDataOk,
            Map<Integer, String> ackDataUnauthorizedItems,
            AckStatus globalStatus,
            String globalException) throws ValidationException {

        return new MessageData1ConnectionAckImpl(
                ackDataOk,
                ackDataUnauthorizedItems,
                globalStatus,
                globalException);
    }

    /**
     * Creates an event message, raising any exception on invalid values.
     * The Event string should refer to attachments used in them with the {@code '0x'} prefix in hex format.
     * Keys in the {@code binaryContents} should be the hex IDs used in the operations without the {@code '0x'} prefix.
     *
     * @param operations     The list of strings containing the event operations.
     * @param binaryContents The attachments sent along with the message.
     * @param priorityLevels The priority levels
     * @return The created {@link MessageData2Event} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData2Event createMessageData2Event(
            List<String> operations,
            Map<String, byte[]> binaryContents,
            List<PriorityLevelHolder> priorityLevels) throws ValidationException {

        return new MessageData2EventImpl(
                operations,
                binaryContents,
                priorityLevels);
    }


    /**
     * Creates an event message, raising any exception on invalid values.
     * The Event string should refer to attachments used in them with the {@code '0x'} prefix in hex format.
     * Keys in the {@code binaryContents} should be the hex IDs used in the operations without the {@code '0x'} prefix.
     *
     * @param operations     The string of the event operations.
     * @param binaryContents The attachments sent along with the message.
     * @param priorityLevels The priority levels
     * @return The created {@link MessageData2Event} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData2Event createMessageData2Event(
            String operations,
            Map<String, byte[]> binaryContents,
            List<PriorityLevelHolder> priorityLevels) throws ValidationException {

        return new MessageData2EventImpl(
                operations,
                binaryContents,
                priorityLevels);
    }

    /**
     * Creates an ACK with the result of a previous event message. Validates and raises error on any rule violation.
     * The app. only should send this if it is a GDS instance, as client code never receives events that should be ACKd.
     *
     * @param eventResults    the list of the result holders
     * @param globalStatus    the global code of the ACK
     * @param globalException the exception (as english text) if any hu.arheu.gds.message.errors were with the events.
     * @return The created {@link MessageData3EventAck} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData3EventAck createMessageData3EventAck(
            List<EventResultHolder> eventResults,
            AckStatus globalStatus,
            String globalException) throws ValidationException {

        return new MessageData3EventAckImpl(
                eventResults,
                globalStatus,
                globalException);
    }

    /**
     * Creates an attachment request message, raising any exception on invalid values.
     * The request should contain the ID of the attachment in the WHERE condition otherwise the GDS will response
     * with an error message.
     *
     * @param request the String containing the request ID.
     * @return The created {@link MessageData4AttachmentRequest} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData4AttachmentRequest createMessageData4AttachmentRequest(
            String request) throws ValidationException {

        return new MessageData4AttachmentRequestImpl(
                request);
    }

    /**
     * Creates an attachment request ACK message, raising any exception on invalid values.
     *
     * @param globalStatus    the ACK status for the message
     * @param data            the data containing the ACK message, if no error happened.
     * @param globalException the String containing any error messages, if something went wrong.
     * @return The created {@link MessageData5AttachmentRequestAck} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData5AttachmentRequestAck createMessageData5AttachmentRequestAck(
            AckStatus globalStatus,
            AttachmentRequestAckDataHolder data,
            String globalException) throws ValidationException {

        return new MessageData5AttachmentRequestAckImpl(
                globalStatus,
                data,
                globalException);
    }

    /**
     * @param result      the result holder containing the attachment
     * @param eventHolder the event holder of the message
     * @return The created {@link MessageData6AttachmentResponse} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData6AttachmentResponse createMessageData6AttachmentResponse(
            AttachmentResultHolder result,
            EventHolder eventHolder
    ) throws ValidationException {

        return new MessageData6AttachmentResponseImpl(
                result,
                eventHolder);
    }

    /**
     * @param globalStatus    the Status code for the request
     * @param data            the data for the attachment response
     * @param globalException the error in string format (if any)
     * @return The created {@link MessageData7AttachmentResponseAck} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData7AttachmentResponseAck createMessageData7AttachmentResponseAck(
            AckStatus globalStatus,
            AttachmentResponseAckResultHolder data,
            String globalException) throws ValidationException {

        return new MessageData7AttachmentResponseAckImpl(
                globalStatus,
                data,
                globalException);
    }

    /**
     * @param tableName    the table name
     * @param fieldHolders the field holder values
     * @param records      the records
     * @return The created {@link MessageData9EventDocumentAck} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData8EventDocument createMessageData8EventDocument(
            String tableName,
            List<FieldHolder> fieldHolders,
            List<List<Value>> records) throws ValidationException {
        return createMessageData8EventDocument(tableName, fieldHolders, records, new HashMap<>(0));
    }

    /**
     * @param tableName        the table name
     * @param fieldHolders     the field holder values
     * @param records          the records
     * @param returningOptions the returning fields
     * @return The created {@link MessageData9EventDocumentAck} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData8EventDocument createMessageData8EventDocument(
            String tableName,
            List<FieldHolder> fieldHolders,
            List<List<Value>> records,
            Map<Integer, List<String>> returningOptions) throws ValidationException {

        return new MessageData8EventDocumentImpl(
                tableName,
                fieldHolders,
                records,
                returningOptions);
    }

    /**
     * @param globalStatus    the Status code for the request
     * @param result          the result of the event document request
     * @param globalException the error in string format (if any)
     * @return The created {@link MessageData9EventDocumentAck} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData9EventDocumentAck createMessageData9EventDocumentAck(
            AckStatus globalStatus,
            List<EventDocumentResultHolder> result,
            String globalException) throws ValidationException {

        return new MessageData9EventDocumentAckImpl(
                globalStatus,
                result,
                globalException);
    }

    /**
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @return The created {@link MessageData10QueryRequest} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData10QueryRequest createMessageData10QueryRequest(
            String query,
            ConsistencyType consistencyType,
            Long timeout) throws ValidationException {

        return new MessageData10QueryRequestImpl(
                query,
                consistencyType,
                timeout);
    }

    /**
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @param pageSize        the page size used for the query
     * @param queryType       the type of the query (scroll/page)
     * @return The created {@link MessageData10QueryRequest} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData10QueryRequest createMessageData10QueryRequest(
            String query,
            ConsistencyType consistencyType,
            Long timeout,
            Integer pageSize,
            Integer queryType) throws ValidationException {

        return new MessageData10QueryRequestImpl(
                query,
                consistencyType,
                timeout,
                pageSize,
                queryType);
    }


    /**
     * Creates a Query ACK message. This should be only used if the client is a GDS instance that can be connected to.
     *
     * @param globalStatus    the global status code for the ACK message.
     * @param queryResponse   the response data for the query
     * @param globalException the global exception message (in plain, english text) if any hu.arheu.gds.message.errors happened.
     * @return The created {@link MessageData11QueryRequestAck} instance
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData11QueryRequestAck createMessageData11QueryRequestAck(
            AckStatus globalStatus,
            QueryResponseHolder queryResponse,
            String globalException) throws ValidationException {

        return new MessageData11QueryRequestAckImpl(
                globalStatus,
                queryResponse,
                globalException);
    }

    /**
     * @param queryContextHolder the ContextHolder containing information about the current query status
     * @param timeout            the timeout used in the GDS for the query
     * @return The created {@link MessageData12NextQueryPage} instance
     * @throws ValidationException if the contents of the headerviolate the class invariant
     */
    public static MessageData12NextQueryPage createMessageData12NextQueryPage(
            QueryContextHolder queryContextHolder,
            Long timeout) throws ValidationException {

        return new MessageData12NextQueryPageImpl(queryContextHolder, timeout);
    }
}
