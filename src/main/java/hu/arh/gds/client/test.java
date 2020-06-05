package hu.arh.gds.client;

import hu.arh.gds.message.data.EventResultHolder;
import hu.arh.gds.message.data.FieldHolder;
import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.data.MessageData3EventAck;
import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.util.MessageManager;
import hu.arh.gds.message.util.ValidationException;
import hu.arh.gds.message.util.WriteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class test {

    private static final Logger logger = Logger.getLogger("logging");
    private static final GDSWebSocketClient client = new GDSWebSocketClient(
            "ws://127.0.0.1:8080/gate",
            "user",
            null,
            logger
    );

    public static void main(String[] args) throws ValidationException, IOException, WriteException, AlreadySubscribedException {

        client.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(MessageHeader header, MessageData data) {
                switch (data.getTypeHelper().getMessageDataType()) {
                    case EVENT_ACK_3:
                        MessageData3EventAck eventAckData = data.getTypeHelper().asEventAckMessageData3();
                        // ...
                        break;
                    //...
                }
            }

            @Override
            public void onConnected() {
                // ...
            }

            @Override
            public void onDisconnected() {
                // ...
            }
        });
    }

    private static void insert() {
        try {
            MessageData data = MessageManager.createMessageData2Event(
                    new ArrayList<String>() {{
                        add("INSERT INTO events (id, numberplate, speed, images) VALUES('EVNT202001010000000000', 'ABC123', 90, array('ATID202001010000000000'))");
                        add("INSERT INTO \"events-@attachment\" (id, meta, data) VALUES('ATID202001010000000000', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
                    }},
                    new HashMap<String, byte[]>() {{
                        put("62696e6172795f69645f6578616d706c65", new byte[] {127, 127, 0, 0});
                    }},
                    new ArrayList<>());
            client.sendMessage(data);
        } catch (Throwable throwable) {}
    }

    private static void update() {
        try {
            MessageData data = MessageManager.createMessageData2Event(
                    new ArrayList<String>() {{
                        add("UPDATE events SET speed = 100 WHERE id = 'EVNT202001010000000000'");
                    }},
                    new HashMap<>(),
                    new ArrayList<>());
            client.sendMessage(data);
        } catch (Throwable throwable) {}
    }

    private static void merge() {
        try {
            MessageData data = MessageManager.createMessageData2Event(
                    new ArrayList<String>() {{
                        add("MERGE INTO events USING (SELECT 'EVNT202001010000000000' as id, 'ABC123' as numberplate, 100 as speed) I " +
                                "ON (events.id = I.id) " +
                                "WHEN MATCHED THEN UPDATE SET events.speed = I.speed " +
                                "WHEN NOT MATCHED THEN INSERT (id, numberplate) VALUES (I.id, I.numberplate)");
                    }},
                    new HashMap<>(),
                    new ArrayList<>());
            client.sendMessage(data);
        } catch (Throwable throwable) {}
    }

    private static void select() {
        try {
            MessageData data = MessageManager.createMessageData4AttachmentRequest(
                    "SELECT * FROM \"events-@attachment\" WHERE id='ATID202001010000000000' and ownerid='EVNT202001010000000000' FOR UPDATE WAIT 86400");
            client.sendMessage(data);
        } catch (Throwable throwable) {}
    }
}
