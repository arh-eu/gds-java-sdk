package hu.arheu.gds.console.parser;

import hu.arheu.gds.console.MessageType;

import java.io.File;
import java.util.List;

public record ArgumentsHolder(String url, String username, String password, String cert, String secret,
                              MessageType messageType, String statement, Integer timeout, List<File> files,
                              boolean export, boolean nogui) {
}
