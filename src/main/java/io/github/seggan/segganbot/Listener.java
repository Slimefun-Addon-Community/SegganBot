package io.github.seggan.segganbot;

import com.besaba.revonline.pastebinapi.paste.Paste;
import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import com.besaba.revonline.pastebinapi.response.Response;
import com.google.gson.Gson;
import io.github.seggan.segganbot.constants.Channels;
import io.github.seggan.segganbot.constants.Patterns;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

public final class Listener extends ListenerAdapter {

    public static final Map<String, String> tags = new HashMap<>();
    public static List<Warning> warnings = new ArrayList<>();
    private static final Map<String, Function<Command, MessageEmbed>> commands = new HashMap<>();

    static {
        commands.put("!warn", command -> {
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

            warnings.add(warning);
            try (FileOutputStream stream = new FileOutputStream("warnings.json")) {
                Gson gson = new Gson();
                stream.write(gson.toJson(warnings).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return builder.build();
        });
        commands.put("!warnings", command -> {
            Member member;
            List<Member> members = command.getMessage().getMentionedMembers();
            if (members.size() > 0) {
                member = members.get(0);
            } else {
                return null;
            }

            List<Warning> memberWarnings = new ArrayList<>();

            for (Warning warning : warnings) {
                if (warning.getPlayerId() == member.getIdLong()) {
                    memberWarnings.add(warning);
                }
            }

            EmbedBuilder builder = new EmbedBuilder()
                .setTitle(member.getEffectiveName() + "'s Warnings")
                .setColor(Color.RED);


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .withLocale(Locale.US)
                .withZone(ZoneId.from(ZoneOffset.UTC));
            for (Warning warning : memberWarnings) {
                builder.addField(
                    "Warning on " + formatter.format(warning.getTime()) + " UTC",
                    warning.getReason(),
                    false
                );
            }

            return builder.build();
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        Command command = Command.parse(e);
        System.out.println(command);
        if (command != null) {
            String result = tags.get(command.getCommand());
            if (result == null) {
                Function<Command, MessageEmbed> function = commands.get(command.getCommand());
                if (function != null) {
                    MessageEmbed embed = function.apply(command);
                    if (embed != null) {
                        e.getChannel().sendMessage(embed).queue();
                    }
                }
            } else {
                if (result.contains("#")) {
                    e.getChannel().sendMessage(Util.parseMessage(null, result).build()).queue();
                } else {
                    e.getChannel().sendMessage(result).queue();
                }
            }
        }

        processErrors(e);
        processIncorrectSlimefun(e);
        processUpdates(e);
    }

    private static void processErrors(MessageReceivedEvent e) {
        if (Patterns.ERROR_PATTERN.matcher(e.getMessage().getContentRaw()).find()) {
            Paste paste = Main.factory.createPaste()
                .setTitle("Message Contents")
                .setRaw(e.getMessage().getContentRaw())
                .setMachineFriendlyLanguage("text")
                .setExpire(PasteExpire.OneWeek)
                .setVisiblity(PasteVisiblity.Public)
                .build();

            Response<String> response = Main.pastebin.post(paste);

            if (response.hasError()) {
                Util.sendMessage(e.getChannel(), "Error in pasting: " + response.getError());
                return;
            }

            e.getMessage().delete().queue();

            @SuppressWarnings("StringBufferReplaceableByString")
            MessageEmbed embed = new EmbedBuilder()
                .setDescription(new StringBuilder()
                    .append(e.getAuthor().getAsMention())
                    .append(" please dont post error logs in here! We recommend you use ")
                    .append("[pastebin](https://pastebin.com) or something similar in the future.\n\n")
                    .append("However, just this once, we did it for you: [")
                    .append(response.get())
                    .append("](")
                    .append(response.get())
                    .append(")")
                    .toString())
                .build();

            e.getChannel().sendMessage(embed).queue();
        }
    }

    private static void processIncorrectSlimefun(MessageReceivedEvent e) {
        String msg = e.getMessage().getContentRaw();
        Matcher matcher = Patterns.INCORRECT_SLIMEFUN_PATTERN.matcher(msg);
        while (matcher.find()) {
            Util.sendMessage(e.getChannel(), String.format(
                "%s It's Slimefun, not \"%s\"",
                e.getAuthor().getAsMention(),
                matcher.group()
            ));
        }
    }

    private static void processUpdates(MessageReceivedEvent e) {
        MessageChannel channel = e.getChannel();
        if (channel.getIdLong() != Channels.ADDON_ANNOUNCEMENTS.getId()) {
            return;
        }

        Message message = e.getMessage();
        message.delete().queue();

        EmbedBuilder embedObj = Util.parseMessage(null, message.getContentRaw());
        channel.sendMessage(embedObj.build()).queue();
    }
}
