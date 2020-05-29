package hu.arh.gds.client;

import hu.arh.gds.client.websocket.BinaryMessageListener;
import hu.arh.gds.client.websocket.ConnectionStateListener;
import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.data.PriorityLevelHolder;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.util.MessageManager;
import hu.arh.gds.message.util.ValidationException;
import hu.arh.gds.message.util.WriteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class test {

    public static void main(String[] args) throws ValidationException, IOException, WriteException {
        final Logger logger = Logger.getLogger("logging");

        final GDSWebSocketClient client = new GDSWebSocketClient(
                "ws://127.0.0.1:8080/gate",
                "user",
                null,
                logger
        );

        client.setConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void onConnected() {
                System.out.println("Client connected!");
                // ...
            }

            @Override
            public void onDisconnected() {
                System.out.println("Client disconnected!");
                // ...
            }
        });

        client.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(MessageHeader header, MessageData data) {
                if(data.getTypeHelper().isEventAckMessageData3()) {
                    // do something with the message...
                }
            }
        });

        client.setBinaryMessageListener(new BinaryMessageListener() {
            @Override
            public void onMessageReceived(byte[] message) {
                System.out.println("message received");
                // ...
            }
        });

        client.connect();

        MessageHeader header = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.EVENT_2);

        List<String> operationsStringBlock = new ArrayList<String>();
        operationsStringBlock.add("INSERT INTO events (id, some_field, images) VALUES('EVNT202001010000000000', 'some_field', array('ATID202001010000000000'));INSERT INTO \"events-@attachment\" (id, meta, data) VALUES('ATID202001010000000000', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
        Map<String, byte[]> binaryContentsMapping = new HashMap<>();
        binaryContentsMapping.put("62696e6172795f69645f6578616d706c65", new byte[] { 1, 2, 3 });
        MessageData data = MessageManager.createMessageData2Event(operationsStringBlock, binaryContentsMapping, new ArrayList<PriorityLevelHolder>());

        //byte[] eventMessage = MessageManager.createMessage(eventMessageHeader, eventMessageData);

        client.sendMessage(header, data);
    }
}
