package hu.arheu.gds.console.parser;

import com.beust.jcommander.JCommander;
import hu.arheu.gds.console.MessageType;
import hu.arheu.gds.message.util.Utils;

public class ArgumentParser {

    public static ArgumentsHolder getConsoleArgument(String[] args) {

        Options options = new Options();
        EventCommand eventCommand = new EventCommand();
        QueryCommand queryCommand = new QueryCommand();
        AttachmentRequestCommand attachmentRequestCommand = new AttachmentRequestCommand();

        JCommander jc = JCommander.newBuilder()
                .addObject(options)
                .addCommand("query", queryCommand)
                .addCommand("event", eventCommand)
                .addCommand("attachment-request", attachmentRequestCommand)
                .build();

        if (args == null || args.length == 0) {
            jc.usage();
            return null;
        }

        jc.parse(args);

        if (options.help) {
            jc.usage();
            return null;
        }

        if (options.hex != null) {
            for (String hexValue : options.hex) {
                System.out.println(hexValue + " = " + "0x" + Utils.stringToUTF8Hex(hexValue));
            }
            return null;
        }

        MessageType messageType = null;
        String statement = null;
        if (eventCommand.event != null) {
            messageType = MessageType.EVENT;
            statement = eventCommand.event;
        } else if (queryCommand.query != null) {
            if (queryCommand.queryAll) {
                messageType = MessageType.QUERYALL;
            } else {
                messageType = MessageType.QUERY;
            }
            statement = queryCommand.query;
        } else if (attachmentRequestCommand.attachmentRequest != null) {
            messageType = MessageType.ATTACHMENT;
            statement = attachmentRequestCommand.attachmentRequest;
        } else {
            System.err.println("Cannot run without any commands specified!");
            return null;
        }

        return new ArgumentsHolder(
                options.url,
                options.user,
                options.password,
                options.cert,
                options.secret,
                messageType,
                statement,
                options.timout,
                eventCommand.files,
                options.export,
                options.nogui);

    }
}
