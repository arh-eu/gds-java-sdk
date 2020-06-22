package hu.arh.gds.console.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.util.List;

@Parameters(separators = "=", commandDescription = "store or modify events")
public class EventArgs {

    @Parameter(description = "the INSERT/UPDATE/MERGE statement you would like to use")
    public String event;

    @Parameter(names = "-attachments", description = "the file name of the attachments separated by semicolon", converter = FileConverter.class)
    List<File> files;
}
