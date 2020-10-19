/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/14
 */

package hu.arh.gds.console;

import hu.arh.gds.client.Either;
import hu.arh.gds.client.Pair;
import hu.arh.gds.client.SyncGDSClient;
import hu.arh.gds.console.parser.ArgumentsHolder;
import hu.arh.gds.message.clienttypes.AttachmentResult;
import hu.arh.gds.message.clienttypes.EventResponse;
import hu.arh.gds.message.clienttypes.QueryResponse;
import hu.arh.gds.message.data.*;
import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.header.MessageHeaderBase;
import hu.arh.gds.message.util.MessageManager;
import hu.arh.gds.message.util.ValidationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ConsoleClient implements Runnable {

    private final ArgumentsHolder argumentsHolder;
    private final SyncGDSClient syncGDSClient;
    private final Logger logger;

    ConsoleClient(ArgumentsHolder argumentsHolder, SyncGDSClient client, Logger logger) {
        this.argumentsHolder = argumentsHolder;
        this.syncGDSClient = client;
        this.logger = logger;
    }

    private static Map<String, byte[]> loadAttachments(List<File> files) {
        Map<String, byte[]> binaries = new HashMap<>();
        if (files == null) {
            return binaries;
        }
        for (File file : files) {
            if (file.exists()) {
                try {
                    binaries.put(file.getName(), Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Could not read the file '" + file.getName() + "'!");
                }
            } else {
                throw new IllegalArgumentException("The file named '" + file.getName() + "' does not exist!");
            }
        }
        return binaries;
    }


    private void exportResult(MessageHeaderBase header, MessageData data) {

        String json = Utils.getJsonFromMessage(header, data);
        System.out.println("Response message in JSON format:");
        System.out.println(json);

        if (argumentsHolder.getExport()) {
            try {
                Utils.exportJson(header.getMessageId(), json);
            } catch (IOException e) {
                logger.severe(e.toString());
            }
        }
    }

    private void sendEvent() {
        try {
            EventResponse eventResponse = syncGDSClient.sendEvent2(MessageManager.createMessageData2Event(
                    argumentsHolder.getStatement(),
                    loadAttachments(argumentsHolder.getFiles()),
                    new ArrayList<>()));

            exportResult(eventResponse.getHeader(), eventResponse.getData());

        } catch (IllegalArgumentException | IOException | ValidationException iae) {
            logger.severe(iae.getMessage());
        }
    }


    private void sendAttachmentRequest() {
        try {
            AttachmentResult attachmentResult =
                    syncGDSClient.sendAttachmentRequest4(MessageManager.createMessageData4AttachmentRequest(
                            argumentsHolder.getStatement()
                    ));

            MessageHeaderBase header = attachmentResult.getHeader();
            String messageId = header.getMessageId();

            if (attachmentResult.isAttachmentRequestAck()) {
                MessageData5AttachmentRequestAck attachmentRequestAck = attachmentResult.getDataAsAttachmentRequestAck();
                exportResult(header, attachmentRequestAck);
                Utils.saveAttachment(messageId, attachmentRequestAck.getBinary(),
                        attachmentRequestAck.getData().getResult().getMeta());

            } else {
                MessageData6AttachmentResponse attachmentResponse = attachmentResult.getDataAsAttachmentResponse();
                exportResult(header, attachmentResponse);
                Utils.saveAttachment(messageId, attachmentResponse.getBinary(),
                        attachmentResponse.getResult().getMeta());
            }
        } catch (IllegalArgumentException | IOException | ValidationException iae) {
            logger.severe(iae.getMessage());
        }
    }

    private void sendQuery() {
        try {

            int counter = 0;
            QueryResponse queryResponse = syncGDSClient.sendQueryRequest10(MessageManager.createMessageData10QueryRequest(
                    argumentsHolder.getStatement(),
                    ConsistencyType.PAGES,
                    Long.valueOf(argumentsHolder.getTimeout())
            ));

            exportAndDisplayOnGUIifNeeded(++counter, queryResponse);

            if (argumentsHolder.getMessageType().equals(MessageType.QUERYALL)) {
                while (queryResponse.getData().getGlobalStatus() == AckStatus.OK &&
                        queryResponse.getData().getQueryResponseHolder().getMorePage()) {
                    queryResponse = syncGDSClient.sendNextQueryPage12(MessageManager.createMessageData12NextQueryPage(
                            queryResponse.getData().getQueryResponseHolder().getQueryContextHolder(), Long.valueOf(argumentsHolder.getTimeout())));

                    exportAndDisplayOnGUIifNeeded(++counter, queryResponse);
                }
            }

        } catch (IllegalArgumentException | IOException | ValidationException iae) {
            logger.severe(iae.getMessage());
        }
    }

    private void exportAndDisplayOnGUIifNeeded(int counter, QueryResponse queryResponse) {
        MessageHeaderBase header = queryResponse.getHeader();
        MessageData11QueryRequestAck data = queryResponse.getData();
        exportResult(header, data);
        if (!argumentsHolder.withNoGUI() && data.getGlobalStatus() == AckStatus.OK) {
            try {
                new ConsoleGUI(
                        counter,
                        header.getMessageId(),
                        data.getQueryResponseHolder()).display();
            } catch (IOException e) {
                logger.severe(e.toString());
            }
        }
    }

    @Override
    public void run() {
        try {
            if (syncGDSClient.connect()) {
                switch (argumentsHolder.getMessageType()) {
                    case EVENT:
                        sendEvent();
                        break;
                    case ATTACHMENT:
                        sendAttachmentRequest();
                        break;
                    case QUERY:
                    case QUERYALL:
                        sendQuery();
                        break;
                }
            } else {
                if (syncGDSClient.hasConnectionFailed()) {
                    System.err.println("Could not connect to the GDS! Reason: " + syncGDSClient.getConnectionError());
                } else if (syncGDSClient.hasLoginFailed()) {
                    System.err.println("Could not log in to the GDS! Reason: " + syncGDSClient.getLoginResponse().getSecond().getGlobalException());
                }
            }
        } finally {
            syncGDSClient.close();
        }
    }
}
