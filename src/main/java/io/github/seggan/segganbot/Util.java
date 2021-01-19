package io.github.seggan.segganbot;

import io.github.seggan.segganbot.constants.Patterns;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Util {
    private Util() {
    }

    @Data
    public static class Embed {
        private final String header;
        private final String body;
        private final String footer;
    }

    public static void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    public static EmbedBuilder parseMessage(@Nullable String title, @Nonnull String msg) {
        final EmbedBuilder builder = new EmbedBuilder();

        if (msg.indexOf('#') == -1) {
            return builder.setTitle(title).setDescription(msg);
        }

        final StringBuilder sb = new StringBuilder();
        String fieldTitle = null;

        final String[] lines = Patterns.NEWLINE_PATTERN.split(msg);

        for (String line : lines) {
            if (line.startsWith("# ")) {
                builder.setTitle((title != null ? title : "") + line.substring(2));
            } else if (line.startsWith("## ")) {
                if (fieldTitle != null) {
                    builder.addField(fieldTitle, sb.toString().trim(), false);
                } else {
                    builder.setDescription(sb.toString().trim());
                }

                sb.setLength(0);

                fieldTitle = line.substring(3);
            } else if (line.startsWith("### ")) {
                builder.setFooter(line.substring(4));
            } else {
                sb.append(line).append("\n");
            }
        }

        if (fieldTitle != null) {
            builder.addField(fieldTitle, sb.toString().trim(), false);
        } else {
            builder.setDescription(sb.toString().trim());
        }

        return builder;
    }
}
