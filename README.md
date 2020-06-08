## Installation

With [JitPack](https://jitpack.io/), you can easily add this project as a maven dependency:

```XML
<repositories>
    <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.arh-eu</groupId>
        <artifactId>gds-java-messages</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>
</dependencies>
```

(The library was made by [this](https://github.com/msgpack/msgpack-java) messagepack java implementation)

## How to create messages

A message consists of two parts, a header and a hata. You can create these objects through the hu.arh.gds.message.util.MessageManager class.

The following example shows the process of creating messages by creating an attachment request type message.

First, we create the header part.
```java
MessageHeader header = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.CONNECTION_0);
```

After that, we create the data part.
```java
MessageData data  = MessageManager.createMessageData4AttachmentRequest("SELECT * FROM \"events-@attachment\" WHERE id='ATID202001010000000000' and ownerid='EVNT202001010000000000' FOR UPDATE WAIT 86400");
```

Once we have a header and a data, we can create the binary message.
```java
byte[] message = MessageManager.createMessage(header, data);
```

## How to send and receive messages

Messages can be sent to the GDS via WebSocket protocol. The SDK contains a WebSocket client with basic functionalities, so you can use this to send and receive messages.
You can also find a GDS Server Simulator written in Java here. With this simulator you can test your client code without a real GDS instance.

A message can be sent as follows.

First, we create the client object and connect to the GDS.
```java
final Logger logger = Logger.getLogger("logging");

final GDSWebSocketClient client = new GDSWebSocketClient(
        "ws://127.0.0.1:8080/gate",
        "user",
        null,
        logger
);
```

The messages sent to the client and the changes of the connection status can be accessed via listeners.
There are two types of listener for this. One to access the serialized message objects and the other to access the binary representation of the message.

If you would like to be notified of changes in the connection status, you can subscribe to the following listener.
There are two type of listener for this.
One to access the serialized message objects and the other to access the binary representation of the message.

```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        System.out.println(data.getTypeHelper().getMessageDataType() + " message received!");
        //do something with the message...
    }
    @Override
    public void onConnected() {
        System.out.println("Client connected!");
    }
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

If you use the BinaryMessageListener, you have to create the message objects from the binary representation of the message.
You can create these objects through the hu.arh.gds.message.util.MessageManager class.

```java
client.setBinaryMessageListener(new BinaryMessageListener() {
    @Override
    public void onMessageReceived(byte[] message) {
        try {
            MessageHeader header = MessageManager.getMessageHeaderFromBinaryMessage(message);
            MessageData data = MessageManager.getMessageData(message);
            System.out.println(data.getTypeHelper().getMessageDataType() + " message received!");
            // do something with the message...
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onConnected() {
        System.out.println("Client connected!");
    }
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

Connecting to the GDS.

```java
client.connect();
```

(During the connection, a connection type message is also sent after the websocket connection. If a positive acknowledgment message arrives, the connected() method returns true.)


After you connected, you can send messages to the GDS. You can do that with the sendMessage() methods.

Send message with the binary representation of the message.
```java
void sendMessage(byte[] message)
```

Send message with the header and data part.
```java
void sendMessage(MessageHeader header, MessageData data)
```

Send message with the data part. The header part is completed automatically with the default values.
```java
void sendMessage(MessageData data)
```

Send message with the data part (the header part is completed automatically with the default values), but without automatic generation of message id.
```java
void sendMessage(MessageData data, String messageId)
```

In the examples, we will use the method in which the header part is generated automatically, but you can define the header part explicit at any time. 
```java
MessageHeader header = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.EVENT_2);
```

And then you can send the message like this:
```java
client.SendMessage(header, data);
```

- [INSERT](#Insert)
- [UPDATE](#Update)
- [MERGE](#Merge)
- [ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE](#ACK-MESSAGE-FOR-THE-INSERT-UPDATE-AND-MERGE)
- [SELECT](#Select)
	- [QUERY](#Query)
	- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)
- [AUTOMATIC PUSHING](#AUTOMATIC-PUSHING)

### INSERT
```java
try {
    MessageData data = MessageManager.createMessageData2Event(
            new ArrayList<String>() {{
                add("INSERT INTO events (id, numberplate, speed, images) VALUES('EVNT202001010000000000', 'ABC123', 90, array('ATID202001010000000000'))");
                add("INSERT INTO \"events-@attachment\" (id, meta, data) VALUES('ATID202001010000000000', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
            }},
            new HashMap<String, byte[]>() {{
                put("62696e6172795f69645f6578616d706c65", new byte[]{127, 127, 0, 0});
            }},
            new ArrayList<>());
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

### UPDATE
```java
try {
    MessageData data = MessageManager.createMessageData2Event(
            new ArrayList<String>() {{
                add("UPDATE events SET speed = 100 WHERE id = 'EVNT202001010000000000'");
            }},
            new HashMap<>(),
            new ArrayList<>());
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

### MERGE
```java
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
} catch (Throwable e) {
    e.printStackTrace();
}
```

### ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE

The ack for these messages is available through the subscribed listener.
```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        switch (data.getTypeHelper().getMessageDataType()) {
            case EVENT_ACK_3:
                MessageData3EventAck eventAckData = data.getTypeHelper().asEventAckMessageData3();
                System.out.println("Event ACK message received with '" + eventAckData.getGlobalStatus() + "' status code");
                //do something with the ack message...
                break;
            //...
        }
    }
    @Override
    public void onConnected() {
        System.out.println("Client connected!");
    }
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

### SELECT

#### QUERY

```java
try {
    MessageData data = MessageManager.createMessageData10QueryRequest(
            "SELECT * FROM events",
            ConsistencyType.NONE,
            60_000L);
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

The ack for this message is available through the subscribed listener. After you received the ack, you can send a 'next query page' type message.
```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        switch (data.getTypeHelper().getMessageDataType()) {
            case QUERY_REQUEST_ACK_11:
                MessageData11QueryRequestAck queryAckData = data.getTypeHelper().asQueryRequestAckMessageData11();
                System.out.println("Query request ack message received with '" + queryAckData.getGlobalStatus() + "' status code");
                //do something with the message...
                try {
                    MessageData12NextQueryPage nextQueryPageData = MessageManager.createMessageData12NextQueryPage(
                            queryAckData.getQueryResponseHolder().getQueryContextHolder(),
                            60_000L);
                    client.sendMessage(nextQueryPageData);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            //...
        }
    }
    @Override
    public void onConnected() {
        System.out.println("Client connected!");
    }
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

#### ATTACHMENT REQUEST
```java
try {
    MessageData data = MessageManager.createMessageData4AttachmentRequest(
            "SELECT * FROM \"events-@attachment\" WHERE id='ATID202001010000000000' and ownerid='EVNT202001010000000000' FOR UPDATE WAIT 86400");
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

The ack for this message is available through the subscribed listener.
The ack may contain the attachment if you also requested the binary attachment.
If not and you requested the binary, the attachment is not yet available and will be sent as an 'attachment response' type message at a later time.
```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        switch (data.getTypeHelper().getMessageDataType()) {
            case ATTACHMENT_REQUEST_ACK_5:
                MessageData5AttachmentRequestAck attachmentRequestAckData = data.getTypeHelper().asAttachmentRequestAckMessageData5();
                System.out.println("Attachment request ack message received with '" + attachmentRequestAckData.getGlobalStatus() + "' status code");
                if(attachmentRequestAckData.getData() != null) {
                    byte[] attachment = attachmentRequestAckData.getData().getResult().getAttachment();
                    if(attachment == null) {
                        //the attachment will be sent as an 'attachment response' type message at a later time if you requested the binary
                    }
                }
                break;
            case ATTACHMENT_RESPONSE_6:
		//if you requested the binary attachment earlier and it was not included in the ack message you received for it
                MessageData6AttachmentResponse attachmentResponseData = data.getTypeHelper().asAttachmentResponseMessageData6();
                try {
                    byte[] attachment = attachmentResponseData.getBinary();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
		//TODO: send an ack for this message
                break;
		//...	
        }
    }
    @Override
    public void onConnected() {
        System.out.println("Client connected!");
    }
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

### AUTOMATIC PUSHING 

```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
       switch (data.getTypeHelper().getMessageDataType()) {
           case ATTACHMENT_RESPONSE_6:
               // ...
               break;
           case EVENT_DOCUMENT_8:
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
```

At the end, we close the websocket connection as well.
```java
client.close();
```
