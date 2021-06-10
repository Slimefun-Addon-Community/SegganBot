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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CommandActions {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private CommandActions() {
    }

    public static Function<Command, MessageEmbed> warnCommand(Listener listener) {
        return cmd -> {
            Guild guild = cmd.getMessage().getGuild();
            Message message = cmd.getMessage();
            if (!message.getMember().getRoles()
                .contains(guild.getRoleById(Roles.STAFF.getId()))) {
                return null;
            }

            String[] args = cmd.getArguments();

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
        return cmd -> {
            Member member;
            List<Member> members = cmd.getMessage().getMentionedMembers();
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

    public static Function<Command, MessageEmbed> setTagCommand(Listener listener) {
        return cmd -> {
            Message message = cmd.getMessage();
            Member member = message.getMember();
            if (!member.getRoles().contains(member.getGuild().getRoleById(Roles.STAFF.getId())) ||
                cmd.getArguments().length < 2) {
                return null;
            }

            String[] args = cmd.getArguments();

            String embed = message.getContentRaw().replaceFirst(Pattern.quote(cmd.getCommand()), "")
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
        return cmd -> {
            Set<? extends String> set = new HashSet<>(listener.getCommands().keySet());
            set.removeIf(s -> s.charAt(0) != '?');

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("\uD83C\uDFF7 Available tags:")
                .setDescription("`" + String.join("`, `", listener.getTags().keySet()) + "`, `" +
                    String.join("`, `", set) + "`");

            return builder.build();
        };
    }

    public static Function<Command, MessageEmbed> banCommand() {
        return cmd -> {
            Guild guild = cmd.getMessage().getGuild();
            Message message = cmd.getMessage();
            if (!message.getMember().getRoles()
                .contains(guild.getRoleById(Roles.STAFF.getId()))) {
                return null;
            }

            String[] args = cmd.getArguments();

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

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("User Banned!")
                .setDescription(String.format(
                    "%s has been banned by %s for: `%s`\n\nThe user is now gone forever!",
                    member.getAsMention(),
                    message.getAuthor().getAsMention(),
                    reason
                ))
                .setColor(Color.RED);

            member.ban(days, reason).queue();

            return builder.build();
        };
    }

    public static Function<Command, MessageEmbed> kickCommand() {
        return cmd -> {
            Guild guild = cmd.getMessage().getGuild();
            Message message = cmd.getMessage();
            if (!message.getMember().getRoles()
                .contains(guild.getRoleById(Roles.STAFF.getId()))) {
                return null;
            }

            String[] args = cmd.getArguments();

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

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("User Kicked!")
                .setDescription(String.format(
                    "%s has been kicked by %s for: `%s`",
                    member.getAsMention(),
                    message.getAuthor().getAsMention(),
                    reason
                ))
                .setColor(Color.RED);

            member.kick(reason).queue();

            return builder.build();
        };
    }

    public static Function<Command, MessageEmbed> muteCommand() {
        return cmd -> {
            Guild guild = cmd.getMessage().getGuild();
            Message message = cmd.getMessage();
            if (!message.getMember().getRoles()
                .contains(guild.getRoleById(Roles.STAFF.getId()))) {
                return null;
            }

            String[] args = cmd.getArguments();

            String reason = String.join(
                " ",
                Arrays.copyOfRange(args, 2, args.length)
            );

            Member member;
            List<Member> members = message.getMentionedMembers();
            if (!members.isEmpty()) {
                member = members.get(0);
            } else {
                return null;
            }

            long time;
            try {
                time = Util.getMillisFromString(args[1]);
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                time = Long.MAX_VALUE;
            }

            guild.addRoleToMember(member, guild.getRoleById(Roles.MUTED.getId())).queue();

            if (time != Long.MAX_VALUE) {
                guild.removeRoleFromMember(member, guild.getRoleById(Roles.MUTED.getId()))
                    .queueAfter(time, TimeUnit.MILLISECONDS);
            }

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("User Muted!")
                .setDescription(String.format(
                    "%s has been muted by %s. Reason: `%s`\n\nDuration: %s %s",
                    member.getAsMention(),
                    message.getAuthor().getAsMention(),
                    reason,
                    args[1].substring(0, args[1].length() - 1),
                    Util.getTimeUnitName(args[1].charAt(args[1].length() - 1))
                ))
                .setColor(Color.RED);

            return builder.build();
        };
    }
}
