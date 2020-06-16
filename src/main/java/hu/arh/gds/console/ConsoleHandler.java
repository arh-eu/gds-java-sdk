package hu.arh.gds.console;

import hu.arh.gds.client.AlreadySubscribedException;
import hu.arh.gds.client.GDSWebSocketClient;

import java.util.logging.Logger;

public class ConsoleHandler {
    private static final String defaultUrl = "ws://127.0.0.1:8080/gate";
    private static final String defaultUser = "user";
    private static final String defaultPassword = null;
    private static final int defaultTimeout = 30_000;

    public static void main(String[] args) {
        ConsoleArguments consoleArguments = ConsoleArgumentsHelper.getConsoleArguments(args);

        final Logger logger = Logger.getLogger("logging");
        logger.setUseParentHandlers(false);

        String url = consoleArguments.getUrl();
        String user = consoleArguments.getUser();
        String password = consoleArguments.getPassword();
        GDSWebSocketClient client = new GDSWebSocketClient(
                url != null ? url : defaultUrl,
                user != null ? user : defaultUser,
                password != null ? password : defaultPassword,
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
