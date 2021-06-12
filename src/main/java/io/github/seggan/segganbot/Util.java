package io.github.seggan.segganbot;

import io.github.seggan.segganbot.constants.Patterns;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
public final class Util {

    public static String getFileAsString(@Nonnull File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            } else if (line.startsWith("- ")) {
                sb.append("\u2022").append(Patterns.DASH.matcher(line).replaceFirst("")).append("\n");
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
        unit = switch (last) {
            case 's' -> TimeUnit.SECONDS;
            case 'm' -> TimeUnit.MINUTES;
            case 'h' -> TimeUnit.HOURS;
            case 'd' -> TimeUnit.DAYS;
            default -> throw new IllegalArgumentException(String.format("Invalid unit: '%c`", last));
        };

        return TimeUnit.MILLISECONDS.convert(Long.parseLong(s.replace(String.valueOf(last), "")), unit);
    }

    public static String getTimeUnitName(char c) {
        return switch (c) {
            case 's' -> "seconds";
            case 'm' -> "minutes";
            case 'h' -> "hours";
            case 'd' -> "days";
            default -> throw new IllegalArgumentException(String.format("Invalid unit: '%c`", c));
        };
    }
}
