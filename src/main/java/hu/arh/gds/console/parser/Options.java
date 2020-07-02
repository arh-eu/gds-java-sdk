package hu.arh.gds.console.parser;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Options {

    @Parameter(names = { "-h", "-help" }, help = true, order = 0)
    public boolean help = false;

    @Parameter(names = "-url", description = "the URL of the GDS instance you would like to connect to", order = 1)
    public String url = "ws://127.0.0.1:8888/gate";

    @Parameter(names = { "-u", "-username" }, description = "the username you would like to use to login to the GDS", order = 2)
    public String user = "user";

    @Parameter(names = {"-p", "-password"}, description = "the password you would like to use to login into the GDS", order = 3)
    public String password;

    @Parameter(names = { "-t", "-timeout" }, description = "the timeout value for the response messages in milliseconds", order = 4)
    public Integer timout = 30_000;

    @Parameter(names = "-hex", description = "convert strings to hexadecimal, you can enter multiple strings separated by commas", order = 5)
    public List<String> hex;

    @Parameter(names = "-export", description = "export all response messages to JSON, the JSON files will be saved in the folder named 'exports' next to the jar file", order = 6)
    public boolean export = false;
}
