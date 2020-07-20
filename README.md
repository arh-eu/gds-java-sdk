# Installation

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

(The library was made by [this](https://github.com/msgpack/msgpack-java) MessagePack Java implementation)

# Usage

Messages can be sent to the GDS via [WebSocket](https://en.wikipedia.org/wiki/WebSocket) protocol. The SDK contains a messages library with serialization/deserialization functionality and with a WebSocket client, so you can use this to send and receive messages.
The SDK also includes a console client that allows you to send and receive messages without writing any code.
You can also find a GDS Server Simulator written in Java [here](https://github.com/arh-eu/gds-server-simulator). With this simulator you can test your client without a real GDS instance. 

- [Console client (high-level usage)](#Console-client)
	- [Arguments](#Arguments)
		- [Options](#Options)
			- [Help](#Help)
			- [URL](#URL)
			- [Username](#Username)
			- [Password](#Password)
            - [Cert](#Cert)
            - [Secret](#Secret)
			- [Timeout](#Timeout)
			- [Hex](#Hex)
			- [Export](#Export)
		- [Commands](#Commands)
			- [EVENT command](#EVENT-command)
			- [ATTACHMENT-REQUEST command](#ATTACHMENT-REQUEST-command)
			- [QUERY command](#QUERY-command)
- [Library (low-level usage)](#Library)
	- [Creating the client](#Creating-the-client)
	- [Subscribing to listeners](#Subscribing-to-listeners)
	- [Connecting to the GDS](#Connecting-to-the-GDS)
	- [Create messages](#Create-messages)
	- [Send and receive messages](#Send-and-receive-messages)
		- [INSERT](#Insert)
		- [UPDATE](#Update)
		- [MERGE](#Merge)
		- [ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE](#ACK-MESSAGE-FOR-THE-INSERT-UPDATE-AND-MERGE)
		- [SELECT](#Select)
			- [QUERY](#Query)
			- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)
		- [AUTOMATIC PUSHING](#AUTOMATIC-PUSHING)
	- [Close the connection](#Close-the-connection)	
	- [Working with custom messages](#Working-with-custom-messages)

## Console client

The console client is used with the SDK executable jar file. 
This jar file can be found in the [Releases](https://github.com/arh-eu/gds-java-sdk/releases) (of course, this jar file can also be created by building the project).

The console client will send the message you specify, and will await for the corresponding ACK messages and print them to your console. 
You can also export the response messages in JSON format and in case of an attachment request you can save the attachments to your local drive.


If you need help about the usage, the syntax can be printed by the -help flag.
```shell
java -jar gds-console-client.jar -help
```

### Arguments

The syntax is as follows:
```shell
java -jar gds-console-client.jar [options] [command] [command options]
```

- [Options](#Options)
	- [Help](#Help)
	- [URL](#URL)
	- [Username](#Username)
	- [Password](#Password)
	- [Cert](#Cert)
	- [Secret](#Secret)
	- [Timeout](#Timeout)
	- [Hex](#Hex)
	- [Export](#Export)
- [Commands](#Commands)
	- [EVENT command](#EVENT-command)
	- [ATTACHMENT-REQUEST command](#ATTACHMENT-REQUEST-command)
	- [QUERY command](#QUERY-command)


#### Options

##### Help

Print the usage to your console. 

##### URL

The URL of the GDS instance you would like to connect to. By default, "ws://127.0.0.1:8888/gate" will be used (this assumes that your local computer has a GDS instance or the server simulator running on the port 8888).

##### Username

The username you would like to use to login to the GDS. By default, "user" will be used.

##### Password

The password you would like to use to login into the GDS. By default there is no authentication.

##### Cert

The name of the file containing your private key that should be used for secure (TLS) connection to the GDS.

##### Secret

The password for the [cert](#Cert) file. 

##### Timeout

The timeout value for the response messages in milliseconds. By default 30000 (30 seconds) will be used. 

##### Hex

Convert strings to hexadecimal. You can enter multiple strings separated by commas.

##### Export

Export all response messages to JSON. The JSON files will be saved in the folder named 'exports' next to the jar file.


#### Commands

##### EVENT command

The INSERT/UPDATE/MERGE statement you would like to use.

With the **-attachments** *command option*, you can enter multiple attachments separated by commas. 
The files must be in the folder named 'attachments' next to the jar file.
Hexadecimal representations of these file names must be referenced in the SQL statement.

INSERT

The following command assumes that there is a folder named 'attachments' next to the jar file with a file named picture.png. 

```shell
java -jar gds-console-client.jar event "INSERT INTO multi_event (id, plate, speed, images) VALUES('TEST2006301005294810', 'ABC123', 90, array('TEST2006301005294740'));INSERT INTO \"multi_event-@attachment\" (id, meta, data) VALUES('TEST2006301005294740', 'some_meta', 0x706963747572652e706e67)" -attachments "picture.png"
```

UPDATE

```shell
java -jar gds-console-client.jar event "UPDATE multi_event SET speed = 100 WHERE id = 'TEST2006301005294810'"
```

MERGE

```shell
java -jar gds-console-client.jar event "MERGE INTO multi_event USING (SELECT 'TEST2006301005294810' as id, 'ABC123' as plate, 110 as speed) I ON (multi_event.id = I.id) WHEN MATCHED THEN UPDATE SET multi_event.speed = I.speed WHEN NOT MATCHED THEN INSERT (id, plate) VALUES (I.id, I.plate)"
```

##### ATTACHMENT-REQUEST command

The SELECT statement you would like to use. This will send an attachment request type message.

```shell
java -jar gds-console-client.jar attachment-request "SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400"
```

The attachment will be saved in the folder named attachments next to the jar file.

##### QUERY command

The SELECT statement you would like to use. This will send a query type message.

```shell
java -jar gds-console-client.jar query "SELECT * FROM multi_event"
```

With the **-query** *command option* you can query all pages, not just the first one.

```shell
java -jar gds-console-client.jar query -all "SELECT * FROM multi_event"
```

## Library

- [Creating the client](#Creating-the-client)
- [Subscribing to listeners](#Subscribing-to-listeners)
- [Connecting to the GDS](#Connecting-to-the-GDS)
- [Create messages](#Create-messages)
- [Send and receive messages](#Send-and-receive-messages)
	- [INSERT](#Insert)
	- [UPDATE](#Update)
	- [MERGE](#Merge)
	- [ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE](#ACK-MESSAGE-FOR-THE-INSERT-UPDATE-AND-MERGE)
	- [SELECT](#Select)
		- [QUERY](#Query)
		- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)
	- [AUTOMATIC PUSHING](#AUTOMATIC-PUSHING)
- [Close the connection](#Close-the-connection)
- [Working with custom messages](#Working-with-custom-messages)

### Creating the client

First, we create the client object.
```java
final Logger logger = Logger.getLogger("logging");

final GDSWebSocketClient client = new GDSWebSocketClient(
        "ws://127.0.0.1:8888/gate", //the URL of the GDS instance you would like to connect to
        "user", //the username you would like to use to login to the GDS
        null, //the password you would like to use to login into the GDS, if null, no authentication will be used
        null, //String - the path of the file containing your private key (*.p12). If the url does not start 'wss', value is ignored.
        null, //String - the password for your private key.
        logger //the logger object
);
```

### Subscribing to listeners

The messages sent to the client and the changes of the connection status can be accessed via listeners.
There are two types of listener for this. One to access the serialized message objects and the other to access the binary representation of the message.

**High-level** (this is the recommended)
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
    public void onConnectionFailed(String s) {
        System.out.println("Connection failed: " + s);
    }
	
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

**Low-level**

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
            //do something with the message...
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onConnected() {
        System.out.println("Client connected!");
    }
	
    @Override
    public void onConnectionFailed(String s) {
        System.out.println("Connection failed: " + s);
    }
	
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

### Connecting to the GDS

```java
client.connect();
```

During the connection, a connection type message is also sent after the websocket connection established. 
If a positive ack message arrives, the client will in connected state, the onConnected() listener triggers and the client.connected() method returns true.
After that, you can send any type of messages to the GDS.

### Create messages

A message consists of two parts, a header and a data.
With this SDK , it is usually enough to create only the data part, because the header part is created automatically when the message is sent.
Of course, you can also customize the header part, see [Working with custom messages](#Working-with-custom-messages) for the details.

Let's see how to create an attachment request type message data.

```java
MessageData data  = MessageManager.createMessageData4AttachmentRequest("SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400");
```


### Send and receive messages

After you connected, you can send messages to the GDS. You can do that with the client.sendMessage() methods.

Send message with the data part. The header part is completed automatically with the default values.
```java
void sendMessage(MessageData data)
```

Send message with the data part (the header part is completed automatically with the default values), but without automatic generation of message id.
```java
void sendMessage(MessageData data, String messageId)
```

(The automatic header part completition means, that there is no fragmentation set, and the user name and the message data type are determined automatically.)

To see how to send a message by specifying the header part too, go to the [Working with custom messages](#Working-with-custom-messages) section.

In the following, take a look at what sending and receiving messages look like for different message types.

- [INSERT](#Insert)
- [UPDATE](#Update)
- [MERGE](#Merge)
- [ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE](#ACK-MESSAGE-FOR-THE-INSERT-UPDATE-AND-MERGE)
- [SELECT](#Select)
	- [QUERY](#Query)
	- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)
- [AUTOMATIC PUSHING](#AUTOMATIC-PUSHING)

#### INSERT
```java
MessageIdGenerator messageIdGenerator = new MessageIdGenerator("TEST", "yyMMddhhmmssSSS");
String eventId = messageIdGenerator.nextId();
String attachmentId = messageIdGenerator.nextId();

int[] binaryData = {
        0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x0d,
        0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x09,
        0x08, 0x06, 0x00, 0x00, 0x00, 0xe0, 0x91, 0x06, 0x10, 0x00, 0x00, 0x00,
        0x01, 0x73, 0x52, 0x47, 0x42, 0x00, 0xae, 0xce, 0x1c, 0xe9, 0x00, 0x00,
        0x00, 0x04, 0x67, 0x41, 0x4d, 0x41, 0x00, 0x00, 0xb1, 0x8f, 0x0b, 0xfc,
        0x61, 0x05, 0x00, 0x00, 0x00, 0x09, 0x70, 0x48, 0x59, 0x73, 0x00, 0x00,
        0x0e, 0xc3, 0x00, 0x00, 0x0e, 0xc3, 0x01, 0xc7, 0x6f, 0xa8, 0x64, 0x00,
        0x00, 0x00, 0x2d, 0x49, 0x44, 0x41, 0x54, 0x28, 0x53, 0x63, 0xf8, 0x4f,
        0x04, 0x20, 0x4d, 0xd1, 0x5b, 0x19, 0x15, 0x30, 0x46, 0x67, 0x83, 0x00,
        0x69, 0x8a, 0xf0, 0x01, 0xe2, 0x15, 0x21, 0x1b, 0x8d, 0x0c, 0x60, 0xe2,
        0x18, 0x6e, 0x42, 0xc6, 0x30, 0x40, 0x84, 0x75, 0xff, 0xff, 0x03, 0x00,
        0x18, 0xd8, 0x27, 0x9c, 0x9f, 0xb7, 0xe9, 0xa0, 0x00, 0x00, 0x00, 0x00,
        0x49, 0x45, 0x4e, 0x44, 0xae, 0x42, 0x60, 0x82
};
ByteArrayOutputStream baos = new ByteArrayOutputStream();
DataOutputStream dos = new DataOutputStream(baos);
for (int pixel : binaryData) {
    dos.writeByte(pixel);
}
byte[] byteArray = baos.toByteArray();
try {
    MessageData data = MessageManager.createMessageData2Event(
            new ArrayList<String>() {{
                add("INSERT INTO multi_event (id, plate, speed, images) VALUES('" + eventId + "', 'ABC123', 90, array('" + attachmentId +"'))");
                add("INSERT INTO \"multi_event-@attachment\" (id, meta, data) VALUES('" + attachmentId + "', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
            }},
            new HashMap<String, byte[]>() {{
                put("binary_id1_example", byteArray);
            }},
            new ArrayList<>());
    client.sendMessage(data);
} catch (Throwable e) {
	e.printStackTrace();
}
```

#### UPDATE
```java
try {
    MessageData data = MessageManager.createMessageData2Event(
            new ArrayList<String>() {{
                add("UPDATE multi_event SET speed = 100 WHERE id = 'TEST2006301005294810'");
            }},
            new HashMap<>(),
            new ArrayList<>());
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

#### MERGE
```java
try {
    MessageData data = MessageManager.createMessageData2Event(
            new ArrayList<String>() {{
                add("MERGE INTO multi_event USING (SELECT 'TEST2006301005294810' as id, 'ABC123' as plate, 100 as speed) I " +
                        "ON (multi_event.id = I.id) " +
                        "WHEN MATCHED THEN UPDATE SET multi_event.speed = I.speed " +
                        "WHEN NOT MATCHED THEN INSERT (id, plate) VALUES (I.id, I.plate)");
            }},
            new HashMap<>(),
            new ArrayList<>());
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

#### ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE

The ack for these messages is available through the subscribed listener.
```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        switch (data.getTypeHelper().getMessageDataType()) {
            case EVENT_ACK_3:
                MessageData3EventAck eventAckData = data.getTypeHelper().asEventAckMessageData3();
                System.out.println("Event ACK message received with '" + eventAckData.getGlobalStatus() + "' status code")
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
    public void onConnectionFailed(String s) {
        System.out.println("Connection failed: " + s);
    }
	
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

#### SELECT

- [QUERY](#QUERY)
- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)

##### QUERY

```java
try {
    MessageData data = MessageManager.createMessageData10QueryRequest(
            "SELECT * FROM events",
            ConsistencyType.PAGES,
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
                //...
                //send a 'next query page' type message
                try {
                    MessageData12NextQueryPage nextQueryPageData = MessageManager.createMessageData12NextQueryPage(
                            queryAckData.getQueryResponseHolder().getQueryContextHolder(),
                            60_000L);
                    client.sendMessage(nextQueryPageData, header.getTypeHelper().asBaseMessageHeader().getMessageId());
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
    public void onConnectionFailed(String s) {
        System.out.println("Connection failed: " + s);
    }
	
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

##### ATTACHMENT REQUEST
```java
try {
    MessageData data = MessageManager.createMessageData4AttachmentRequest(
            "SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400");
    client.sendMessage(data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

The ack for this message is available through the subscribed listener.
The ack may contain the attachment if you also requested the binary attachment.
If not contains and you requested the binary, the attachment is not yet available and will be sent as an 'attachment response' type message at a later time.
```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        switch (data.getTypeHelper().getMessageDataType()) {
            case ATTACHMENT_REQUEST_ACK_5:
                MessageData5AttachmentRequestAck attachmentRequestAckData = data.getTypeHelper().asAttachmentRequestAckMessageData5();
                System.out.println("Attachment request ack message received with '" + attachmentRequestAckData.getGlobalStatus() + "' status code");
                if (attachmentRequestAckData.getData() != null) {
                    byte[] attachment = attachmentRequestAckData.getData().getResult().getAttachment();
                    if (attachment == null) {
                        //if you requested the binary the attachment will be sent as an 'attachment response' type message at a later time
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
                //do something with the message...
                //...
                //send an ack message for the attachment response
                try {
                    MessageData7AttachmentResponseAck attachmentResponseAckData = MessageManager.createMessageData7AttachmentResponseAck(
                            AckStatus.OK,
                            new AttachmentResponseAckResultHolderImpl(
                                    AckStatus.CREATED,
                                    new AttachmentResultHolderImpl(
                                            attachmentResponseData.getResult().getRequestIds(),
                                            attachmentResponseData.getResult().getOwnerTable(),
                                            attachmentResponseData.getResult().getAttachmentId()
                                    )
                            ),
                            null
                    );
                    client.sendMessage(attachmentResponseAckData, header.getTypeHelper().asBaseMessageHeader().getMessageId());
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
    public void onConnectionFailed(String s) {
        System.out.println("Connection failed: " + s);
    }
	
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

Note: the GDS may also send an attachment request to the client.


#### AUTOMATIC PUSHING 

A user may be interested in data or changes in specific data. 
The criteria system, based on which data may be of interest to the user, is included in the configuration of the delivered system. 
This data is sent automatically by the GDS.

```java
client.setMessageListener(new MessageListener() {
    @Override
    public void onMessageReceived(MessageHeader header, MessageData data) {
        switch (data.getTypeHelper().getMessageDataType()) {
            case ATTACHMENT_RESPONSE_6:
                MessageData6AttachmentResponse attachmentResponseData = data.getTypeHelper().asAttachmentResponseMessageData6();
                System.out.println("Attachment response message received");
                //do something with the message...
                //...
                //send an ack message for the attachment response
                try {
                    MessageData7AttachmentResponseAck attachmentResponseAckData = MessageManager.createMessageData7AttachmentResponseAck(
                            AckStatus.OK,
                            new AttachmentResponseAckResultHolderImpl(
                                    AckStatus.CREATED,
                                    new AttachmentResultHolderImpl(
                                            attachmentResponseData.getResult().getRequestIds(),
                                            attachmentResponseData.getResult().getOwnerTable(),
                                            attachmentResponseData.getResult().getAttachmentId()
                                    )
                            ),
                            null
                    );
                    client.sendMessage(attachmentResponseAckData, header.getTypeHelper().asBaseMessageHeader().getMessageId());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case EVENT_DOCUMENT_8:
                MessageData8EventDocument eventDocumentData = data.getTypeHelper().asEventDocumentMessageData8();
                System.out.println("Event document message received");
                //do something with the message...
                //...
                //send an ack message for the event document message
                try {
                    MessageData9EventDocumentAck eventDocumentAckData = MessageManager.createMessageMessageData9EventDocumentAck(
                            AckStatus.OK,
                            new ArrayList<EventDocumentResultHolder>(){{
                                add(new EventDocumentResultHolderImpl(
                                        AckStatus.CREATED,
                                        null,
                                        new HashMap<>()
                                ));
                            }},
                            null
                    );
                    client.sendMessage(eventDocumentAckData, header.getTypeHelper().asBaseMessageHeader().getMessageId());
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
    public void onConnectionFailed(String s) {
        System.out.println("Connection failed: " + s);
    }
	
    @Override
    public void onDisconnected() {
        System.out.println("Client disconnected!");
    }
});
```

### Close the connection

```java
client.close();
```

### Working with custom messages

A message consists of two parts, a header and a data. 
With this SDK, it is usually enough to create only the data part because the header part is created automatically when the message is sent using the ```sendMessage(MessageData data)``` method.
But it is also possible to explicitly define the header part with customized values.

So first, we create the header part.

```java
MessageHeader header = MessageManager.createMessageHeaderBase("username", UUID.randomUUID().toString(), System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.ATTACHMENT_REQUEST_4);
```

After that, we create the data part.
```java
MessageData data  = MessageManager.createMessageData4AttachmentRequest("SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400");
```

Once we have a header and a data, we can use the following methods to send the message.


Send message with the header and data part.
```java
void sendMessage(MessageHeader header, MessageData data)
```

```java
try {	
    client.sendMessage(header, data);
} catch (Throwable e) {
    e.printStackTrace();
}
```

Send message with the binary representation of the message.
```java
void sendMessage(byte[] message)
```

```java
try {
    byte[] message = MessageManager.createMessage(header, data);
    client.sendMessage(message);
} catch (Throwable e) {
    e.printStackTrace();
}
```
