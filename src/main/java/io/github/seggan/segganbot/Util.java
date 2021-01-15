package io.github.seggan.segganbot;

import net.dv8tion.jda.api.entities.MessageChannel;

public final class Util {
    private Util() {
    }

    public static void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }
}
