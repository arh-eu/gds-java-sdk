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
Client client = new Client("ws://127.0.0.1:8080/gate", new ReceivedMessageHandler() {
	@Override
    public void messageReceived(byte[] message) throws IOException {
		try {
			MessageHeader header = MessageManager.getMessageHeaderFromBinaryMessage(message);
            MessageData data = MessageManager.getMessageData(message);
            //do something with the header and data...
        } catch (Throwable throwable) {
			throwable.printStackTrace();
        }
    }
});
```

```java
client.connect();
```

Before sending any message to gds, it is also necessary to send a connection type message. So we will send such a message first. 
This message can be created in the same way as any other (see [How to create messages](##How-to-create-messages)).

An example for creating a connection type message.
```java
MessageHeader connectionMessageHeader = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.CONNECTION_0);
MessageData connectionMessageData = MessageManager.createMessageData0Connection(false, 1, false, null, "pass");
byte[] connectionMessage = MessageManager.createMessage(connectionMessageHeader, connectionMessageData);
```

Now, we can send this message to the GDS through the previously created client object.
```java
client.sendMessage(connectionMessage);
```

Once we have received the response, we can process it as follows, for example.

```java
...
@Override
public void messageReceived(byte[] message) throws IOException {
	try {
		MessageHeader header = MessageManager.getMessageHeaderFromBinaryMessage(message);
        MessageData data = MessageManager.getMessageData(message);
        if(data.getTypeHelper().isConnectionAckMessageData1()) {
			MessageData1ConnectionAck connectionAckMessageData = data.getTypeHelper().asConnectionAckMessageData1();
            //do something woth the connection ack message data
        }
    } catch (Throwable throwable) {
			throwable.printStackTrace();
    }
}
...		
```

After you received a positive acknowledgement for the connection message, you can send any message type. Let's see an event message for example. 
```java
MessageHeader eventMessageHeader = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.CONNECTION_0);

List<String> operationsStringBlock = new ArrayList<String>();
operationsStringBlock.add("INSERT INTO events (id, some_field, images) VALUES('EVNT202001010000000000', 'some_field', array('ATID202001010000000000'));INSERT INTO \"events-@attachment\" (id, meta, data) VALUES('ATID202001010000000000', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
Map<String, byte[]> binaryContentsMapping = new HashMap<>();
binaryContentsMapping.put("62696e6172795f69645f6578616d706c65", new byte[] { 1, 2, 3 });
MessageData eventMessageData = MessageManager.createMessageData2Event(operationsStringBlock, binaryContentsMapping, new ArrayList<PriorityLevelHolder>());

byte[] eventMessage = MessageManager.createMessage(eventMessageHeader, eventMessageData);

client.sendMessage(eventMessage);
```

At the end, we close the websocket connection as well.
```java
client.close();
```