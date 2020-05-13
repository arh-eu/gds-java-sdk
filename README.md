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

## Examples

### Create the Message object

A message consists of two parts, a header and a data.

The following example shows how to create the header part:

```java
MessageHeader header = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.CONNECTION_0);
```
The data part is made in the same way:

```java
MessageData data = MessageManager.createMessageData0Connection(false, 1, false, null, "pass");
```

### Pack the Message (create the binary)

Once you have the header and the data part, the message can be created:

```java
byte[] message = MessageManager.createMessage(header, data);
```

### Unpack the Message (create objects from binary)

To get the header part of the message:

```java
MessageHeader unpackedHeader = MessageManager.getMessageHeaderFromBinaryMessage(message);
```

To get the data part of the message is the same way:

```java
MessageData unpackedData = MessageManager.getMessageData(message);
```

```java
if(unpackedData.getTypeHelper().isConnectionMessageData0()) {
    MessageData0Connection connectionData = unpackedData.getTypeHelper().asConnectionMessageData0();
}
```

### Send and receive messages

The library contains a simple websocket client with basic functionalities.

First, you need to create a client and connect to GDS:

```java
simulator = new Client("ws://127.0.0.1:8080/gate", new ReceivedMessageHandler() {
    @Override
    public void messageReceived(byte[] message) throws IOException {
        //do something with the received message
    }});
    
simulator.connect();
```

After you connected, you can easily send messages:

```java
simulator.sendMessage(message);
```
