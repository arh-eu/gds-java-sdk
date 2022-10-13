package hu.arheu.gds.console.parser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@SuppressWarnings("CanBeFinal")
@Parameters(separators = "=", commandDescription = "a request for querying a GDS user table")
public class QueryCommand {

    @Parameter(description = "the SELECT statement you would like to use")
    public String query;

    @Parameter(names = "-all", description = "query all pages, not just the first one")
    public boolean queryAll = false;
}
