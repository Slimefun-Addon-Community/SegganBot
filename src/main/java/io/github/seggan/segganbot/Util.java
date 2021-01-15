package io.github.seggan.segganbot;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public final class Util {
    private Util() {
    }

    public static String pingUser(User user) {
        return String.format(
            "<@%s>",
            user.getId()
        );
    }

    public static void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }
}
