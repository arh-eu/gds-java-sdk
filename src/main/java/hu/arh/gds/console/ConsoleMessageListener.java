package hu.arh.gds.console;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.*;
import hu.arh.gds.client.GDSWebSocketClient;
import hu.arh.gds.client.MessageListener;
import hu.arh.gds.console.parser.ArgumentsHolder;
import hu.arh.gds.message.data.*;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.util.MessageManager;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleMessageListener implements MessageListener {
    private final ArgumentsHolder argumentsHolder;
    private final GDSWebSocketClient client;
    private final Integer timeout;

    private CountDownLatch eventAckLatch;
    private CountDownLatch attachmentRequestAckLatch;
    private CountDownLatch attachmentResponseLatch;
    private CountDownLatch queryAckLatch;

    private AtomicBoolean closeConnection = new AtomicBoolean(true);

    private List<QueryAckHolder> queryAckHolders = new ArrayList<>();

    private int counter = 1;

    public ConsoleMessageListener(ArgumentsHolder argumentsHolder, GDSWebSocketClient client) {
        this.argumentsHolder = argumentsHolder;
        this.client = client;
        this.timeout = argumentsHolder.getTimeout();
    }

    private void closeClient() {
        if (client != null && !client.connected()) {
            client.close();
        }
    }

    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        System.out.println(data.getTypeHelper().getMessageDataType() + " message received.");

        String json = Utils.getJsonFromMessage(header, data);
        System.out.println("Response message in JSON format:");
        System.out.println(json);

        if (argumentsHolder.getExport()) {
            try {
                Utils.exportJson(header.getTypeHelper().asBaseMessageHeader().getMessageId(), json);
            } catch (IOException e) {
                e.printStackTrace();
                closeClient();
            }
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
                handleQueryAck(header, data);
                break;
            default:
                closeClient();
                break;
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        System.out.println("Failed to connecting to the GDS: " + reason);
    }

    @Override
    public void onConnected() {
        System.out.println("Client connected!");
        switch (argumentsHolder.getMessageType()) {
            case EVENT:
                System.out.println("Sending event message...");
                try {
                    eventAckLatch = new CountDownLatch(1);
                    client.sendMessage(MessageManager.createMessageData2Event(
                            argumentsHolder.getStatement(),
                            loadAttachments(argumentsHolder.getFiles()),
                            new ArrayList<>()));
                    System.out.println("Event message sent.");
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
                            argumentsHolder.getStatement()
                    ));
                    System.out.println("Attachment request message sent.");
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
                            argumentsHolder.getStatement(),
                            ConsistencyType.PAGES,
                            60_000L
                    ));
                    System.out.println("Query message sent.");
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
                try {
                    Utils.saveAttachment(header.getTypeHelper().asBaseMessageHeader().getMessageId(), attachment, attachmentRequestAckData.getData().getResult().getMeta());
                } catch (IOException e) {
                    closeClient();
                }
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
            try {
                Utils.saveAttachment(header.getTypeHelper().asBaseMessageHeader().getMessageId(), attachment, attachmentResponseData.getResult().getMeta());
            } catch (IOException e) {
                closeClient();
            }
        }
        countDownLatch(attachmentResponseLatch);
    }

    private void handleQueryAck(MessageHeader header, MessageData data) {
        MessageData11QueryRequestAck queryRequestAck =
                data.getTypeHelper().asQueryRequestAckMessageData11();

        queryAckHolders.add(
                new QueryAckHolder(header.getTypeHelper().asBaseMessageHeader().getMessageId(),
                        data.getTypeHelper().asQueryRequestAckMessageData11().getQueryResponseHolder()));

        if (queryRequestAck.getQueryResponseHolder().getMorePage() && argumentsHolder.getMessageType().equals(MessageType.QUERYALL)) {
            QueryContextHolder queryContextHolder = queryRequestAck.getQueryResponseHolder().getQueryContextHolder();
            try {
                if (!client.connected()) {
                    client.connect();
                }
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

            TerminalScreen screen;
            MultiWindowTextGUI gui;

            try {
                if (Utils.isWindows()) {
                    SwingTerminalFrame swingTerminalFrame = new SwingTerminalFrame(
                            "gds console client",
                            TerminalEmulatorDeviceConfiguration.getDefault(),
                            SwingTerminalFontConfiguration.getDefault(),
                            TerminalEmulatorColorConfiguration.getDefault(),
                            TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
                    swingTerminalFrame.setDefaultCloseOperation(
                            WindowConstants.EXIT_ON_CLOSE);
                    swingTerminalFrame.setVisible(true);
                    screen = new TerminalScreen(swingTerminalFrame);
                } else {
                    screen = new DefaultTerminalFactory().createScreen();
                }

                gui = new MultiWindowTextGUI(screen);
                gui.setTheme(new TableWindowThemeDefinition(screen));

                for (QueryAckHolder queryAckHolder : queryAckHolders) {
                    try {
                        new TableWindow(
                                gui, screen,
                                queryAckHolder.getQueryResponseHolder(),
                                queryAckHolder.getMessageId(),
                                counter++)
                                .show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, byte[]> loadAttachments(List<File> files) {
        Map<String, byte[]> binaries = new HashMap<>();
        if (files == null) {
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
