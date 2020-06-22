package hu.arh.gds.console;

import java.io.File;
import java.util.List;

public class ConsoleArguments {
    private String url;
    private String username;
    private String password;
    private MessageType messageType;
    private String statement;
    private Integer timeout;
    private List<File> files;

    public ConsoleArguments(String url,
                            String username,
                            String password,
                            MessageType messageType,
                            String statement,
                            Integer timeout,
                            List<File> files) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.messageType = messageType;
        this.statement = statement;
        this.timeout = timeout;
        this.files = files;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
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

    public List<File> getFiles() {
        return files;
    }
}
