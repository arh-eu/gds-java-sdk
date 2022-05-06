package hu.arheu.gds.console;

import hu.arheu.gds.client.AsyncGDSClient;
import hu.arheu.gds.client.SyncGDSClient;
import hu.arheu.gds.console.parser.ArgumentParser;
import hu.arheu.gds.console.parser.ArgumentsHolder;

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
        logger.setLevel(Level.ALL);
        SimpleFormatter fmt = new SimpleFormatter();
        StreamHandler sh = new StreamHandler(System.err, fmt);
        logger.addHandler(sh);
        logger.setUseParentHandlers(false);

        try (SyncGDSClient client = new SyncGDSClient(
                argumentsHolder.url(),
                argumentsHolder.username(),
                argumentsHolder.password(),
                logger,
                argumentsHolder.timeout(),
                AsyncGDSClient.createSSLContext(
                        argumentsHolder.cert(),
                        argumentsHolder.secret()));
             ConsoleClient cc = new ConsoleClient(argumentsHolder, client, logger)) {
            cc.run();
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException sslExc) {
            logger.severe(sslExc.toString());
        }
    }
}
