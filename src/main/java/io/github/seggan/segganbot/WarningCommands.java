package io.github.seggan.segganbot;

import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class WarningCommands {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private WarningCommands() {
    }

    public static Function<Command, MessageEmbed> warnCommand(Listener listener) {
        return command -> {
            Guild guild = command.getMessage().getGuild();
            Message message = command.getMessage();
            if (!message.getMember().getRoles()
                .contains(guild.getRoleById(Roles.ADDON_CREATORS.getId()))) {
                return null;
            }

            String[] args = command.getArguments();

            String reason = String.join(
                " ",
                Arrays.copyOfRange(args, 1, args.length)
            );

            Member member;
            List<Member> members = message.getMentionedMembers();
            if (members.size() > 0) {
                member = members.get(0);
            } else {
                return null;
            }

            System.out.println(Instant.now());
            System.out.println(reason);

            Warning warning = new Warning(member.getIdLong(), Instant.now(), reason);

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("User Warned!")
                .setDescription(String.format(
                    "%s has been warned by %s for: `%s`",
                    member.getAsMention(),
                    message.getAuthor().getAsMention(),
                    reason
                ))
                .setColor(Color.RED);

            listener.warnings.add(warning);
            MongoUtil.addWarning(Listener.warningDb, warning);

            return builder.build();
        };
    }

    public static Function<Command, MessageEmbed> warningsCommand(Listener listener) {
        return command -> {
            Member member;
            List<Member> members = command.getMessage().getMentionedMembers();
            if (members.size() > 0) {
                member = members.get(0);
            } else {
                return null;
            }

            List<Warning> memberWarnings = new ArrayList<>();

            for (Warning warning : listener.warnings) {
                if (warning.getPlayerId() == member.getIdLong()) {
                    memberWarnings.add(warning);
                }
            }

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle(member.getEffectiveName() + "'s Warnings")
                .setColor(Color.RED);

            for (Warning warning : memberWarnings) {
                builder.addField(
                    "Warning on " + formatter.format(warning.getTime()) + " UTC",
                    warning.getReason(),
                    false
                );
            }

            return builder.build();
        };
    }
}
