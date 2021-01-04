package hu.arheu.gds.console.parser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "requesting an attachment")
public class AttachmentRequestCommand {

    @Parameter(description = "the SELECT statement you would like to use")
    public String attachmentRequest;
}
