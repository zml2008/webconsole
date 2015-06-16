package ninja.leaping.webconsole;

import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

public class WebConsoleConfiguration {
    public static final ObjectMapper<WebConsoleConfiguration> MAPPER;
    static {
        try {
            MAPPER = ObjectMapper.forClass(WebConsoleConfiguration.class);
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Setting(comment = "The port to listen on")
    private int port = 4858;
    @Setting(value = "bind-address", comment = "The address to bind to. By default this only binds to localhost.")
    private String bindAddress = "::1";
    @Setting(value = "path-base", comment = "The base path to use for creating links")
    private String pathBase = "";
    @Setting(value = "datastore", comment = "The JDBC url to use to store user data")
    private String dataStore = "jdbc:h2:plugins/webconsole/database.db";

    public int getPort() {
        return port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public String getPathBase() {
        return pathBase;
    }
}
