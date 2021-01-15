package io.github.seggan.segganbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class Listener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        if (e.getMessage().getContentRaw().equals("?ping")) {
            e.getChannel().sendMessage("Pong!").queue();
        }

        processIncorrectSlimefun(e);
    }

    private static void processIncorrectSlimefun(MessageReceivedEvent e) {
        if (e.getMessage().getContentRaw().toLowerCase().contains("slime fun")) {
            e.getChannel().sendMessage(Util.pingUser(e.getAuthor()) + " It's Slimefun, not \"slime fun\"").queue();
        }
    }
}
