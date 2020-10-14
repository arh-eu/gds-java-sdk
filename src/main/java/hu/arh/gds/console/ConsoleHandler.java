package hu.arh.gds.console;

import hu.arh.gds.client.AsyncGDSClient;
import hu.arh.gds.client.SyncGDSClient;
import hu.arh.gds.console.parser.ArgumentParser;
import hu.arh.gds.console.parser.ArgumentsHolder;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
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
        logger.setLevel(Level.SEVERE);
        SimpleFormatter fmt = new SimpleFormatter();
        StreamHandler sh = new StreamHandler(System.err, fmt);
        logger.addHandler(sh);
        logger.setUseParentHandlers(false);

        SyncGDSClient client;
        try {
            client = new SyncGDSClient(
                    argumentsHolder.getUrl(),
                    argumentsHolder.getUsername(),
                    argumentsHolder.getPassword(),
                    logger,
                    argumentsHolder.getTimeout(),
                    AsyncGDSClient.createSSLContext(
                            argumentsHolder.getCert(),
                            argumentsHolder.getSecret())
            );
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException sslExc) {
            logger.severe(sslExc.toString());
            return;
        }

        new ConsoleClient(argumentsHolder, client, logger).run();
    }
}
