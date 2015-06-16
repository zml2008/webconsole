package ninja.leaping.webconsole;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SecureRandomSessionIdGenerator;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionIdGenerator;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionListeners;
import io.undertow.server.session.SessionManager;

import java.util.Set;

public class DatabaseSessionManager implements SessionManager {
    private final SessionIdGenerator generator = new SecureRandomSessionIdGenerator();
    private final SessionListeners listeners = new SessionListeners();

    @Override
    public String getDeploymentName() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Session createSession(HttpServerExchange exchange, SessionConfig config) {
        return null;
    }

    @Override
    public Session getSession(HttpServerExchange exchange, SessionConfig config) {
        final String sessionId = config.findSessionId(exchange);
        if (sessionId == null) {
            return null;
        }
        return getSession(sessionId);
    }

    @Override
    public Session getSession(String sessionId) {
        return null;
    }

    @Override
    public void registerSessionListener(SessionListener listener) {
        listeners.addSessionListener(listener);

    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        listeners.removeSessionListener(listener);
    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
    }

    @Override
    public Set<String> getTransientSessions() {
        return null;
    }

    @Override
    public Set<String> getActiveSessions() {
        return null;
    }

    @Override
    public Set<String> getAllSessions() {
        return null;
    }
}
