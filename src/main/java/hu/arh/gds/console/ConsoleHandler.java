package hu.arh.gds.console;

import hu.arh.gds.client.AlreadySubscribedException;
import hu.arh.gds.client.GDSWebSocketClient;
import hu.arh.gds.console.parser.ArgumentsHolder;
import hu.arh.gds.console.parser.ArgumentParser;

import java.util.logging.Logger;

public class ConsoleHandler {

    public static void main(String[] args) {
        ArgumentsHolder argumentsHolder = ArgumentParser.getConsoleArgument(args);

        if(argumentsHolder == null) {
            return;
        }

        final Logger logger = Logger.getLogger("logging");
        logger.setUseParentHandlers(false);

        GDSWebSocketClient client = new GDSWebSocketClient(
                argumentsHolder.getUrl(),
                argumentsHolder.getUsername(),
                argumentsHolder.getPassword(),
                logger);

        try {
            client.setMessageListener(new ConsoleMessageListener(argumentsHolder, client));
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
