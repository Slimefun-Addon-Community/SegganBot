package io.github.seggan.segganbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Listener extends ListenerAdapter {
    private static final Pattern INCORRECT_SLIMEFUN_PATTERN = Pattern.compile("(?<=[\\w\\d\\s])*[Ss]lime(?:F|( [Ff]))un(?=[\\w\\d\\s])*");

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        if (e.getMessage().getContentRaw().equals("?ping")) {
            Util.sendMessage(e.getChannel(), "Pong!");
        }

        processIncorrectSlimefun(e);
    }

    private static void processIncorrectSlimefun(MessageReceivedEvent e) {
        String msg = e.getMessage().getContentRaw();
        Matcher matcher = INCORRECT_SLIMEFUN_PATTERN.matcher(msg);
        while (matcher.find()) {
            Util.sendMessage(e.getChannel(), String.format(
                "%s It's Slimefun, not \"%s\"",
                Util.pingUser(e.getAuthor()),
                matcher.group()
            ));
        }
    }
}
