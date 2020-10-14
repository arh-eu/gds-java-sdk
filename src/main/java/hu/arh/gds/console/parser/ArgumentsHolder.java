package hu.arh.gds.console.parser;

import hu.arh.gds.console.MessageType;

import java.io.File;
import java.util.List;

public class ArgumentsHolder {
    private String url;
    private String username;
    private String password;
    private String cert;
    private String secret;
    private MessageType messageType;
    private String statement;
    private Integer timeout;
    private List<File> files;
    private boolean export;
    private boolean nogui;

    public ArgumentsHolder(String url,
                           String username,
                           String password,
                           String cert,
                           String secret,
                           MessageType messageType,
                           String statement,
                           Integer timeout,
                           List<File> files,
                           boolean export,
                           boolean nogui) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.cert = cert;
        this.secret = secret;
        this.messageType = messageType;
        this.statement = statement;
        this.timeout = timeout;
        this.files = files;
        this.export = export;
        this.nogui = nogui;
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

    public String getCert() {
        return cert;
    }

    public String getSecret() {
        return secret;
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

    public boolean getExport() {
        return this.export;
    }

    public boolean withNoGUI() {
        return nogui;
    }
}
