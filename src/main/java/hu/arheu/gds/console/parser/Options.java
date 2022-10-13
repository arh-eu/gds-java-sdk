package hu.arheu.gds.console.parser;

import com.beust.jcommander.Parameter;

import java.util.List;

@SuppressWarnings("CanBeFinal")
public class Options {

    @Parameter(names = {"-h", "-help"}, help = true, order = 0)
    public boolean help = false;

    @Parameter(names = "-url", description = "the URL of the GDS instance you would like to connect to", order = 1)
    public String url = "ws://127.0.0.1:8888/gate";

    @Parameter(names = {"-u", "-username"}, description = "the username you would like to use to login to the GDS", order = 2)
    public String user = "user";

    @Parameter(names = {"-p", "-password"}, description = "the password you would like to use to login into the GDS", order = 3)
    public String password;

    @Parameter(names = {"-c", "-cert"}, description = "the private key file which you would like to use for TLS connection", order = 4)
    public String cert;

    @Parameter(names = {"-s", "-secret"}, description = "the password for the private key to use for TLS connection", order = 5)
    public String secret;

    @Parameter(names = {"-t", "-timeout"}, description = "the timeout value for the response messages in milliseconds", order = 6)
    public Integer timout = 30_000;

    @Parameter(names = "-hex", description = "convert strings to hexadecimal, you can enter multiple strings separated by commas", order = 7)
    public List<String> hex;

    @Parameter(names = "-export", description = "export all response messages to JSON, the JSON files will be saved in the folder named 'exports' next to the jar file", order = 8)
    public boolean export = false;

    @Parameter(names = "-nogui", description = "Disables the GUI on the query response if there is only one page returned.", order = 9)
    public boolean nogui = false;
}
