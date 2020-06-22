/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.util;

import hu.arh.gds.message.data.AttachmentRequestAckDataHolder;
import hu.arh.gds.message.data.AttachmentResponseAckResultHolder;
import hu.arh.gds.message.data.AttachmentResultHolder;
import hu.arh.gds.message.data.ConsistencyType;
import hu.arh.gds.message.data.EventDocumentResultHolder;
import hu.arh.gds.message.data.EventHolder;
import hu.arh.gds.message.data.EventResultHolder;
import hu.arh.gds.message.data.FieldHolder;
import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.data.MessageData0Connection;
import hu.arh.gds.message.data.MessageData0ConnectionDescriptor;
import hu.arh.gds.message.data.MessageData10QueryRequest;
import hu.arh.gds.message.data.MessageData11QueryRequestAck;
import hu.arh.gds.message.data.MessageData12NextQueryPage;
import hu.arh.gds.message.data.MessageData1ConnectionAck;
import hu.arh.gds.message.data.MessageData2Event;
import hu.arh.gds.message.data.MessageData3EventAck;
import hu.arh.gds.message.data.MessageData4AttachmentRequest;
import hu.arh.gds.message.data.MessageData5AttachmentRequestAck;
import hu.arh.gds.message.data.MessageData6AttachmentResponse;
import hu.arh.gds.message.data.MessageData7AttachmentResponseAck;
import hu.arh.gds.message.data.MessageData8EventDocument;
import hu.arh.gds.message.data.MessageData9EventDocumentAck;
import hu.arh.gds.message.data.PriorityLevelHolder;
import hu.arh.gds.message.data.QueryContextHolder;
import hu.arh.gds.message.data.QueryContextHolderSerializable;
import hu.arh.gds.message.data.QueryResponseHolder;
import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.data.impl.MessageData0ConnectionImpl;
import hu.arh.gds.message.data.impl.MessageData10QueryRequestImpl;
import hu.arh.gds.message.data.impl.MessageData11QueryRequestAckImpl;
import hu.arh.gds.message.data.impl.MessageData12NextQueryPageImpl;
import hu.arh.gds.message.data.impl.MessageData1ConnectionAckImpl;
import hu.arh.gds.message.data.impl.MessageData2EventImpl;
import hu.arh.gds.message.data.impl.MessageData3EventAckImpl;
import hu.arh.gds.message.data.impl.MessageData4AttachmentRequestImpl;
import hu.arh.gds.message.data.impl.MessageData5AttachmentRequestAckImpl;
import hu.arh.gds.message.data.impl.MessageData6AttachmentResponseImpl;
import hu.arh.gds.message.data.impl.MessageData7AttachmentResponseAckImpl;
import hu.arh.gds.message.data.impl.MessageData8EventDocumentImpl;
import hu.arh.gds.message.data.impl.MessageData9EventDocumentAckImpl;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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

    public static byte[] createMessage(MessageHeader header, MessageData data) throws IOException, WriteException, ValidationException {

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
                throw new WriteException(String.format("%s: Unknown message header type (%s)",
                        MessageManager.class.getSimpleName(),
                        header.getTypeHelper().getMessageHeaderType()));
        }

        binary.write(header.getBinary());
        binary.write(data.getBinary());

        return binary.toByteArray();
    }

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

    public static MessageHeader getMessageHeader(byte[] header, MessageHeaderType headerType) throws IOException, ReadException, ValidationException {

        ExceptionHelper.requireNonNullValue(header, MessageManager.class.getSimpleName(), "header");

        switch (headerType) {
            case BASE:
                return new MessageHeaderBaseImpl(header, true, false);
            default:
                throw new ReadException(String.format("%s: Unknown message header type (%s)",
                        MessageManager.class.getSimpleName(),
                        headerType));
        }
    }

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

    public static MessageData getMessageDataFromPartialBinary(byte[] data, MessageDataType dataType) throws IOException, ReadException, ValidationException {

        ExceptionHelper.requireNonNullValue(data, MessageManager.class.getSimpleName(), "data");

        switch (dataType) {
            case CONNECTION_0:
                return new MessageData0ConnectionImpl(data, false, false);
            case CONNECTION_ACK_1:
                return new MessageData1ConnectionAckImpl(data, false, false);
            case EVENT_2:
                return new MessageData2EventImpl(data, false, false);
            case EVENT_ACK_3:
                return new MessageData3EventAckImpl(data, false, false);
            case ATTACHMENT_REQUEST_4:
                return new MessageData4AttachmentRequestImpl(data, false, false);
            case ATTACHMENT_REQUEST_ACK_5:
                return new MessageData5AttachmentRequestAckImpl(data, false, false);
            case ATTACHMENT_RESPONSE_6:
                return new MessageData6AttachmentResponseImpl(data, false, false);
            case ATTACHMENT_RESPONSE_ACK_7:
                return new MessageData7AttachmentResponseAckImpl(data, false, false);
            case EVENT_DOCUMENT_8:
                return new MessageData8EventDocumentImpl(data, false, false);
            case EVENT_DOCUMENT_ACK_9:
                return new MessageData9EventDocumentAckImpl(data, false, false);
            case QUERY_REQUEST_10:
                return new MessageData10QueryRequestImpl(data, false, false);
            case QUERY_REQUEST_ACK_11:
                return new MessageData11QueryRequestAckImpl(data, false, false);
            case NEXT_QUERY_PAGE_12:
                return new MessageData12NextQueryPageImpl(data, false, false);
            default:
                throw new ReadException(String.format("%s: Unknown message data type (%s)",
                        MessageManager.class.getSimpleName(),
                        dataType));
        }
    }

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

    public static MessageData2Event createMessageData2Event(
            List<String> operations,
            Map<String, byte[]> binaryContents,
            List<PriorityLevelHolder> priorityLevels) throws IOException, ValidationException {

        return new MessageData2EventImpl(false,
                operations,
                binaryContents,
                priorityLevels);
    }

    public static MessageData2Event createMessageData2Event(
            String operations,
            Map<String, byte[]> binaryContents,
            List<PriorityLevelHolder> priorityLevels) throws IOException, ValidationException {

        return new MessageData2EventImpl(false,
                operations,
                binaryContents,
                priorityLevels);
    }

    public static MessageData3EventAck createMessageData3EventAck(
            List<EventResultHolder> eventResults,
            AckStatus globalStatus,
            String globalException) throws IOException, ValidationException {

        return new MessageData3EventAckImpl(false,
                eventResults,
                globalStatus,
                globalException);
    }

    public static MessageData4AttachmentRequest createMessageData4AttachmentRequest(
            String request) throws IOException, ValidationException {

        return new MessageData4AttachmentRequestImpl(false,
                request);
    }

    public static MessageData5AttachmentRequestAck createMessageData5AttachmentRequestAck
            (AckStatus globalStatus,
             AttachmentRequestAckDataHolder data,
             String globalException) throws IOException, ValidationException {

        return new MessageData5AttachmentRequestAckImpl(false,
                globalStatus,
                data,
                globalException);
    }

    public static MessageData6AttachmentResponse createMessageData6AttachmentResponse(
            AttachmentResultHolder result,
            EventHolder eventHolder
    ) throws IOException, ValidationException {

        return new MessageData6AttachmentResponseImpl(false,
                result,
                eventHolder);
    }

    public static MessageData7AttachmentResponseAck createMessageData7AttachmentResponseAck(
            AckStatus globalStatus,
            AttachmentResponseAckResultHolder data,
            String globalException) throws IOException, ValidationException {

        return new MessageData7AttachmentResponseAckImpl(false,
                globalStatus,
                data,
                globalException);
    }

    public static MessageData8EventDocument createMessageData8EventDocument(
            String tableName,
            List<FieldHolder> fieldHolders,
            List<List<Value>> records) throws IOException, ValidationException {
        return createMessageData8EventDocument(tableName, fieldHolders, records, new HashMap<>(0));
    }

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

    public static MessageData9EventDocumentAck createMessageMessageData9EventDocumentAck(
            AckStatus globalStatus,
            List<EventDocumentResultHolder> result,
            String globalException) throws IOException, ValidationException {

        return new MessageData9EventDocumentAckImpl(false,
                globalStatus,
                result,
                globalException);
    }

    public static MessageData10QueryRequest createMessageData10QueryRequest(
            String query,
            ConsistencyType consistencyType,
            Long timeout) throws IOException, NullPointerException, ValidationException {

        return new MessageData10QueryRequestImpl(false,
                query,
                consistencyType,
                timeout);
    }

    public static MessageData10QueryRequest createMessageData10QueryRequest(
            String query,
            ConsistencyType consistencyType,
            Long timeout,
            Integer pageSize,
            Integer queryType) throws IOException, NullPointerException, ValidationException {

        return new MessageData10QueryRequestImpl(false,
                query,
                consistencyType,
                timeout,
                pageSize,
                queryType);
    }

    public static MessageData11QueryRequestAck createMessageData11QueryRequestAck(
            AckStatus globalStatus,
            QueryResponseHolder queryResponse,
            String globalException) throws IOException, NullPointerException, ValidationException {

        return new MessageData11QueryRequestAckImpl(false,
                globalStatus,
                queryResponse,
                globalException);
    }

    public static MessageData12NextQueryPage createMessageData12NextQueryPage(
            QueryContextHolder queryContextHolder,
            Long timeout) throws IOException, NullPointerException, ValidationException {

        return new MessageData12NextQueryPageImpl(false,
                queryContextHolder, timeout);
    }

    public static MessageData12NextQueryPage createMessageData12NextQueryPage(
            QueryContextHolderSerializable queryContextHolderSerializable,
            Long timeout) throws IOException, NullPointerException, ValidationException {

        return new MessageData12NextQueryPageImpl(false,
                queryContextHolderSerializable, timeout);
    }
}