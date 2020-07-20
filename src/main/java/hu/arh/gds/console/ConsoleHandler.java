package hu.arh.gds.console;

import hu.arh.gds.client.AlreadySubscribedException;
import hu.arh.gds.client.GDSWebSocketClient;
import hu.arh.gds.console.parser.ArgumentParser;
import hu.arh.gds.console.parser.ArgumentsHolder;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class ConsoleHandler {

    public static void main(String[] args) {
        ArgumentsHolder argumentsHolder = ArgumentParser.getConsoleArgument(args);

        if (argumentsHolder == null) {
            return;
        }

        final Logger logger = Logger.getLogger("logging");
        SimpleFormatter fmt = new SimpleFormatter();
        StreamHandler sh = new StreamHandler(System.err, fmt);
        logger.addHandler(sh);
        logger.setUseParentHandlers(false);

        GDSWebSocketClient client = new GDSWebSocketClient(
                argumentsHolder.getUrl(),
                argumentsHolder.getUsername(),
                argumentsHolder.getPassword(),
                argumentsHolder.getCert(),
                argumentsHolder.getSecret(),
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
