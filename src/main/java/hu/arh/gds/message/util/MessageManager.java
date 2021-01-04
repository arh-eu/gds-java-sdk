/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.util;

import hu.arh.gds.message.data.*;
import hu.arh.gds.message.data.impl.*;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.header.MessageHeaderBase;
import hu.arh.gds.message.header.MessageHeaderType;
import hu.arh.gds.message.header.impl.MessageHeaderBaseImpl;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Used to validate the messages that are created to/from the GDS, raising exceptions is any constraint gets violated.
 *
 * @author oliver.nagy
 */
public class MessageManager {

    private static byte[] packMessageWrapper(int arraySize) throws IOException {

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(arraySize);
        return packer.toByteArray();
    }

    private static int getMessageArrayHeaderSize(byte[] message) throws ReadException {

        ExceptionHelper.requireNonNullValue(message, MessageManager.class.getSimpleName(), "message");

        String prefix = String.format(
                "%8s", Integer.toBinaryString(message[0] & 0xFF)).replace(' ', '0');

        int arrayHeaderSize;
        if (prefix.substring(0, 4).equals(Globals.fixarray)) { //4 bit
            arrayHeaderSize = Integer.parseInt(prefix.substring(4, 8), 2);
        } else if (prefix.equals(Globals.array16)) { //16 bit
            arrayHeaderSize = new BigInteger(Arrays.copyOfRange(message, 1, 3)).intValue();
        } else if (prefix.equals(Globals.array32)) { //32 bit
            arrayHeaderSize = new BigInteger(Arrays.copyOfRange(message, 1, 5)).intValue();
        } else {
            throw new ReadException(
                    String.format("%s: MessagePack prefix not valid (%s)",
                            MessageManager.class.getSimpleName(),
                            prefix));
        }

        return arrayHeaderSize;
    }

    /**
     * Checks the format of a binary message, validating its header.
     * Header is considered valid if the message can be parsed as an array containing exactly 11 elements.
     *
     * @param message the binary message
     * @return {@link MessageHeaderType#BASE} on successful read
     * @throws ReadException If the message format is invalid or the array contains more or less than 11 elements.
     */
    public static MessageHeaderType getMessageHeaderType(byte[] message) throws ReadException {

        ExceptionHelper.requireNonNullValue(message, MessageManager.class.getSimpleName(), "message");

        int arrayHeaderSize = getMessageArrayHeaderSize(message);
        switch (arrayHeaderSize) {
            case (Globals.BASE_HEADER_FIELDS_NUMBER + Globals.DATA_FIELDS_NUMBER):
                return MessageHeaderType.BASE;
            default:
                throw new ReadException(String.format("%s: Array header size (%s) does not match expected value (%s)." +
                                " Array name: %s.",
                        MessageHeader.class.getSimpleName(),
                        arrayHeaderSize,
                        Globals.BASE_HEADER_FIELDS_NUMBER +
                                Globals.DATA_FIELDS_NUMBER +
                                "or" +
                                Globals.BASE_HEADER_FIELDS_NUMBER +
                                Globals.EXTRA_HEADER_FIELDS_NUMBER +
                                Globals.DATA_FIELDS_NUMBER,
                        "Message Header"));
        }
    }

    /**
     * Checks the contents of the binary message, returning the type specified in the header indicating the type of the
     * data part.
     *
     * @param message the binary array containing the message
     * @return the {@link MessageDataType} value indicating the type of the message data
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ReadException       if the message format is invalid (not an array) or it does not contain 11 elements
     * @throws ValidationException if the contents of the header violate the class invariant (ie. if {@code is_fragmented}
     *                             is given, {@code first_fragment} cannot be {@code null} and so.)
     */
    public static MessageDataType getMessageDataType(byte[] message) throws ReadException, IOException, ValidationException {

        ExceptionHelper.requireNonNullValue(message, MessageManager.class.getSimpleName(), "message");

        MessageHeader header = getMessageHeaderFromBinaryMessage(message);
        switch (header.getTypeHelper().getMessageHeaderType()) {
            case BASE:
                return header.getTypeHelper().asBaseMessageHeader().getDataType();
            default:
                throw new ReadException(String.format("%s: Unknown message header type (%s)",
                        MessageManager.class.getSimpleName(),
                        header.getTypeHelper().getMessageHeaderType()));
        }
    }

    /**
     * Packs the given header and data to a full message by MessagePack, returning the raw bytes from the created message.
     *
     * @param header The header of the message.
     * @param data   The data (content) of the message.
     * @return the binary array containing the message packed by {@code MessagePack}
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant (ie. if {@code is_fragmented}
     *                             is given, {@code first_fragment} cannot be {@code null} and so.)
     */
    public static byte[] createMessage(MessageHeader header, MessageData data) throws IOException, ValidationException {

        ExceptionHelper.requireNonNullValue(header, MessageManager.class.getSimpleName(), "header");
        ExceptionHelper.requireNonNullValue(data, MessageManager.class.getSimpleName(), "data");

        ByteArrayOutputStream binary = new ByteArrayOutputStream();
        switch (header.getTypeHelper().getMessageHeaderType()) {
            case BASE:
                binary.write(packMessageWrapper(
                        Globals.BASE_HEADER_FIELDS_NUMBER +
                                Globals.DATA_FIELDS_NUMBER));
                break;
            default:
                throw new ValidationException(String.format("%s: Unknown message header type (%s)",
                        MessageManager.class.getSimpleName(),
                        header.getTypeHelper().getMessageHeaderType()));
        }

        binary.write(header.getBinary());
        binary.write(data.getBinary());

        return binary.toByteArray();
    }

    /**
     * Returns the header part of a message given in binary format.
     * Validates the contents, raising any exception on invalid message formats.
     *
     * @param message the binary message
     * @return {@link MessageHeaderBase} containing the header information
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ReadException       if the message format is invalid (not an array) or it does not contain 11 elements
     * @throws ValidationException if the contents of the header violate the class invariant (ie. if {@code is_fragmented}
     *                             is given, {@code first_fragment} cannot be {@code null} and so.)
     */
    public static MessageHeader getMessageHeaderFromBinaryMessage(byte[] message) throws IOException, ReadException, ValidationException {

        ExceptionHelper.requireNonNullValue(message, MessageManager.class.getSimpleName(), "message");

        MessageHeaderType headerType = getMessageHeaderType(message);
        switch (headerType) {
            case BASE:
                return new MessageHeaderBaseImpl(message, true);
            default:
                throw new ReadException(String.format("%s: Unknown message header type (%s)",
                        MessageManager.class.getSimpleName(),
                        headerType));
        }
    }

    /**
     * Returns the header part of a message given in binary format.
     * Validates the contents, raising any exception on invalid message formats.
     *
     * @param message the binary message
     * @return {@link MessageData} containing the header information
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ReadException       if the message format is invalid (not an array) or it does not contain 11 elements
     * @throws ValidationException if the contents of the header violate the class invariant (ie. if {@code is_fragmented}
     *                             is given, {@code first_fragment} cannot be {@code null} and so.)
     */
    public static MessageData getMessageData(byte[] message) throws IOException, ReadException, ValidationException {

        ExceptionHelper.requireNonNullValue(message, MessageManager.class.getSimpleName(), "message");

        MessageDataType dataType = getMessageDataType(message);
        switch (dataType) {
            case CONNECTION_0:
                return new MessageData0ConnectionImpl(message, false);
            case CONNECTION_ACK_1:
                return new MessageData1ConnectionAckImpl(message, false);
            case EVENT_2:
                return new MessageData2EventImpl(message, false);
            case EVENT_ACK_3:
                return new MessageData3EventAckImpl(message, false);
            case ATTACHMENT_REQUEST_4:
                return new MessageData4AttachmentRequestImpl(message, false);
            case ATTACHMENT_REQUEST_ACK_5:
                return new MessageData5AttachmentRequestAckImpl(message, false);
            case ATTACHMENT_RESPONSE_6:
                return new MessageData6AttachmentResponseImpl(message, false);
            case ATTACHMENT_RESPONSE_ACK_7:
                return new MessageData7AttachmentResponseAckImpl(message, false);
            case EVENT_DOCUMENT_8:
                return new MessageData8EventDocumentImpl(message, false);
            case EVENT_DOCUMENT_ACK_9:
                return new MessageData9EventDocumentAckImpl(message, false);
            case QUERY_REQUEST_10:
                return new MessageData10QueryRequestImpl(message, false);
            case QUERY_REQUEST_ACK_11:
                return new MessageData11QueryRequestAckImpl(message, false);
            case NEXT_QUERY_PAGE_12:
                return new MessageData12NextQueryPageImpl(message, false);
            default:
                throw new ReadException(String.format("%s: Unknown message data type (%s)",
                        MessageManager.class.getSimpleName(),
                        dataType));
        }
    }

    /**
     * Creates a header for a message based on the username, and data type.
     * The message ID will be randomly generated.
     * Throws exception if any value(type) is illegal or if the validating fails.
     *
     * @param userName the name of the user
     * @param dataType the type of the message body
     * @return the created {@link MessageHeaderBase} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     *                             (ie. {@code userName} cannot be null)
     */
    public static MessageHeaderBase createMessageHeaderBase(
            String userName,
            MessageDataType dataType) throws IOException, ValidationException {

        return new MessageHeaderBaseImpl(true,
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
     * Creates a header for a message based on the username, messageID and data type.
     * Throws exception if any value(type) is illegal or if the validating fails.
     *
     * @param userName  the name of the user
     * @param messageId the messageID used to identify the message
     * @param dataType  the type of the message body
     * @return the created {@link MessageHeaderBase} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     *                             (ie. {@code userName} cannot be null)
     */
    public static MessageHeaderBase createMessageHeaderBase(
            String userName,
            String messageId,
            MessageDataType dataType) throws IOException, ValidationException {

        return new MessageHeaderBaseImpl(true,
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
     * Creates a message header, based on the field values given.
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
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
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
            MessageDataType dataType) throws IOException, ValidationException {

        return new MessageHeaderBaseImpl(true,
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
     * Creates a message header, based on the field values given.
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
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
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
            MessageDataType dataType) throws IOException, ValidationException {

        return new MessageHeaderBaseImpl(true,
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
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit) throws IOException, ValidationException {

        return new MessageData0ConnectionImpl(false,
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
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit,
            String password) throws IOException, ValidationException {

        return new MessageData0ConnectionImpl(false,
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
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData0Connection createMessageData0Connection(
            Boolean serveOnTheSameConnection,
            String clusterName,
            Integer protocolVersionNumber,
            Boolean fragmentationSupported,
            Long fragmentTransmissionUnit,
            String password) throws IOException, ValidationException {

        return new MessageData0ConnectionImpl(false,
                serveOnTheSameConnection,
                clusterName,
                protocolVersionNumber,
                fragmentationSupported,
                fragmentTransmissionUnit,
                password);
    }

    /**
     * Creates a connection ACK message. This should be only used if the client is a GDS instance that can be connected to.
     *
     * @param ackDataOk                the data containing the ACK values if the login is successful.
     * @param ackDataUnauthorizedItems the map containing the illegal values if the login is unsuccessful
     * @param globalStatus             the global status code for the ACK message.
     * @param globalException          the global exception message (in plain, english text) if any errors happened.
     * @return The created {@link MessageData1ConnectionAck} instance
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData1ConnectionAck createMessageData1ConnectionAck(
            MessageData0ConnectionDescriptor ackDataOk,
            Map<Integer, String> ackDataUnauthorizedItems,
            AckStatus globalStatus,
            String globalException) throws IOException, ValidationException {

        return new MessageData1ConnectionAckImpl(false,
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
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData2Event createMessageData2Event(
            List<String> operations,
            Map<String, byte[]> binaryContents,
            List<PriorityLevelHolder> priorityLevels) throws IOException, ValidationException {

        return new MessageData2EventImpl(false,
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
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData2Event createMessageData2Event(
            String operations,
            Map<String, byte[]> binaryContents,
            List<PriorityLevelHolder> priorityLevels) throws IOException, ValidationException {

        return new MessageData2EventImpl(false,
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
     * @param globalException the exception (as english text) if any errors were with the events.
     * @return The created {@link MessageData3EventAck} instance
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData3EventAck createMessageData3EventAck(
            List<EventResultHolder> eventResults,
            AckStatus globalStatus,
            String globalException) throws IOException, ValidationException {

        return new MessageData3EventAckImpl(false,
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
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData4AttachmentRequest createMessageData4AttachmentRequest(
            String request) throws IOException, ValidationException {

        return new MessageData4AttachmentRequestImpl(false,
                request);
    }

    /**
     * Creates an attachment request ACK message, raising any exception on invalid values.
     *
     * @param globalStatus    the ACK status for the message
     * @param data            the data containing the ACK message, if no error happened.
     * @param globalException the String containing any error messages, if something went wrong.
     * @return The created {@link MessageData5AttachmentRequestAck} instance
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData5AttachmentRequestAck createMessageData5AttachmentRequestAck(
            AckStatus globalStatus,
            AttachmentRequestAckDataHolder data,
            String globalException) throws IOException, ValidationException {

        return new MessageData5AttachmentRequestAckImpl(false,
                globalStatus,
                data,
                globalException);
    }

    /**
     * @param result      the result holder containing the attachment
     * @param eventHolder the event holder of the message
     * @return The created {@link MessageData6AttachmentResponse} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData6AttachmentResponse createMessageData6AttachmentResponse(
            AttachmentResultHolder result,
            EventHolder eventHolder
    ) throws IOException, ValidationException {

        return new MessageData6AttachmentResponseImpl(false,
                result,
                eventHolder);
    }

    /**
     * @param globalStatus    the Status code for the request
     * @param data            the data for the attachment response
     * @param globalException the error in string format (if any)
     * @return The created {@link MessageData7AttachmentResponseAck} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData7AttachmentResponseAck createMessageData7AttachmentResponseAck(
            AckStatus globalStatus,
            AttachmentResponseAckResultHolder data,
            String globalException) throws IOException, ValidationException {

        return new MessageData7AttachmentResponseAckImpl(false,
                globalStatus,
                data,
                globalException);
    }

    /**
     * @param tableName    the table name
     * @param fieldHolders the field holder values
     * @param records      the records
     * @return The created {@link MessageData9EventDocumentAck} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData8EventDocument createMessageData8EventDocument(
            String tableName,
            List<FieldHolder> fieldHolders,
            List<List<Value>> records) throws IOException, ValidationException {
        return createMessageData8EventDocument(tableName, fieldHolders, records, new HashMap<>(0));
    }

    /**
     * @param tableName        the table name
     * @param fieldHolders     the field holder values
     * @param records          the records
     * @param returningOptions the returning fields
     * @return The created {@link MessageData9EventDocumentAck} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData8EventDocument createMessageData8EventDocument(
            String tableName,
            List<FieldHolder> fieldHolders,
            List<List<Value>> records,
            Map<Integer, List<String>> returningOptions) throws IOException, ValidationException {

        return new MessageData8EventDocumentImpl(false,
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
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData9EventDocumentAck createMessageData9EventDocumentAck(
            AckStatus globalStatus,
            List<EventDocumentResultHolder> result,
            String globalException) throws IOException, ValidationException {

        return new MessageData9EventDocumentAckImpl(false,
                globalStatus,
                result,
                globalException);
    }

    /**
     * @param query           the String containing the SELECT query
     * @param consistencyType the type of consistency used for the query
     * @param timeout         the timeout used in the GDS for the query
     * @return The created {@link MessageData10QueryRequest} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData10QueryRequest createMessageData10QueryRequest(
            String query,
            ConsistencyType consistencyType,
            Long timeout) throws IOException, ValidationException {

        return new MessageData10QueryRequestImpl(false,
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
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData10QueryRequest createMessageData10QueryRequest(
            String query,
            ConsistencyType consistencyType,
            Long timeout,
            Integer pageSize,
            Integer queryType) throws IOException, ValidationException {

        return new MessageData10QueryRequestImpl(false,
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
     * @param globalException the global exception message (in plain, english text) if any errors happened.
     * @return The created {@link MessageData11QueryRequestAck} instance
     * @throws IOException         if any of the fields contain illegal value(type)s
     * @throws ValidationException if the contents of message violate the class invariant
     */
    public static MessageData11QueryRequestAck createMessageData11QueryRequestAck(
            AckStatus globalStatus,
            QueryResponseHolder queryResponse,
            String globalException) throws IOException, ValidationException {

        return new MessageData11QueryRequestAckImpl(false,
                globalStatus,
                queryResponse,
                globalException);
    }

    /**
     * @param queryContextHolder the ContextHolder containing information about the current query status
     * @param timeout            the timeout used in the GDS for the query
     * @return The created {@link MessageData12NextQueryPage} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData12NextQueryPage createMessageData12NextQueryPage(
            QueryContextHolder queryContextHolder,
            Long timeout) throws IOException, ValidationException {

        return new MessageData12NextQueryPageImpl(false,
                queryContextHolder, timeout);
    }

    /**
     * @param queryContextHolderSerializable the ContextHolder containing information about the current query status
     * @param timeout                        the timeout used in the GDS for the query
     * @return The created {@link MessageData12NextQueryPage} instance
     * @throws IOException         if any of the header fields contain illegal value(type)s
     * @throws ValidationException if the contents of the header violate the class invariant
     */
    public static MessageData12NextQueryPage createMessageData12NextQueryPage(
            QueryContextHolderSerializable queryContextHolderSerializable,
            Long timeout) throws IOException, ValidationException {

        return new MessageData12NextQueryPageImpl(false,
                queryContextHolderSerializable, timeout);
    }
}
