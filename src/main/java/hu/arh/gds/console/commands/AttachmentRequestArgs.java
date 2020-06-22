package hu.arh.gds.console.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.util.List;

@Parameters(separators = "=", commandDescription = "requesting an attachment")
public class AttachmentRequestArgs {

    @Parameter(description = "the SELECT statement you would like to use")
    public String attachmentRequest;
}
