package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Listener;
import io.github.seggan.segganbot.MongoUtil;
import io.github.seggan.segganbot.Util;
import io.github.seggan.segganbot.Warning;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bson.Document;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CommandActions {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private CommandActions() {
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

            listener.getWarnings().add(warning);
            MongoUtil.addWarning(listener.getWarningDb(), warning);

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

            for (Warning warning : listener.getWarnings()) {
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

    public static Function<Command, MessageEmbed> setCommandCommand(Listener listener) {
        return command -> {
            Message message = command.getMessage();
            Member member = message.getMember();
            if (!member.getRoles().contains(member.getGuild().getRoleById(Roles.ADDON_CREATORS.getId())) ||
                command.getArguments().length < 2) {
                return null;
            }

            String[] args = command.getArguments();

            String embed = message.getContentRaw().replaceFirst(Pattern.quote(command.getCommand()), "")
                .replaceFirst(Pattern.quote(args[0]), "")
                .trim();

            loop:
            {
                Document d = new Document("_id", args[0]);
                for (Document document : listener.getCommandsDb().find()) {
                    if (document.get("_id").equals(args[0])) {
                        listener.getCommandsDb().replaceOne(document, d.append("message", embed));
                        break loop;
                    }
                }
                listener.getCommandsDb().insertOne(d.append("message", embed));
            }

            listener.getTags().put(args[0], embed);

            return Util.parseMessage(null, embed).build();
        };
    }

    public static Function<Command, MessageEmbed> tagsCommand(Listener listener) {
        return command -> {
            Set<? extends String> set = new HashSet<>(listener.getCommands().keySet());
            set.removeIf(s -> s.charAt(0) != '?');

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("\uD83C\uDFF7 Available tags:")
                .setDescription("`" + String.join("`, `", listener.getTags().keySet()) + "`, `" +
                    String.join("`, `", set) + "`");

            return builder.build();
        };
    }

    public static Function<Command, MessageEmbed> banCommand(Listener listener) {
        return command -> {
            Guild guild = command.getMessage().getGuild();
            Message message = command.getMessage();
            if (!message.getMember().getRoles()
                .contains(guild.getRoleById(Roles.STAFF.getId()))) {
                return null;
            }

            String[] args = command.getArguments();

            int days;
            try {
                days = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                days = 0;
            }

            String reason = String.join(
                " ",
                Arrays.copyOfRange(args, 2, args.length)
            );

            Member member;
            List<Member> members = message.getMentionedMembers();
            if (members.size() > 0) {
                member = members.get(0);
            } else {
                return null;
            }

            AtomicReference<MessageEmbed> embed = new AtomicReference<>();

            member.ban(days, reason).queue(success -> {
                EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("User Banned!")
                    .setDescription(String.format(
                        "%s has been banned by %s for: `%s`\n\nThe user is now gone forever!",
                        member.getAsMention(),
                        message.getAuthor().getAsMention(),
                        reason
                    ))
                    .setColor(Color.RED);
                embed.set(builder.build());
            }, System.out::println);

            return embed.get();
        };
    }
}
