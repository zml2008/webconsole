package ninja.leaping.webconsole;

import com.google.common.base.Optional;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.spongepowered.api.Game;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.RemoteSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@NonnullByDefault
public class WebConsoleSession extends AbstractReceiveListener implements RemoteSource, RemoteConnection {
    private final WebSocketChannel chan;
    private final ConsoleUser user;
    private final Game game;
    private Subject subject;
    private MessageSink sink;

    public WebConsoleSession(WebSocketChannel chan, ConsoleUser user, Game game) {
        this.chan = chan;
        this.user = user;
        this.game = game;
        this.subject = game.getServiceManager().provideUnchecked(PermissionService.class).getSubjects(WebConsolePlugin.WEB_CONSOLE_SUBJECT_TYPE)
                .get(getIdentifier());
        this.sink = game.getServer().getBroadcastSink();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public void sendMessage(Text... texts) {
        sendMessage(Arrays.asList(texts));
    }

    @Override
    public void sendMessage(Iterable<Text> iterable) {
        for (Text text : iterable) {
            sendText(text);
        }

    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        String[] split = message.getData().split(":", 2);
        switch (split[0]) {
            case "message":
                try {
                    final String msg = split[1];
                    if (msg.startsWith("/")) {

                    }
                    Text text = Texts.json().from(split[1]); // TODO: Allow client to send json messages?
                } catch (TextMessageException ex) {

                }
                break;
            case "tabcomplete":
            case "close":
                break;
            default:
                throw new IOException("Unrecognized packet type " + split[0]);
        }
    }

    private void sendText(Text text) {
       if (!this.chan.isOpen()) {
           throw new IllegalStateException("Tried to send to a closed channel!");
       }
        WebSockets.sendText("message:" + Texts.json().to(text), this.chan, null);
    }

    @Override
    public MessageSink getMessageSink() {
        return this.sink;
    }

    @Override
    public void setMessageSink(MessageSink sink) {
        this.sink = sink;
    }

    @Override
    public String getIdentifier() {
        return user.getName();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.<CommandSource>of(this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.subject.getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return this.subject.getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return this.subject.getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String s) {
        return this.subject.hasPermission(contexts, s);
    }

    @Override
    public boolean hasPermission(String s) {
        return this.subject.hasPermission(s);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String s) {
        return this.subject.getPermissionValue(contexts, s);
    }

    @Override
    public boolean isChildOf(Subject subject) {
        return this.subject.isChildOf(subject);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject subject) {
        return this.subject.isChildOf(contexts, subject);
    }

    @Override
    public List<Subject> getParents() {
        return this.subject.getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return this.subject.getParents(contexts);
    }

    @Override
    public Set<Context> getActiveContexts() {
        return this.subject.getActiveContexts();
    }

    @Override
    public RemoteConnection getConnection() {
        return this;
    }

    @Override
    public InetSocketAddress getAddress() {
        return this.chan.getSourceAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return this.chan.getDestinationAddress(); // TODO: Try to get actual address
    }
}
