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
        // ...
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

If you use the BinaryMessageListener, you have to create the message objects from the binary representation of the message.
You can create these objects through the hu.arh.gds.message.util.MessageManager class.

```java
client.setBinaryMessageListener(new BinaryMessageListener() {
    @Override
    public void onMessageReceived(byte[] message) {
        // ...
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

Connecting to the GDS.

```java
client.connect();
```

(During the connection, a connection type message is also sent after the websocket connection. If a positive acknowledgment message arrives, the connected() method returns true.)


After you connected, you can send messages to the GDS. You can do that with the sendMessage() methods.

Let's see an event message for example. 
```java
List<String> operationsStringBlock = new ArrayList<String>();
operationsStringBlock.add("INSERT INTO events (id, some_field, images) VALUES('EVNT202001010000000000', 'some_field', array('ATID202001010000000000'));INSERT INTO \"events-@attachment\" (id, meta, data) VALUES('ATID202001010000000000', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
Map<String, byte[]> binaryContentsMapping = new HashMap<>();
binaryContentsMapping.put("62696e6172795f69645f6578616d706c65", new byte[] { 1, 2, 3 });
MessageData data = MessageManager.createMessageData2Event(operationsStringBlock, binaryContentsMapping, new ArrayList<PriorityLevelHolder>());

client.sendMessage(data);
```

Or if you want to define the header part explicitly.
```java
MessageHeader header = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.EVENT_2);

client.sendMessage(header, data);
```

The response is available through the subscribed listener.
```java
client.setMessageListener(new MessageListener() {
	...
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        if(data.getTypeHelper().isEventAckMessageData3()) {
            // do something with the message...
        }
    }
	...
});
```

At the end, we close the websocket connection as well.
```java
client.close();
```