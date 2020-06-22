package hu.arh.gds.console.commands;

import com.beust.jcommander.JCommander;
import hu.arh.gds.console.ConsoleArguments;
import hu.arh.gds.console.MessageType;
import hu.arh.gds.message.util.Utils;

public class ArgsParser {

    public static ConsoleArguments getConsoleArgument(String[] args) {

        OptionalArgs optionalArgs = new OptionalArgs();
        EventArgs eventArgs = new EventArgs();
        QueryArgs queryArgs = new QueryArgs();
        AttachmentRequestArgs attachmentRequestArgs = new AttachmentRequestArgs();

        JCommander jc = JCommander.newBuilder()
                .addObject(optionalArgs)
                .addCommand("query", queryArgs)
                .addCommand("event", eventArgs)
                .addCommand("attachment-request", attachmentRequestArgs)
                .build();

        jc.parse(args);

        if(optionalArgs.help) {
            jc.usage();
            return null;
        }

        if(optionalArgs.hex != null) {
            for(String hexValue: optionalArgs.hex) {
                System.out.println("0x" + Utils.stringToUTF8Hex(hexValue));
            }
            return null;
        }

        MessageType messageType = null;
        String statement = null;
        if(eventArgs.event != null) {
            messageType = MessageType.EVENT;
            statement = eventArgs.event;
        } else if(queryArgs.query != null) {
            if(queryArgs.queryAll) {
                messageType = MessageType.QUERYALL;
            } else {
                messageType = MessageType.QUERY;
            }
            statement = queryArgs.query;
        } else if(attachmentRequestArgs.attachmentRequest != null) {
            messageType = MessageType.ATTACHMENT;
            statement = attachmentRequestArgs.attachmentRequest;
        }

        return new ConsoleArguments(
                optionalArgs.url,
                optionalArgs.user,
                optionalArgs.password,
                messageType,
                statement,
                optionalArgs.timout,
                eventArgs.files);

    }
}
