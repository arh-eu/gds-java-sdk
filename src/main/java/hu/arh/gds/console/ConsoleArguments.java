package hu.arh.gds.console;

public class ConsoleArguments {
    private String url;
    private String user;
    private String password;
    private MessageType messageType;
    private String statement;
    private Integer timeout;

    public ConsoleArguments(String url, String user, String password, MessageType messageType, String statement, Integer timeout) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.messageType = messageType;
        this.statement = statement;
        this.timeout = timeout;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public String getStatement() {
        return this.statement;
    }

    public Integer getTimeout() {
        return timeout;
    }
}
