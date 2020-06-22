package hu.arh.gds.console;

import hu.arh.gds.client.AlreadySubscribedException;
import hu.arh.gds.client.GDSWebSocketClient;
import hu.arh.gds.console.commands.ArgsParser;

import java.util.logging.Logger;

public class ConsoleHandler {

    public static void main(String[] args) {
        ConsoleArguments consoleArguments = ArgsParser.getConsoleArgument(args);

        if(consoleArguments == null) {
            return;
        }

        final Logger logger = Logger.getLogger("logging");
        logger.setUseParentHandlers(false);

        GDSWebSocketClient client = new GDSWebSocketClient(
                consoleArguments.getUrl(),
                consoleArguments.getUsername(),
                consoleArguments.getPassword(),
                logger);

        try {
            client.setMessageListener(new ConsoleMessageListener(consoleArguments, client));
        } catch (AlreadySubscribedException e) {
            System.out.println(e.getMessage());
        }

        try {
            client.connect();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }
}
