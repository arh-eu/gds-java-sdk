# Installation

With [JitPack](https://jitpack.io/), you can easily add this project as a maven dependency:

```XML
<project>
    <!--
        any additional info for your maven project.
        To add this SDK, you have to add the JitPack repository,
        and the GDS Java messages as dependency.
    -->
    <repositories>
        <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>com.github.arh-eu</groupId>
            <artifactId>gds-java-sdk</artifactId>
            <version>master-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

JitPack gives you a flexible, virtual maven repository which can work with github projects.

The `<groupId>` stands for the github user (in this case, that is `arh-eu`), the `<artifactId>` gives the required project (`gds-java-sdk`), while the `<version>` will indicate which release tag or commit-state that will be used. You can use the latest release of this - `1.3` (or if you want to keep up with the updates - `master-SNAPSHOT`).

However, if you need to use an earlier version, you can specify them as well - see releases for more info.

If you want to download this and install it manually in your local repository without using JitPack, you should use the following dependency after installation:
```XML
<dependency>
    <groupId>com.arh-eu</groupId>
    <artifactId>gds-java-sdk</artifactId>
    <version>1.3-SNAPSHOT</version>
</dependency>
```

(The library was made using [this](https://github.com/msgpack/msgpack-java) MessagePack Java implementation.)

# Usage

Messages can be sent to the GDS via [WebSocket](https://en.wikipedia.org/wiki/WebSocket) protocol. The SDK contains a messages library with serialization/deserialization functionality and with a WebSocket client, so you can use this to send and receive messages.
The SDK also includes a console client that allows you to send and receive messages without writing any code.
You can also find a GDS Server Simulator written in Java [here](https://github.com/arh-eu/gds-server-simulator). With this simulator you can test your client without a real GDS instance. 

  * [Console client](#console-client)
    + [Arguments](#arguments)
      - [Options](#options)
        * [Help](#help)
        * [URL](#url)
        * [Username](#username)
        * [Password](#password)
        * [Cert, secret](#cert-secret)
        * [Timeout](#timeout)
        * [Hex](#hex)
        * [Export](#export)
      - [Commands](#commands)
        * [EVENT command](#event-command)
        * [ATTACHMENT-REQUEST command](#attachment-request-command)
        * [QUERY command](#query-command)
  * [Library](#library)
    + [Creating the client](#creating-the-client)
      - [Client Factory](#client-factory)
      - [Constructor and value restrictions](#constructor-and-value-restrictions)
    + [Subscribing with the listener](#subscribing-with-the-listener)
    + [Connecting to the GDS](#connecting-to-the-gds)
    + [Create messages](#create-messages)
    + [Send and receive messages](#send-and-receive-messages)
      - [INSERT](#insert)
      - [UPDATE](#update)
      - [MERGE](#merge)
      - [ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE](#ack-message-for-the-insert-update-and-merge)
      - [SELECT](#select)
        * [QUERY](#query)
        * [ATTACHMENT REQUEST](#attachment-request)
      - [AUTOMATIC PUSHING](#automatic-pushing)
    + [Close the connection](#close-the-connection)
    + [Reusing the client](#reusing-the-client)
    + [Thread-safety](#thread-safety)
    + [Working with custom messages](#working-with-custom-messages)
    + [Full example](#Async-client-example)
  * [Synchronous Client](#synchronous-client)
    + [Client creation](#client-creation)
    + [Methods](#methods)
    + [Connecting](#connecting)
    + [Closing](#closing)
    + [Sending (sync) messages](#sending-sync-messages)
    + [Full example](#Sync-client-example)

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

 - [Options](#options)
    * [Help](#help)
    * [URL](#url)
    * [Username](#username)
    * [Password](#password)
    * [Cert, secret](#cert--secret)
    * [Timeout](#timeout)
    * [Hex](#hex)
    * [Export](#export)
  - [Commands](#commands)
    * [EVENT command](#event-command)
    * [ATTACHMENT-REQUEST command](#attachment-request-command)
    * [QUERY command](#query-command)


#### Options

##### Help

Print the usage to your console.

```shell
java -jar gds-console-client.jar -help
```

##### URL

The URL of the GDS instance you would like to connect to. By default, "`ws://127.0.0.1:8888/gate`" will be used (this assumes that your local computer has a GDS instance or the server simulator running on the port 8888).

```shell
java -jar gds-console-client.jar -url "ws://192.168.222.111:9999/gate" query "SELECT * FROM multi_event"
```

##### Username

The username you would like to use to login to the GDS. By default, `"user"` will be used.

```shell
java -jar gds-console-client.jar -username "some_other_user" query "SELECT * FROM multi_event"
```

##### Password

The password you would like to use to login into the GDS. By default there is no authentication. Usually you want to specify the username as well!

```shell
java -jar gds-console-client.jar -username "some_other_user" -password "userpassword" query "SELECT * FROM multi_event"
```

##### Cert, secret

The name of the file containing the certificate chain and your private key that should be used for secure (TLS) connection to the GDS should be given by the `-cert` option (it should be in PKCS12 format - a `*.p12` file). TLS will only be used if the scheme in the URL is `wss`, therefore the url should be specified as well (the GDS uses different port (and endpoint) for default and for encrypted connection).

The password that was used to generate and encrypt the cert file should be given by the `-secret` flag. 

```shell
java -jar gds-console-client.jar -url "wss://192.168.222.111:8443/gates" -cert "my_cert_file.p12" -secret "password_for_cert" query "SELECT * FROM multi_event"
```

##### Timeout

The timeout value for the response messages in milliseconds. By default 30000 (30 seconds) will be used.

```shell
java -jar gds-console-client.jar -timeout 10000 query "SELECT * FROM multi_event"
```

##### Hex

Convert strings to hexadecimal. You can enter multiple strings separated by commas (`,`). The client will print the results without any connection to a GDS.


```shell
java -jar gds-console-client.jar -hex "picture1.jpg,image2.png"
picture1.jpg = 0x70696374757265312e6a7067
image2.png = 0x696d616765322e706e67
```

##### Export

Export all response messages to JSON. The JSON files will be saved in the folder named 'exports' next to the jar file.


```shell
java -jar gds-console-client.jar -export query "SELECT * FROM multi_event"
```

#### Commands

##### EVENT command

The INSERT/UPDATE/MERGE statement you would like to use.

With the **-attachments** *command option*, you can enter multiple attachments separated by commas (`,`). 
The files must be in the folder named `attachments` next to the jar file.
Hexadecimal representations of these file names must be referenced in the SQL statement.

About hex:

The attachments you specify are stored in a different table in the GDS than the event's data (to increase performance, and one attachment might be used for multiple events).
To create a connection between the two we have to reference the attachment ID in your event record. The attachment itself can have multiple fields connected to it (like meta descriptors). The binary part of the attachment usually cannot be inserted into a query easily, therefore a unique ID is used in the SQL string to resolve this issue.
 This is usually generated from the attachment's filename, but you can use any name you want. Because of how things are stored in the background we have to use hexadecimal format for these IDs (with the `0x` prefix), thus it leads to converting the filename into a hex format (conversion can be done by the `-hex` option, see it [above](#Hex)).
 
 This is the reason our SQL for the attachment part will have these hexes in them.
 
 The binaries themselves are sent with the event data, in a map (associative array), where the keys are these IDs, and the values are the binary data themselves (`Map<String, byte[]>`).
 
 These binaries are generated from the files you specify with the `-attachments` flag by the client itself.

This makes the event statements use two tables, named `multi_event` and `multi_event-@attachment`. The event data will be inserted into the `multi_event`, the binary attachment into the `multi_event-@attachments`.

INSERT

The following command assumes that there is a folder named 'attachments' next to the jar file with a file named `picture.png`. 

```shell
java -jar gds-console-client.jar event "INSERT INTO multi_event (id, plate, speed, images) VALUES('TEST2006301005294810', 'ABC123', 90, array('TEST2006301005294740'));INSERT INTO \"multi_event-@attachment\" (id, meta, data) VALUES('TEST2006301005294740', 'image/png', 0x706963747572652e706e67)" -attachments "picture.png"
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

With the **-all** *command option* you can query all pages, not just the first one.

```shell
java -jar gds-console-client.jar query -all "SELECT * FROM multi_event"
```

## Library

 * [Library](#library)
    + [Creating the client](#creating-the-client)
      - [Client Factory](#client-factory)
      - [Constructor and value restrictions](#constructor-and-value-restrictions)
    + [Subscribing with the listener](#subscribing-with-the-listener)
    + [Connecting to the GDS](#connecting-to-the-gds)
    + [Create messages](#create-messages)
    + [Send and receive messages](#send-and-receive-messages)
      - [INSERT](#insert)
      - [UPDATE](#update)
      - [MERGE](#merge)
      - [ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE](#ack-message-for-the-insert-update-and-merge)
      - [SELECT](#select)
        * [QUERY](#query)
        * [ATTACHMENT REQUEST](#attachment-request)
      - [AUTOMATIC PUSHING](#automatic-pushing)
    + [Close the connection](#close-the-connection)
    + [Reusing the client](#reusing-the-client)
    + [Thread-safety](#thread-safety)
    + [Working with custom messages](#working-with-custom-messages)
    + [Full example](#Async-client-example)

### Creating the client

#### Client Factory

First, we create the client object. To make things easier, a `AsyncGDSClientBuilder` class is available as a static class nested in the `AsyncGDSClient` class, making the creation of the client easier.

This (and the listener interface) can be found in the `hu.arh.gds.client` package.
 
 The factory class provides multiple methods to set the initial parameters for the client you wish to create, this way you dont have to specify every value in the constructor. The methods can be chained, as they all return the builder instance. The methods found in the factory are the following (for value restrictions see the constructor below):
   - `withListener(GDSMessageListener listener)` - sets the callback listener to the given value.
   - `withLogger(Logger logger)` - sets the `Logger` instance used for logging messages.
   - `withURI(String URI)` - sets the GDS URI.
   - `withUserName(String userName)` - sets the username used in the GDS communication.
   - `withUserPassword(String userPassword)` - sets the password used for _password authentication_.
   - `withTimeout(long timeout)` - sets the timeout (in milliseconds) for the login procedure. If you do not specify this, the value will be set to `3000` (ms).
   - `withTLS(InputStream cert, String secret)` -sets the credentials from a PKCS12 formatted cert (file) used for connecting via TLS. This method has an overload for `(String cert,String secret)` parameters, in this case the cert first parameter be used as a path to a file containing the certification.
   - `build()` - creates the `AsyncGDSClient` instance.
 
 #### Constructor and value restrictions
 
 If you want to avoid using the builder, you can invoke the constructor directly, which has the following signature:
 ```java
public AsyncGDSClient(
    String URI,
    String userName,
    String userPassword,
    long timeout,
    Logger log,
    GDSMessageListener listener,
    SslContext sslCtx) {
        //...
    }
```
 
 As mentioned above, restrictions to some of these parameters apply:
 
  - `URI` - cannot be `null`, and has to represent a valid URI to the GDS. Since the connection is established through the WebSocket protocol, the URI scheme must start with either `ws` or `wss` to be accepted.
  - `userName` - the username cannot be null or set to an empty string (or a string containing only whitespaces).
  - `userPassword` - if the user wishes to use _password authentication_, this will be used. Otherwise, the value should be set to `null`. 
  - `timeout` - the timeout must be a positive number, representing the maximum wait time (in milliseconds) before the client drops the connection and login request and raises an exception if it does not arrive (or if the GDS is unavailable).
  - `log` - the Logger instance used to track and debug the client. if the value is `null`, a default one will be created with the name `"AsyncGDSClient"` and the log level set to `SEVERE`. Otherwise, the given one will be used. The default log will use the standard error (`System.err`) as its output stream. The log format will be the following: `[2020-10-19 08:15:39] [SEVERE] | hu.arh.gds.client.AsyncGDSClient::methodName | Some error message, that will be in the log.`
  - `listener` - the `GDSMessageListener` instance used for callbacks. Value cannot be `null`.
  - `sslCtx` - the SSLContext used to setup the TLS for the client. If TLS is not used, the value should be set to `null`.
  The context can be created via the static `AsyncGDSClient.createSSLContext(..)` method.
 
 
```java
String URI = "ws://127.0.0.1:8888/gate";
String USERNAME = "user";
GDSMessageListener listener = new GDSMessageListener();

AsyncGDSClient client = AsyncGDSClient.getBuilder()
        .withURI(URI)
        .withUserName(USERNAME)
        .withListener(listener).build();
```

### Subscribing with the listener

The messages sent to the client and the changes of the connection status can be accessed via listeners.

The listener you pass will be used and called without any changes by the WebSocketClient after you connect successfully. Since login message is sent automatically; when the connection is ready, the `ConnectionACK` message will processed before any of your handler method is called with the data (but it will be called with the login ACK as well).

The `WebSocketClient` uses `netty`'s `NioEventLoop` in the background, therefore the communication is asynchronous. The `Channel` object, that is used by netty will be passed in the main methods.

The `GDSMessageListener` interface has methods that do not have to be overridden, and others that _should be_. The signature for these methods can be seen below.

 - `onConnectionSuccess(..)` this method is called when the WebSocket connection is established, and the ACK for the login message is received with an OK status (`AckStatus.OK - code 200`). The received header and login body is passed to the method. By default, this method does not do anything, therefore it does not _need_ to be overridden (but it _should be_, as this indicates that your client is ready to be used).
 - `onConnectionFailure(..)` if the connection or the login fails for any reason (network error, error in the login credentials or timeout) this method will be called. The `Either` class indicates that it is not predetermined which value will be set, so the error can be _either_ a `Throwable` or a `Pair` of header and connection ACK message. the `isLeftSet() / isRightSet()` and `getLeft() / getRight()` methods can be used to access the proper value. If you do not override this, the method will throw an `AbstractMethodError` if invoked (otherwise if the connection is established, it will never be called).
  - `onDisconnect(..)` when your connection to the GDS is closed this handler will be called. If the connection (and login) is not successful, the `onConnectionFailure(..)` method will be invoked, not this. Otherwise, if the GDS or the client closes the connection for any reason, this callback will be used. By default, this does not do anything.
  
  The callbacks for the actual messages will always receive the `MessageHeaderBase` and the appropriate `MessageData` sent by the GDS. Since your client might only send (and receive) one kind of message (ie. you're only using your client for querying data, but never insert or update), the listener does not require you to override its methods for every possible type. This can make your client more compact, without having to worry about overriding methods that will never be invoked.
  
  However, if you receive any of those but you do not implement your logic for it, the listener will throw an `AbstractMethodError` by default.

```java
public interface GDSMessageListener {
    default void onConnectionSuccess(Channel ch, MessageHeaderBase header, MessageData1ConnectionAck response) {

    }
    
    default void onConnectionFailure(Channel channel, Either<Throwable, Pair<MessageHeaderBase, MessageData1ConnectionAck>> reason) {
            throw new AbstractMethodError();
    }
    
   default void onDisconnect(Channel channel) {

    }
    
    default void onEventAck3(MessageHeaderBase header, MessageData3EventAck response) {
        throw new AbstractMethodError();
    }

    default void onAttachmentRequest4(MessageHeaderBase header, MessageData4AttachmentRequest request) {
        throw new AbstractMethodError();
    }

    default void onAttachmentRequestAck5(MessageHeaderBase header, MessageData5AttachmentRequestAck requestAck) {
        throw new AbstractMethodError();
    }

    default void onAttachmentResponse6(MessageHeaderBase header, MessageData6AttachmentResponse response) {
        throw new AbstractMethodError();
    }

    default void onAttachmentResponseAck7(MessageHeaderBase header, MessageData7AttachmentResponseAck responseAck) {
        throw new AbstractMethodError();
    }
    
    default void onEventDocument8(MessageHeaderBase header, MessageData8EventDocument eventDocument) {
        throw new AbstractMethodError();
    }

    default void onEventDocumentAck9(MessageHeaderBase header, MessageData9EventDocumentAck eventDocumentAck) {
        throw new AbstractMethodError();
    }

    default void onQueryRequestAck11(MessageHeaderBase header, MessageData11QueryRequestAck response) {
        throw new AbstractMethodError();
    }
}
```

You probably want to reference the client from the listener, so you can send messages when they arrive (ie. a `NextQueryPage` request). There are multiple ways to do this, one is through a final `AtomicReference`.

```java
 final AtomicReference<AsyncGDSClient> clientReference = new AtomicReference<>();
AsyncGDSClient client = AsyncGDSClient.getBuilder()
    .withURI(URI)
    .withUserName(USERNAME)
    .withListener(new GDSMessageListener() {

            @Override
            public void onQueryRequestAck11(MessageHeaderBase header, MessageData11QueryRequestAck response) {
                if (response.getQueryResponseHolder().getMorePage()) {
                    try {
                        clientReference.get().sendNextQueryPage12(MessageManager.createMessageData12NextQueryPage(response.getQueryResponseHolder().getQueryContextHolder(), 10000L));
                    } catch (IOException | ValidationException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Received " + response.getQueryResponseHolder().getHits().size() + " records..");
                //do whatever else you want with this.
            }
        }).build();

clientReference.set(client);


client.connect();

```

### Connecting to the GDS

Connecting to the GDS after you set up your client is a simple method invocation:

```java
client.connect();
```

During the connection, a login message is automatically sent after the websocket connection is established. Please keep in mind that the `connect()` method will not block the current (main) thread. If the given timeout occurs or any error happens during the connection or the login, you will be notified on the listener you specified and not here.

If a positive ACK message arrives, the client will be in connected state, the `onConnectionSuccess(..)` listener triggers and the `client.isConnected()` method will return `true`. The client state can be always checked by the `getState()` method.

The possible status values are:
```java
public enum ConnectionState {
    NOT_CONNECTED, //The client is instantiated but connect() was not yet called.
    INITIALIZING,  //The connect() was called, the underlying Netty channels are being created
    CONNECTING, // Netty initialized, trying to establish the TCP/WebSocket Connection
    CONNECTED, //The connection is established
    LOGGING_IN, //The login message was successfully sent
    LOGGED_IN, //The login was successful. Client is ready to use
    DISCONNECTED, //the connection was closed after a successful login.
    FAILED //error happened during the initialization. 
}
```

After that, you can send any type of messages to the GDS.

Please keep in mind that you should not send any messages until you have received the (positive) ACK for your login, otherwise the GDS will drop your connection as the authentication and authorization processes did not finish yet but your client is trying to send messages (which is invalid without a positive login ACK).

### Create messages

A message consists of two parts, a header and a data.
With this SDK, it is usually enough to create only the data part, because the header part is created automatically when the message is sent.

Let's see how to create an attachment request type message data.

To create the messages you can use the `MessageManager` class, which provides many overloads for the types you want to create and includes automatic validation for the values you specified.

If any of the values violate the rules of the specified message (ie. if the `isFragmented` flag is set to `false`, the `firstFragment` should be set to `null` and so), the `MessageManager` will throw a (checked) `ValidationException`.

As the messages are serialized into `MessagePack` packets, it is possible that something fails while the message is serialized (which is invoked automatically upon construction). Therefore an `IOException` can also raise during creation.

```java
try {
    MessageData4AttachmentRequest data  = MessageManager.createMessageData4AttachmentRequest("SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400");
} catch (ValidationException | IOException e) {
    //your message could not be created for some reason
}
```

These `try-catch` blocks will not be included for most of the examples in the rest of this guide to make reading easier.

### Send and receive messages

After you connected, you can send your messages to the GDS. The method names used for sending always contain the types of messages you are about to send to lead you. For example, sending attachment requests can be done by invoking the `sendAttachmentRequest4(..)` methods. The `4` in the name stands for the internal type of the message.

Methods used for sending always contain multiple overloads (based on what values can be passed as their data), but they share these three overloads across the types:

 - One with the the appropriate `MessageData`, using default values for the header with a randomly generated message ID.
 - One with a `String` and the `MessageData`, which sets the message ID to the specified string value.
 - One with a `MessageHeader` and a `MessageData`, allowing you to fully customise the message you want to send. 

This translates for the `AttachmentRequest` as the following:
 - `sendAttachmentRequest4(MessageData4AttachmentRequest request);`
 - `sendAttachmentRequest4(String messageID, MessageData4AttachmentRequest request);`
 - `sendAttachmentRequest4(MessageHeaderBase header, MessageData4AttachmentRequest request);`
 
As `AttachmentRequest` messages need a `String` parameter to describe their query, there is also an overload to accept a single string parameter: 
 - `sendAttachmentRequest4(String request);`


If any of the values passed to these methods violate the rules of the specified message (ie. if you create your own header, and set the `isFragmented` flag to `true`, but leave the `firstFragment` as `null`), the `MessageManager`, and therefore the `send..()` will throw a (checked) `ValidationException`.

As the messages are serialized into `MessagePack` packets, it is possible that something fails while the message is serialized (which is invoked automatically upon construction). Therefore an `IOException` can also raise during creation.


This means that this code can raise these exceptions without sending anything while trying to create the messages:
```java
try {
    client.sendAttachmentRequest4("SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400");
} catch (ValidationException | IOException e) {
    //your message could not be created for some reason
}
```

For the rest of our guides, the `try-catch` blocks around the creation and sending will be omitted so they are easier to read, but you should _not_ forget them.
For the rest of our guides, the `try-catch` blocks around the creation and sending will be omitted so they are easier to read, but you should not forget them.

The default header means that there is no fragmentation set, the creation and request times are set to the current system time. The message data type is determined automatically by the method you call.

To see how to send a message by specifying the header part too, go to the [Working with custom messages](#Working-with-custom-messages) section.

In the following, take a look at what sending and receiving messages look like for different message types.

- [INSERT](#INSERT)
- [UPDATE](#UPDATE)
- [MERGE](#MERGE)
- [ACK message for the INSERT, UPDATE and MERGE](#ACK-MESSAGE-FOR-THE-INSERT-UPDATE-AND-MERGE)
- [SELECT](#Select)
	- [QUERY](#Query)
	- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)
- [Automatic pushing](#AUTOMATIC-PUSHING)


You might want to read the hex part from the [EVENT command](#event-command) to understand why and how we use these in the attachments.

The hex values for a string can be retrieved via the `hu.arh.gds.message.util.Utils` class, by a `static` method with a signature of `String stringToUTF8Hex(String)`.

#### INSERT
```java
MessageIdGenerator messageIdGenerator = new MessageIdGenerator("TEST", "yyMMddhhmmssSSS");
String eventId = messageIdGenerator.nextId();
String attachmentId = messageIdGenerator.nextId();


// you can create your binary any way you want.
// this simply uses a series of predetermined bytes / pixels that will result 
// in a valid 9x9 PNG image

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

MessageData data = MessageManager.createMessageData2Event(
        new ArrayList<String>() {{
            add("INSERT INTO multi_event (id, plate, speed, images) VALUES('" + eventId + "', 'ABC123', 90, array('" + attachmentId +"'))");
            add("INSERT INTO \"multi_event-@attachment\" (id, meta, data) VALUES('" + attachmentId + "', 'some_meta', 0x62696e6172795f6964315f6578616d706c65)");
        }},
        new HashMap<String, byte[]>() {{
            put("binary_id1_example", byteArray);
        }},
        new ArrayList<>());
client.sendEvent2(data);
```

#### UPDATE
```java
MessageData data = MessageManager.createMessageData2Event(
        new ArrayList<String>() {{
            add("UPDATE multi_event SET speed = 100 WHERE id = 'TEST2006301005294810'");
        }},
        new HashMap<>(),
        new ArrayList<>());

client.sendEvent2(data);

```

#### MERGE
```java
MessageData data = MessageManager.createMessageData2Event(
        new ArrayList<String>() {{
            add("MERGE INTO multi_event USING (SELECT 'TEST2006301005294810' as id, 'ABC123' as plate, 100 as speed) I " +
                    "ON (multi_event.id = I.id) " +
                    "WHEN MATCHED THEN UPDATE SET multi_event.speed = I.speed " +
                    "WHEN NOT MATCHED THEN INSERT (id, plate) VALUES (I.id, I.plate)");
        }},
        new HashMap<>(),
        new ArrayList<>());

client.sendEvent2(data);
    
```

#### ACK MESSAGE FOR THE INSERT, UPDATE AND MERGE

The ack for these messages is available through the subscribed listener.
```java
GDSMessageListener listener = new GDSMessageListener() {
    
    // ... rest of the overrides as you want 
    
    @Override
    void onEventAck3(MessageHeaderBase header, MessageData3EventAck response) {
            System.out.println("Event ACK message received with '" + response.getGlobalStatus() + "' status code");
    }  
};
```

#### SELECT

- [QUERY](#QUERY)
- [ATTACHMENT REQUEST](#ATTACHMENT-REQUEST)

##### QUERY

```java
MessageData10QueryRequest data = MessageManager.createMessageData10QueryRequest(
        "SELECT * FROM multi_event",
        ConsistencyType.NONE,
        60_000L);

client.sendQueryRequest10(data);
```

The ack for this message is available through the subscribed listener. After you received the ack, you can send a 'next query page' type message. (To see how the `clientReference`is created or why is it used, check the [Subscribing with a listener](#Subscribing-with-a-listener) part.)
```java
GDSMessageListener listener = new GDSMessageListener() {

    // ... rest of the overrides as you want
     
    @Override
    public void onQueryRequestAck11(MessageHeaderBase header, MessageData11QueryRequestAck response) {
        if (response.getQueryResponseHolder().getMorePage()) {
            try {
                clientReference.get().sendNextQueryPage12(MessageManager.createMessageData12NextQueryPage(response.getQueryResponseHolder().getQueryContextHolder(), 10000L));
            } catch (IOException | ValidationException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Received " + response.getQueryResponseHolder().getHits().size() + " records..");
        //do whatever else you want with this.
    }
};
```

##### ATTACHMENT REQUEST

Sending an attachment request goes the same way as anything else so far. 

```java
MessageData4AttachmentRequest data = MessageManager.createMessageData4AttachmentRequest(
        "SELECT * FROM \"multi_event-@attachment\" WHERE id='TEST2006301005294740' and ownerid='TEST2006301005294810' FOR UPDATE WAIT 86400");
client.sendAttachmentRequest4(data);
```

The ack for this message is available through the subscribed listener.
The ack may contain the attachment if you also requested the binary attachment.
If not contains and you requested the binary, the attachment is not yet available and will be sent as an 'attachment response' type message at a later time.

You should not forget, that if you receive the attachment in an `AttachmentResponse6` type of message, you're required to send the appropriate ACK back to the GDS, otherwise it will send the attachment again and again unless the ACK arrives for it.

```java
GDSMessageListener listener = new GDSMessageListener() {

    // ... rest of the overrides as you want
     
    @Override 
    public void onAttachmentRequestAck5(MessageHeaderBase header, MessageData5AttachmentRequestAck requestAck) {
         System.out.println("Attachment request ack message received with '" + requestAck.getGlobalStatus() + "' status code");
         if (requestAck.getData() != null) {
             byte[] attachment = requestAck.getData().getResult().getAttachment();
             if (attachment == null) {
                 //if you requested the binary, the attachment will be sent as an 'attachment response' type message at a later time
             }
         }
     }
    
    @Override
    public void onAttachmentResponse6(MessageHeaderBase header, MessageData6AttachmentResponse response) {
        String messageID = header.getMessageId();
         try {
         byte[] attachment = response.getBinary();
         clientReference.get().sendAttachmentResponseAck7(messageID, MessageManager.createMessageData7AttachmentResponseAck(
                 AckStatus.OK,
                 new AttachmentResponseAckResultHolderImpl(AckStatus.CREATED,
                         new AttachmentResultHolderImpl(
                                 response.getResult().getRequestIds(),
                                 response.getResult().getOwnerTable(),
                                 response.getResult().getAttachmentId()
                         )),
                 null));
         } catch (IOException | ValidationException e) {
             //this can happen as well if the getBinary() call on the response fails.
         }
     }
};
```

Note: the GDS may also send an attachment request - `onAttachmentRequest4(MessageHeaderBase header, MessageData4AttachmentRequest request)` - to the client.


#### AUTOMATIC PUSHING 

A user may be interested in data or changes in specific data. 
The criteria system, based on which data may be of interest to the user, is included in the configuration of the delivered system. 
This data is sent automatically by the GDS. For these, you should also send an ACK back for the same reason.

```java
GDSMessageListener listener = new GDSMessageListener() {

    // ... rest of the overrides as you want
    
    @Override
    void onAttachmentResponse6(MessageHeaderBase header, MessageData6AttachmentResponse response) {
         //... same as above.
     }
     
     @Override
     public void onEventDocument8(MessageHeaderBase header, MessageData8EventDocument eventDocument) {
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
        clientReference.get().sendEventDocumentAck9(eventDocumentAckData);
         } catch (IOException | ValidationException e) {
             //this should not happen as the message creation only contains valid values above.
         }
     }
};
```

### Close the connection

The client is simply closed by the `close()` method. This should be always called, otherwise the event loop in the background is never stopped and the WebSocket connection might not be closed properly, either.

```java
client.close();
```

### Reusing the client

The client is not reusable, meaning that if the connection (login) fails or the client is closed, the `connect()` method cannot be invoked and will throw an `IllegalStateException`. If you want to use the client again, you have to create a new instance.

### Thread-safety

The `AsyncGDSClient` is created with a thread-safe approach, meaning you can send messages from multiple threads without having to worry about race conditions or deadlocks. If multiple threads try to invoke the `connect()` method on the client, only the first one will be successful, the client will raise an `IllegalStateException` for the other threads.

### Working with custom messages

A message consists of two parts, a header and a data. 
With this SDK, it is usually enough to create only the data part because the header part is created automatically when the message is sent using the `send..()` method (see above). However, it is also possible to explicitly define the header part with customized values.

To create the header, you should use the `MessageManager` here as well. 
If you do not want to specify every parameter for the header, these overloads for the `CreateMessageHeaderBase(..)` method might come in handy for you:

 - `CreateMessageHeaderBase(String userName, MessageDataType dataType)`
 - `CreateMessageHeaderBase(String userName, String messageID, MessageDataType dataType)`

Both methods can throw `IOException` and `ValidationException`, so you want to use a `try-catch` block around these as well.

```java
try{
    MessageHeader header = MessageManager.createMessageHeaderBase("username", UUID.randomUUID().toString(), MessageDataType.ATTACHMENT_REQUEST_4);
} catch(IOException | ValidationException e ){
    // handle error
}
```

### Async client example
This is a simple class demonstrating how can you send queries to the GDS using the async client.
```java
import hu.arh.gds.client.AsyncGDSClient;
import hu.arh.gds.client.Either;
import hu.arh.gds.client.GDSMessageListener;
import hu.arh.gds.client.Pair;
import hu.arh.gds.message.data.ConsistencyType;
import hu.arh.gds.message.data.MessageData11QueryRequestAck;
import hu.arh.gds.message.data.MessageData1ConnectionAck;
import hu.arh.gds.message.header.MessageHeaderBase;
import hu.arh.gds.message.util.ValidationException;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncExample {

    private final static String USERNAME = "user";
    private final static String URI = "ws://127.0.0.1:8888/gate";


    public static void main(String[] args) {
        try {
            Object lock = new Object();
            final AtomicReference<AsyncGDSClient> clientAtomicReference = new AtomicReference<>();

            AsyncGDSClient asyncGDSClient = AsyncGDSClient.getBuilder()
                    .withURI(URI)
                    .withUserName(USERNAME)
                    .withListener(new GDSMessageListener() {

                        @Override
                        public void onConnectionSuccess(Channel ch, MessageHeaderBase header, MessageData1ConnectionAck response) {
                            System.out.println("Client connected!");
                            synchronized (lock) {
                                lock.notify();
                            }
                        }

                        @Override
                        public void onDisconnect(Channel channel) {
                            System.out.println("Client disconnected!");
                        }

                        @Override
                        public void onConnectionFailure(Channel channel, Either<Throwable, Pair<MessageHeaderBase, MessageData1ConnectionAck>> reason) {
                            System.out.println("Client could not connect!");
                            System.out.println("Reason: " + reason);
                            synchronized (lock) {
                                lock.notify();
                            }
                        }


                        @Override
                        public void onQueryRequestAck11(MessageHeaderBase header, MessageData11QueryRequestAck response) {
                            AsyncGDSClient gdsClient = clientAtomicReference.get();
                            if (response.getGlobalStatus().isErrorStatus()) {
                                System.out.println("Select FAILED: " + response.getGlobalException());
                                gdsClient.close();
                                return;
                            }

                            System.out.println("Received " + response.getQueryResponseHolder().getHits().size() + " records.");
                            if (response.getQueryResponseHolder().getMorePage()) {
                                try {
                                    gdsClient.sendNextQueryPage12(response.getQueryResponseHolder().getQueryContextHolder(), 10_000L);
                                } catch (IOException | ValidationException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                gdsClient.close();
                            }
                        }
                    }).build();

            clientAtomicReference.set(asyncGDSClient);
            asyncGDSClient.connect();

            synchronized (lock) {
                lock.wait();
            }

            if (asyncGDSClient.isConnected()) {
                try {
                    asyncGDSClient.sendQueryRequest10("SELECT * FROM multi_event LIMIT 1000", ConsistencyType.NONE, 10_000L);

                } catch (ValidationException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                asyncGDSClient.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
```


## Synchronous Client


Sometimes you do not want to suffer with the problems of the asynchronous world, and want to use your client with _return values_ instead of callbacks and handlers.

For this scenario, a built-in wrapper around the async client specified above is available, by the `SyncGDSClient` class.

This is a wrapper around the async client, making it thread-safe as well. The method calls will block until the reply arrive, then giving the result as their return value.

If you need to, multiple threads can use the same client, as the blocking in the background uses the messageID to await replies. This means that IDs must be unique across the threads (as long as their reply does not arrive), otherwise the synchronous client will throw an `IllegalStateException`.


### Client creation

The synchronous client uses a builder with the same methods as the async client (excluding the `setListener(..)`, as it cannot defined here), but its name is `SyncGDSClientBuilder`. It can be invoked with the static `getBuilder()` method as well in the `SyncGDSClient`.

```java
final String URI = "ws://127.0.0.1:8888/gate";
final String USERNAME = "user";

SyncGDSClient client = SyncGDSClient.getBuilder()
        .withURI(URI)
        .withUserName(USERNAME)
        .withTimeout(5_000L) //5 seconds for a request before the timeout occurs
        .build();
```

- `withLogger(Logger logger)` - sets the `Logger` instance used for logging messages.
- `withURI(String URI)` - sets the GDS URI.
- `withUserName(String userName)` - sets the username used in the GDS communication.
- `withUserPassword(String userPassword)` - sets the password used for _password authentication_.
- `withTimeout(long timeout)` - sets the timeout (in milliseconds) for the login procedure. If you do not specify this, the value will be set to `3000` (ms).
- `withTLS(InputStream cert, String secret)` -sets the credentials from a PKCS12 formatted cert (file) used for connecting via TLS. This method has an overload for `(String cert,String secret)` parameters, in this case the cert first parameter be used as a path to a file containing the certification.
- `build()` - creates the `SyncGDSClient` instance.

The restrictions for these values are the same as specified in the async client, but here is a reminder:

- `URI` - cannot be `null`, and has to represent a valid URI to the GDS. Since the connection is established through the WebSocket protocol, the URI scheme must start with either `ws` or `wss` to be accepted.
- `userName` - the username cannot be null or set to an empty string (or a string containing only whitespaces).
- `userPassword` - if the user wishes to use _password authentication_, this will be used. Otherwise, the value should be set to `null`. 
- `timeout` - the timeout must be a positive number, representing the maximum wait time (in milliseconds) before the client raises an exception if a response does not arrive (including the login).
- `log` - the Logger instance used to track and debug the client. if the value is `null`, a default one will be created with the name `"SyncGDSClient"` and the log level set to `SEVERE`. Otherwise, the given one will be used. The default log will use the standard error (`System.err`) as its output stream. The log format will be the following: `[2020-10-19 08:15:39] [SEVERE] | hu.arh.gds.client.SyncGDSClient::methodName | Some error message, that will be in the log.`
 - `listener` - the `GDSMessageListener` instance used for callbacks. Value cannot be `null`.
- `sslCtx` - the SSLContext used to setup the TLS for the client. If TLS is not used, the value should be set to `null`.
  The context can be created via the static `AsyncGDSClient.createSSLContext(..)` method.
### Methods

Since the synchronous client uses a request-reply scheme, not all type of messages are supported. You can invoke the following methods (send these types):

 - `EventResponse sendEvent2(..)`
 - `AttachmentResult sendAttachmentRequest4(..)`
 - `EventDocumentResponse sendEventDocument8(..)`
 - `QueryResponse sendQueryRequest10(..)`
 - `QueryResponse sendNextQueryPage12(..)`


The parameters (and overloads) for these messages are the same as specified on the async client.

The returning types inherit from the same class, the `GDSMessage` (found in the `hu.arh.gds.message.clienttypes` package).

This class has two methods:
 - `MessageHeaderBase getHeader();` which returns the header from the message, 
 - `T getData();` which returns the data part of the message.

 The generic type `T` is substituted for each subclass as the following:

  - `EventResponse -> MessageData3EventAck`
  - `AttachmentResult -> Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse>`
  - `EventDocumentResponse -> MessageData9EventDocumentAck`
  - `QueryResponse -> MessageData11QueryRequestAck`

As the type of the message the attachment result arrives in is not predetermined, it returns an _either_ object.
To make things easier the `AttachmentResult` class has additional methods to check for the proper type:

The `isAttachmentRequestAck()` and the `isAttachmentResponse()` will return boolean values to indicate the message content, while the `getDataAsAttachmentRequestAck()` and `getDataAsAttachmentResponse()` methods will return the proper types from the underlying data structure.



### Connecting

The client's `connect()` method will return a `boolean` value, indicating its success. If it is `false`, the reason can be retrieved with the `hasConnectionFailed()` or the `hasLoginFailed()` methods and the `getConnectionError()` or the `getLoginFailureReason()` which will return a `Throwable` or a `Pair<MessageHeaderBase, MessageData1ConnectionAck>`.
```java
 SyncGDSClient client = SyncGDSClient.getBuilder()
        .withURI(URI)
        .withUserName(USERNAME)
        .build();

    if (client.connect()) {
        // ... client logic
        
        client.close();
        
    } else {
        if (client.hasConnectionFailed()) {
            System.err.println("Could not connect to the GDS! Reason: " + client.getConnectionError());
        } else if (client.hasLoginFailed()) {
            System.err.println("Could not log in to the GDS! Reason: " + client.getLoginFailureReason().getSecond());
        } else {
            //this should never happen
        }
    }
```

If for any reason you need the login response, the `getLoginResponse()` method will return the appropriate value (`Pair<MessageHeaderBase, MessageData1ConnectionAck>`).

### Closing

Same as in the async version, closing can simply be done with the `close()` method. You should not forget to call this.

```java
client.close();
```

### Sending (sync) messages 


As mentioned, these methods use return values. To make life easier, special classes were introduced (found in the `hu.arh.gds.message.clienttypes` package) with the received `MessageHeaderBase` and proper subclass of the `MessageData`.

The header can be accessed by the `getHeader()` and the data part with the `getData()` getter method (as explained above).

Example:
```java
QueryResponse response =
    client.sendQueryRequest10(
        MessageManager.createMessageData10QueryRequest(
            "SELECT * FROM multi_event LIMIT 1000",
            ConsistencyType.NONE,
            3_000L
        )
    );

System.out.println("Received: " + response.getData().getQueryResponseHolder().getNumberOfHits() + " records.");
```

The only exception to this scheme is the `sendAttachmentRequest4(..)` method.

Since the GDS might not have the attachment stored, it is possible that the first reply will not contain any binaries, and the GDS will send it later in an other message, so the attachment itself will be sent in either a `MessageData5AttachmentRequestAck` or an `MessageData6AttachmentResponse` type of message.
Since the sync client awaits the binary itself, the type of the returned result will mirror this: the return value `AttachmentResult` will contain as data an `Either<MessageData5AttachmentRequestAck, MessageData6AttachmentResponse>` object.

The `Either` class indicates that it is not predetermined which value will be set, so the result is _either_ a  `MessageData5AttachmentRequestAck` or a `MessageData6AttachmentResponse`. The either object has methods like `isLeftSet() / isRightSet()` and `getLeft() / getRight()`, but the `AttachmentResult` class has more flexibility. 

The `isAttachmentRequestAck()` and the `isAttachmentResponse()` methods will return boolean values to indicate the message content, while the `getDataAsAttachmentRequestAck()` and `getDataAsAttachmentResponse()` methods will return the proper types from the underlying data structure.


```java
AttachmentResult result = syncGDSClient.sendAttachmentRequest4(MessageManager.createMessageData4AttachmentRequest("SELECT * FROM attachments"));

if (result.isAttachmentRequestAck()) {
    if (result.getDataAsAttachmentRequestAck().getBinary() == null) {
        throw new IllegalStateException("Attachment should not be null!!");
    } else {
        System.err.println("Attachment is " + result.getDataAsAttachmentRequestAck().getBinary().length + " bytes.");
    }
} else {
    if (result.getDataAsAttachmentResponse().getBinary() == null) {
        throw new IllegalStateException("Attachment should not be null!!");
    } else {
        System.err.println("Attachment is " + result.getDataAsAttachmentResponse().getBinary().length + " bytes.");
    }
}
```

### Sync client example

A full example of a synchronous client can be seen below.

You can test your application with the [GDS Server simulator](https://github.com/arh-eu/gds-server-simulator) if you have no access to a GDS instance.

```java
import hu.arh.gds.client.SyncGDSClient;
import hu.arh.gds.message.clienttypes.QueryResponse;
import hu.arh.gds.message.data.ConsistencyType;
import hu.arh.gds.message.util.MessageManager;

public class SimpleExample {
    public static void main(String[] args) throws Throwable {
        SyncGDSClient syncGDSClient = SyncGDSClient.getBuilder()
                .withURI("ws://127.0.0.1:8888/gate")
                .withUserName("user")
                .withTimeout(10_000L)
                .build();

        try {
            if (syncGDSClient.connect()) {
                long timeout = 6_000L;
                QueryResponse response =
                        syncGDSClient.sendQueryRequest10(
                                MessageManager.createMessageData10QueryRequest(
                                        "SELECT * FROM multi_event LIMIT 1000", ConsistencyType.NONE, timeout));
                System.out.println("Received a total of " + response.getData().getQueryResponseHolder().getNumberOfHits() + " records.");

                while (response.getData().getQueryResponseHolder().getMorePage()) {
                    response = syncGDSClient.sendNextQueryPage12(MessageManager.createMessageData12NextQueryPage(
                            response.getData().getQueryResponseHolder().getQueryContextHolder(), timeout));
                    System.out.println("Received an additional " + response.getData().getQueryResponseHolder().getNumberOfHits() + " records.");
                }
            } else {
                if (syncGDSClient.hasConnectionFailed()) {
                    System.err.println("Could not connect to the GDS instance! Reason: " + syncGDSClient.getConnectionError());
                } else if (syncGDSClient.hasLoginFailed()) {
                    System.err.println("Could not log in to the GDS! Reason: " + syncGDSClient.getLoginResponse().getSecond().getGlobalException());
                }
            }
        } finally {
            syncGDSClient.close();
        }
    }
}
```

The output can be the following:
```txt
Received a total of 300 records.
Received an additional 300 records.
Received an additional 300 records.
Received an additional 100 records.
```
