package io.github.seggan.segganbot;

import com.besaba.revonline.pastebinapi.Pastebin;
import com.besaba.revonline.pastebinapi.impl.factory.PastebinFactory;
import com.besaba.revonline.pastebinapi.paste.Paste;
import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import com.besaba.revonline.pastebinapi.response.Response;
import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import io.github.seggan.segganbot.commands.AbstractSlashCommand;
import io.github.seggan.segganbot.commands.AdminCommand;
import io.github.seggan.segganbot.commands.CommandActions;
import io.github.seggan.segganbot.constants.Channels;
import io.github.seggan.segganbot.constants.Patterns;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;

@Getter
public final class Listener extends ListenerAdapter {


    private final Map<String, Function<AdminCommand, MessageEmbed>> commands = new HashMap<>();
    public static final Set<AbstractAdminCommand> adminCommands = new HashSet<>();


    private final PastebinFactory factory = new PastebinFactory();
    private final Pastebin pastebin = factory.createPastebin(Main.config.get("pastebin").getAsString());

    public Listener() {
        commands.put("!warnings", CommandActions.warningsCommand());
        commands.put("?tags", CommandActions.tagsCommand(this));
        commands.put("?help", CommandActions.tagsCommand(this));
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("Welcome " + e.getUser().getAsTag() + "!")
            .setThumbnail(e.getUser().getEffectiveAvatarUrl())
            .setColor(Color.GREEN)
            .setDescription("Welcome to the Slimefun Addon Community Server!\n\nIf you want help with a specific addon, go to its respective channel. Addon updates can be found in " + Channels.CHANGELOGS.getChannel().getAsMention());
        Channels.WELCOMES.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        for (AbstractSlashCommand command : Main.slashCommands) {
            if (command.getName().equals(event.getName())) {
                if (command.canExecute(event.getMember())) {
                    command.execute(event, event.getHook());
                    break;
                } else {
                    event.reply("You don;t have the required permissions to execute this command");
                }
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        if (processMutes(e)) {
            return;
        }

        AdminCommand command = AdminCommand.parse(e);
        System.out.println(command);
        bigIf:
        if (command != null) {
            for (AbstractAdminCommand adminCommand : adminCommands) {
                if (adminCommand.getName().equals(command.command().replace("!", ""))) {
                    adminCommand.startExecution(e.getMessage());
                    break bigIf;
                }
            }
            String result = Main.tags.get(command.command());
            if (result == null) {
                Function<AdminCommand, MessageEmbed> function = commands.get(command.command());
                if (function != null) {
                    MessageEmbed embed = function.apply(command);
                    if (embed != null) {
                        e.getChannel().sendMessage(embed).queue();
                    }
                }
            } else {
                if (result.contains("#")) {
                    e.getChannel().sendMessage(Util.parseMessage(null, result.replace("\\n", "\n")).build()).queue();
                } else {
                    e.getChannel().sendMessage(result).queue();
                }
            }
        }

        processErrors(e);
        processIncorrectSlimefun(e);
        processUpdates(e);
    }

    private void processErrors(MessageReceivedEvent e) {
        if (Patterns.ERROR_PATTERN.matcher(e.getMessage().getContentRaw()).find()) {
            Paste paste = factory.createPaste()
                .setTitle("Message Contents")
                .setRaw(e.getMessage().getContentRaw())
                .setMachineFriendlyLanguage("text")
                .setExpire(PasteExpire.OneWeek)
                .setVisiblity(PasteVisiblity.Public)
                .build();

            Response<String> response = pastebin.post(paste);

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

    private void processIncorrectSlimefun(MessageReceivedEvent e) {
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

    private void processUpdates(MessageReceivedEvent e) {
        MessageChannel channel = e.getChannel();
        if (channel.getIdLong() != Channels.CHANGELOGS.getId()) {
            return;
        }

        Message message = e.getMessage();
        message.delete().queue();

        EmbedBuilder embedObj = Util.parseMessage(null, message.getContentRaw());
        channel.sendMessage(embedObj.build()).queue();
    }

    private boolean processMutes(MessageReceivedEvent e) {
        Member member = e.getMember();
        if (member == null) return false;

        if (member.getRoles().contains(e.getGuild().getRoleById(Roles.MUTED.getId()))) {
            e.getMessage().delete().queue();
            return true;
        }

        return false;
    }
}
