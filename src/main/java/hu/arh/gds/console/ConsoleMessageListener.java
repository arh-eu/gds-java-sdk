package hu.arh.gds.console;

import hu.arh.gds.client.GDSWebSocketClient;
import hu.arh.gds.client.MessageListener;
import hu.arh.gds.message.data.*;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.util.MessageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleMessageListener implements MessageListener {
    private final ConsoleArguments consoleArguments;
    private final GDSWebSocketClient client;
    private final int timeout;

    private CountDownLatch eventAckLatch;
    private CountDownLatch attachmentRequestAckLatch;
    private CountDownLatch attachmentResponseLatch;
    private CountDownLatch queryAckLatch;

    private AtomicBoolean closeConnection = new AtomicBoolean(true);

    public ConsoleMessageListener(ConsoleArguments consoleArguments, GDSWebSocketClient client) {
        this.consoleArguments = consoleArguments;
        this.client = client;
        this.timeout = consoleArguments.getTimeout();
    }

    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        System.out.println(data.getTypeHelper().getMessageDataType() + " type message received");
        System.out.println(data.toString());

        switch (data.getTypeHelper().getMessageDataType()) {
            case EVENT_2:
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
                if (client.connected()) {
                    client.close();
                }
                break;
        }
    }

    @Override
    public void onConnected() {
        System.out.println("Client connected!");
        switch (consoleArguments.getMessageType()) {
            case EVENT:
                System.out.println("Sending event message...");
                try {
                    client.sendMessage(MessageManager.createMessageData2Event(
                            new ArrayList<String>() {{
                                add(consoleArguments.getStatement());
                            }},
                            new HashMap<>(),
                            new ArrayList<>()));
                    eventAckLatch = new CountDownLatch(1);
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
                    client.sendMessage(MessageManager.createMessageData4AttachmentRequest(
                            consoleArguments.getStatement()
                    ));
                    attachmentRequestAckLatch = new CountDownLatch(1);
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
                    client.sendMessage(MessageManager.createMessageData10QueryRequest(
                            consoleArguments.getStatement(),
                            ConsistencyType.PAGES,
                            60_000L
                    ));
                    queryAckLatch = new CountDownLatch(1);
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

    private void saveAttachment(byte[] attachment) {
        try {
            OutputStream os = new FileOutputStream(new File("attachment"));
            os.write(attachment);
            os.close();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    private void waitForResponse(CountDownLatch latch, int timeout) {
        new Thread(() -> {
            try {
                latch.await(timeout, TimeUnit.MILLISECONDS);
                if (client.connected()) {
                    if(closeConnection.get()) {
                        client.close();
                    } else {
                        closeConnection.set(true);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
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
                saveAttachment(attachment);
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
            saveAttachment(attachment);
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
            }
        } else {
            countDownLatch(queryAckLatch);
        }
    }
}
