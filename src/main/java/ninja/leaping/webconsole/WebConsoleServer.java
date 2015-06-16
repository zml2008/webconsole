package ninja.leaping.webconsole;


import com.github.mustachejava.Mustache;
import com.google.common.collect.ImmutableMap;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.Sessions;
import io.undertow.util.StatusCodes;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class WebConsoleServer {
    private final InetSocketAddress listenAddr;
    private final WebConsolePlugin plugin;
    private final Undertow server;

    public WebConsoleServer(WebConsolePlugin plugin) {
        this.plugin = plugin;
        final String base = plugin.getConfig().getPathBase();
        try {
            this.listenAddr = new InetSocketAddress(InetAddress.getByName(plugin.getConfig().getBindAddress()), plugin.getConfig().getPort());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Address " + plugin.getConfig().getBindAddress() + " is invalid!");
        }

        server = Undertow.builder()
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setServerOption(UndertowOptions.ENABLE_SPDY, true)
                .addHttpListener(plugin.getConfig().getPort(), plugin.getConfig().getBindAddress())
                .setHandler(new SessionAttachmentHandler(Handlers.path()
                        .addExactPath(base + "/login", new LoginHandler())
                        .addExactPath(base + "/logout", exchange -> {
                            exchange.setResponseCode(StatusCodes.TEMPORARY_REDIRECT);
                            exchange.getResponseHeaders().put(Headers.LOCATION, plugin.getConfig().getPathBase() + "/login");
                            exchange.endExchange();
                        })
                        .addExactPath(base + "/", new RootHandler())
                        .addExactPath(base + "/socket", Handlers.websocket(new WebSocketHandler()))
                        .addPrefixPath("/static",
                                Handlers.resource(new ClassPathResourceManager(getClass().getClassLoader(), getClass().getPackage().getName()
                                        .replace(".", "/") + "/web/static"))),
                        new InMemorySessionManager("WebConsole"), new SessionCookieConfig()))
                .build();
    }

    public void start() {
        this.server.start();
    }

    public void close() {
        server.stop();
    }

    private static boolean isLoggedIn(HttpServerExchange exchange) {
        Session sess = Sessions.getSession(exchange);
        if (sess != null) {
            return sess.getAttribute("user") != null;
        }
        return false;
    }

    private static ConsoleUser getUser(HttpServerExchange exchange) {
        Session sess = Sessions.getSession(exchange);
        if (sess != null) {
            return (ConsoleUser) sess.getAttribute("user");
        }
        return null;
    }

    private static void templateToResponse(Mustache mustache, HttpServerExchange exchange, Map<String, Object> data)
            throws IOException {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        Session sess = Sessions.getSession(exchange);
        if (sess != null) {
            ConsoleUser user = (ConsoleUser) sess.getAttribute("user");
            if (user != null) {
                data = ImmutableMap.<String, Object>builder().putAll(data).put("user", user).build();
            }
        }
        exchange.startBlocking();
        OutputStreamWriter writer = new OutputStreamWriter(exchange.getOutputStream(), exchange.getResponseCharset());
        mustache.execute(writer, data);
        writer.flush();
    }

    public SocketAddress getListeningAddress() {
        return this.listenAddr;
    }

    private class LoginHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            if (isLoggedIn(httpServerExchange)) { // Already logged in? Redirect to main
                httpServerExchange.setResponseCode(StatusCodes.TEMPORARY_REDIRECT);
                httpServerExchange.getResponseHeaders().put(Headers.LOCATION, plugin.getConfig().getPathBase() + "/");
            }

            if (httpServerExchange.getRequestMethod().equals(Methods.GET)) {
                templateToResponse(plugin.getTemplate("login.mustache"), httpServerExchange, ImmutableMap.<String, Object>of("title", "Login",
                        "basedir", plugin.getConfig().getPathBase()));
                httpServerExchange.endExchange();
            } else if (httpServerExchange.getRequestMethod().equals(Methods.POST)) {
                Session sess = Sessions.getOrCreateSession(httpServerExchange);
                //sess.setAttribute("user");
                httpServerExchange.setResponseCode(StatusCodes.TEMPORARY_REDIRECT);
                httpServerExchange.getResponseHeaders().put(Headers.LOCATION, plugin.getConfig().getPathBase() + "/");
                httpServerExchange.endExchange();
            } else {
                httpServerExchange.setResponseCode(StatusCodes.METHOD_NOT_ALLOWED);
                httpServerExchange.endExchange();
            }
        }
    }

    private class RootHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            templateToResponse(plugin.getTemplate("index.mustache"), exchange, ImmutableMap.<String, Object>of("title", "Login",
                    "basedir", plugin.getConfig().getPathBase()));
            exchange.endExchange();

        }
    }

    private class WebSocketHandler implements WebSocketConnectionCallback {

        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
            System.out.println("Received websocket connection!");
            System.out.println("WebSocket version: " + channel.getVersion());
            SessionManager manager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
            SessionConfig config = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
            ConsoleUser user = null;

            if (user == null) {
                WebSockets.sendClose(new CloseMessage(CloseMessage.MSG_VIOLATES_POLICY, "Not authenticated"), channel, null);
            } else {
                channel.getReceiveSetter().set(plugin.openSession(channel, user));
            }

        }
    }



}
