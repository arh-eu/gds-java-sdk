package hu.arh.gds.console.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "a request for querying a GDS user table")
public class QueryArgs {

    @Parameter(description = "the SELECT statement you would like to use")
    public String query;

    @Parameter(names = "-all", description = "query all pages, not just the first one")
    public boolean queryAll = false;
}
