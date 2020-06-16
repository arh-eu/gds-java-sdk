package hu.arh.gds.console;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;

public class ConsoleArgumentsHelper {

    private static String getJarName() {
        return new java.io.File(ConsoleHandler.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }

    public static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newFor(getJarName())
                .build()
                .defaultHelp(true)
                .description("GDS Console Client");

        parser.addArgument("-username").setDefault("user").help("the username you would like to use to login to the GDS");
        parser.addArgument("-password").help("the password you would like to use to login into the GDS");
        parser.addArgument("-url").setDefault("ws://127.0.0.1:8080/gate").help("the URL of the GDS instance you would like to connect to");
        parser.addArgument("-timeout").setDefault(30000).help("the timeout value for the response messages in milliseconds").type(Integer.TYPE);

        MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup();
        group.required(true);
        group.addArgument("-event").help("the INSERT/UPDATE/MERGE statement you would like to use");
        group.addArgument("-attachment").help("the SELECT statement you would like to use");
        group.addArgument("-query").help("the SELECT statement you would like to use");
        group.addArgument("-queryall").help("the SELECT statement you would like to use (this will query all pages, not just the first one)");

        return parser;
    }

    public static ConsoleArguments getConsoleArguments(String[] args) {
        ArgumentParser parser = getArgumentParser();
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        MessageType messageType = null;
        String statement = null;

        String sqlEventStatement = ns.getString("event");
        if (sqlEventStatement != null) {
            messageType = MessageType.EVENT;
            statement = sqlEventStatement;
        }

        String querySqlStatement = ns.get("query");
        if (querySqlStatement != null) {
            messageType = MessageType.QUERY;
            statement = querySqlStatement;
        }

        String queryAllSqlStatement = ns.getString("queryall");
        if (queryAllSqlStatement != null) {
            messageType = MessageType.QUERYALL;
            statement = queryAllSqlStatement;
        }

        String attachmentSqlStatement = ns.getString("attachment");
        if (attachmentSqlStatement != null) {
            messageType = MessageType.ATTACHMENT;
            statement = attachmentSqlStatement;
        }

        Integer timeout = ns.getInt("timeout");

        return new ConsoleArguments(
                ns.getString("url"),
                ns.getString("user"),
                ns.get("password"),
                messageType,
                statement,
                timeout);
    }
}
