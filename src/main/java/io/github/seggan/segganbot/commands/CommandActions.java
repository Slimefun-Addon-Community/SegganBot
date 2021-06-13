package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Listener;
import io.github.seggan.segganbot.Main;
import io.github.seggan.segganbot.Warning;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

@UtilityClass
public class CommandActions {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.from(ZoneOffset.UTC));

    public static Function<AdminCommand, MessageEmbed> warningsCommand() {
        return cmd -> {
            Member member;
            List<Member> members = cmd.message().getMentionedMembers();
            if (members.size() > 0) {
                member = members.get(0);
            } else {
                return null;
            }

            List<Warning> memberWarnings = new ArrayList<>();

            for (Warning warning : Main.warnings) {
                if (warning.playerId() == member.getIdLong()) {
                    memberWarnings.add(warning);
                }
            }

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle(member.getEffectiveName() + "'s Warnings")
                .setColor(Color.RED);

            for (Warning warning : memberWarnings) {
                builder.addField(
                    "Warning on " + formatter.format(warning.time()) + " UTC",
                    warning.reason(),
                    false
                );
            }

            return builder.build();
        };
    }

    public static Function<AdminCommand, MessageEmbed> tagsCommand(Listener listener) {
        return cmd -> {
            Set<? extends String> set = new HashSet<>(listener.getCommands().keySet());
            set.removeIf(s -> s.charAt(0) != '?');

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("\uD83C\uDFF7 Available tags:")
                .setDescription("`" + String.join("`, `", Main.tags.keySet()) + "`, `" +
                    String.join("`, `", set) + "`");

            return builder.build();
        };
    }
}
