package ninja.leaping.webconsole;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.ClasspathResolver;
import com.google.inject.Inject;
import io.undertow.websockets.core.WebSocketChannel;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.service.sql.SqlService;
import org.xnio.ChannelListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple sponge plugin
 */
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class WebConsolePlugin {
    public static final String WEB_CONSOLE_SUBJECT_TYPE = "webconsole";

    @Inject private Logger logger;
    @Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject @ConfigDir(sharedRoot = false) private File configDir;
    @Inject private Game game;

    private SqlService sql;
    private WebConsoleConfiguration config;
    private final MustacheFactory factory = new DefaultMustacheFactory(new ClasspathResolver(getClass().getPackage().getName().replace(".", "/")
            + "/web"));
    private final AtomicReference<WebConsoleServer> server = new AtomicReference<>();

    @Subscribe
    public void onPreInit(PreInitializationEvent event) throws IOException, ObjectMappingException {
        sql = game.getServiceManager().provideUnchecked(SqlService.class);
        reload();

    }

    @Subscribe
    public void disable(ServerStoppingEvent event) {
        WebConsoleServer server = this.server.getAndSet(null);
        server.close();
        // Perform shutdown tasks here
    }

    public void reload() throws IOException, ObjectMappingException {
        WebConsoleServer oldServer = server.getAndSet(null);
        if (oldServer != null) {
            oldServer.close();
        }
        CommentedConfigurationNode node = configLoader.load();
        this.config = WebConsoleConfiguration.MAPPER.bindToNew().populate(node);
        configDir.mkdirs();
        this.configLoader.save(node);

        WebConsoleServer newServer = new WebConsoleServer(this);
        if (server.compareAndSet(null, newServer)) {
            newServer.start();
            logger.info("Web console is listening on {}", newServer.getListeningAddress());
        }
    }

    public Mustache getTemplate(String file) {
        return factory.compile(file);
    }

    public ChannelListener<? super WebSocketChannel> openSession(WebSocketChannel channel, ConsoleUser user) {
        return new WebConsoleSession(channel, user, this.game);
    }

    public WebConsoleConfiguration getConfig() {
        return config;
    }
}
