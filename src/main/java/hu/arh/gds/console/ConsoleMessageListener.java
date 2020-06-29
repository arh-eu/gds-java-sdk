package hu.arh.gds.console;

import com.google.gson.*;
import hu.arh.gds.client.GDSWebSocketClient;
import hu.arh.gds.client.MessageListener;
import hu.arh.gds.message.data.*;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.header.MessageHeaderTypeHelper;
import hu.arh.gds.message.util.MessageManager;
import org.msgpack.value.Value;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleMessageListener implements MessageListener {
    private final ConsoleArguments consoleArguments;
    private final GDSWebSocketClient client;
    private final Integer timeout;

    private CountDownLatch eventAckLatch;
    private CountDownLatch attachmentRequestAckLatch;
    private CountDownLatch attachmentResponseLatch;
    private CountDownLatch queryAckLatch;

    private AtomicBoolean closeConnection = new AtomicBoolean(true);

    private static final String ATTACHMENTS_FOLDER = "attachments";

    public ConsoleMessageListener(ConsoleArguments consoleArguments, GDSWebSocketClient client) {
        this.consoleArguments = consoleArguments;
        this.client = client;
        this.timeout = consoleArguments.getTimeout();
    }

    private void closeClient() {
        if(client != null && !client.connected()) {
            client.close();
        }
    }

    private void writeToFile(String json, String messageId) {
        File file = new File("exports" + "/" + messageId);
        try (FileWriter writer = new FileWriter(file.getPath());
             BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(json);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            closeClient();
        }
    }

    private String getPrettyJson(Object obj) {
        File exportsFolder = new File("exports");
        if(!exportsFolder.exists()) {
            exportsFolder.mkdir();
        }

        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Value> valueJsonSerializer = (value, type, jsonSerializationContext) ->
                new JsonParser().parse(value.toString());

        JsonSerializer<byte[]> binaryJsonSerializer = (value, type, jsonSerializationContext) ->
                new JsonParser().parse(value == null ? "null" : String.valueOf(value.length) + "bytes");

        gsonBuilder.registerTypeAdapter(Value.class, valueJsonSerializer);
        gsonBuilder.registerTypeAdapter(byte[].class, binaryJsonSerializer);

        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                if(fieldAttributes.getName().equals("cache")
                        || fieldAttributes.getName().equals("messageSize")
                        || (fieldAttributes.getName().equals("binary"))) {
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                if(aClass.equals(MessageHeaderTypeHelper.class)
                        || aClass.equals(MessageDataTypeHelper.class)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        gsonBuilder.setExclusionStrategies(strategy);

        Gson gson = gsonBuilder.setPrettyPrinting().serializeNulls().create();
        return gson.toJson(obj);
    }

    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        //System.out.println(data.getTypeHelper().getMessageDataType() + " type message received");
        //System.out.println(data.toString());

        String json = getPrettyJson(new Message(header, data));
        System.out.println(json);

        if(consoleArguments.getExport()) {
            writeToFile(json, header.getTypeHelper().asBaseMessageHeader().getMessageId());
        }

        switch (data.getTypeHelper().getMessageDataType()) {
            case EVENT_ACK_3:
                handleEventAck(header, data);
                break;
            case ATTACHMENT_REQUEST_ACK_5:
                handleAttachmentRequestAck(header, data);
                break;
            case ATTACHMENT_RESPONSE_6:
                handleAttachmentResponse(header, data);
                break;
            case QUERY_REQUEST_ACK_11:
                //printQueryReply(data.getTypeHelper().asQueryRequestAckMessageData11());
                handleQueryAck(header, data);
                break;
            default:
                closeClient();
                break;
        }
    }

    private void printQueryReply(MessageData11QueryRequestAck data) {
        StringBuilder sb = new StringBuilder();
        if (data.getGlobalStatus().isErrorStatus()) {
            sb.append("The query was unsuccessful!").append(System.lineSeparator());
            appendErrorCode(sb, data);
        } else {
            sb.append("Query was successful! Total of ").append(data.getQueryResponseHolder().getNumberOfHits()).append(" record(s) returned.").append(System.lineSeparator());
            sb.append("Records: ").append(System.lineSeparator());
            data.getQueryResponseHolder().getHits().forEach( hit -> sb.append(hit).append(System.lineSeparator()));
        }
        System.out.println(sb.toString());
    }

    private void appendErrorCode(StringBuilder sb, Ack data) {
        sb.append("Status: ").append(data.getGlobalStatus().name()).append(" (").append(data.getGlobalStatus().getValue()).append(")").append(System.lineSeparator());
        sb.append("Error message: ").append(data.getGlobalException()).append(System.lineSeparator());
    }

    @Override
    public void onConnectionFailed(String reason) {
        System.out.println("Failed to connecting to the GDS: " + reason);
    }

    @Override
    public void onConnected() {
        System.out.println("Client connected!");
        switch (consoleArguments.getMessageType()) {
            case EVENT:
                System.out.println("Sending event message...");
                try {
                    eventAckLatch = new CountDownLatch(1);
                    client.sendMessage(MessageManager.createMessageData2Event(
                            consoleArguments.getStatement(),
                            loadAttachments(consoleArguments.getFiles()),
                            new ArrayList<>()));
                    waitForResponse(
                            eventAckLatch,
                            timeout);
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                    return;
                }
                break;
            case ATTACHMENT:
                System.out.println("Sending attachment request message...");
                try {
                    attachmentRequestAckLatch = new CountDownLatch(1);
                    client.sendMessage(MessageManager.createMessageData4AttachmentRequest(
                            consoleArguments.getStatement()
                    ));
                    waitForResponse(
                            attachmentRequestAckLatch,
                            timeout
                    );
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                    return;
                }
                break;
            case QUERY:
            case QUERYALL:
                System.out.println("Sending query message...");
                try {
                    queryAckLatch = new CountDownLatch(1);
                    client.sendMessage(MessageManager.createMessageData10QueryRequest(
                            consoleArguments.getStatement(),
                            ConsistencyType.PAGES,
                            60_000L
                    ));
                    waitForResponse(
                            queryAckLatch,
                            timeout
                    );
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                    return;
                }
                break;
            default:
                return;
        }
    }

    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }

    private void waitForResponse(CountDownLatch latch, int timeout) {
        new Thread(() -> {
            try {
                latch.await(timeout, TimeUnit.MILLISECONDS);
                if (client.connected()) {
                    if (closeConnection.get()) {
                        client.close();
                    } else {
                        closeConnection.set(true);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                closeClient();
            }
        }).start();
    }

    private void countDownLatch(CountDownLatch latch) {
        if (latch != null) {
            latch.countDown();
        } else {
            if (client.connected() && closeConnection.get()) {
                client.close();
            }
        }
    }

    private void handleEventAck(MessageHeader header, MessageData data) {
        countDownLatch(eventAckLatch);
    }

    private void handleAttachmentRequestAck(MessageHeader header, MessageData data) {
        MessageData5AttachmentRequestAck attachmentRequestAckData =
                data.getTypeHelper().asAttachmentRequestAckMessageData5();
        if (attachmentRequestAckData.getData() != null) {
            byte[] attachment = attachmentRequestAckData.getData().getResult().getAttachment();
            if (attachment != null) {
                saveAttachment(attachment, header.getTypeHelper().asBaseMessageHeader().getMessageId());
                countDownLatch(attachmentRequestAckLatch);
            } else {
                closeConnection.set(false);
                countDownLatch(attachmentRequestAckLatch);
                attachmentResponseLatch = new CountDownLatch(1);
                waitForResponse(attachmentResponseLatch, timeout);
            }
        } else {
            countDownLatch(attachmentRequestAckLatch);
        }
    }

    private void handleAttachmentResponse(MessageHeader header, MessageData data) {
        MessageData6AttachmentResponse attachmentResponseData =
                data.getTypeHelper().asAttachmentResponseMessageData6();
        byte[] attachment = attachmentResponseData.getResult().getAttachment();
        if (attachment != null) {
            saveAttachment(attachment, header.getTypeHelper().asBaseMessageHeader().getMessageId());
        }
        countDownLatch(attachmentResponseLatch);
    }

    private void handleQueryAck(MessageHeader header, MessageData data) {
        MessageData11QueryRequestAck queryRequestAck =
                data.getTypeHelper().asQueryRequestAckMessageData11();
        if (queryRequestAck.getQueryResponseHolder() != null && queryRequestAck.getQueryResponseHolder().getMorePage() && consoleArguments.getMessageType().equals(MessageType.QUERYALL)) {
            QueryContextHolder queryContextHolder = queryRequestAck.getQueryResponseHolder().getQueryContextHolder();
            try {
                closeConnection.set(false);
                countDownLatch(queryAckLatch);
                MessageData12NextQueryPage nextQueryPageData = MessageManager.createMessageData12NextQueryPage(
                        queryContextHolder, 60_000L);
                client.sendMessage(nextQueryPageData, header.getTypeHelper().asBaseMessageHeader().getMessageId());
                queryAckLatch = new CountDownLatch(1);
                waitForResponse(queryAckLatch, timeout);
            } catch (Throwable e) {
                System.out.println(e.getMessage());
                closeClient();
            }
        } else {
            countDownLatch(queryAckLatch);
        }
    }

    private void saveAttachment(byte[] attachment, String messageId) {
        try {
            File attachmentsFolder = new File(ATTACHMENTS_FOLDER);
            if (!attachmentsFolder.exists()) {
                attachmentsFolder.mkdir();
            }
            OutputStream os = new FileOutputStream(attachmentsFolder.getPath() + "/" + messageId);
            os.write(attachment);
            os.close();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            closeClient();
        }
    }

    private static Map<String, byte[]> loadAttachments(List<File> files) {
        Map<String, byte[]> binaries = new HashMap<>();
        if(files == null) {
            return binaries;
        }
        for (File file : files) {
            if (file.exists()) {
                try {
                    byte[] binary = Files.readAllBytes(file.toPath());
                    binaries.put(file.getName(), binary);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return binaries;
    }
}
