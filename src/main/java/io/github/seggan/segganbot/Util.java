package io.github.seggan.segganbot;

import io.github.seggan.segganbot.constants.Patterns;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class Util {
    private Util() {
    }

    public static String getFileAsString(@Nonnull File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    public static long getMillisFromString(String s) {
        TimeUnit unit;
        char last = s.charAt(s.length() - 1);
        switch (last) {
            case 's':
                unit = TimeUnit.SECONDS;
                break;
            case 'm':
                unit = TimeUnit.MINUTES;
                break;
            case 'h':
                unit = TimeUnit.HOURS;
                break;
            case 'd':
                unit = TimeUnit.DAYS;
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid unit: '%c`", last));
        }

        return TimeUnit.MILLISECONDS.convert(Long.parseLong(s.replace(String.valueOf(last), "")), unit);
    }

    public static String getTimeUnitName(char c) {
        switch (c) {
            case 's':
                return "seconds";
            case 'm':
                return "minutes";
            case 'h':
                return "hours";
            case 'd':
                return "days";
            default:
                throw new IllegalArgumentException(String.format("Invalid unit: '%c`", c));
        }
    }
}
