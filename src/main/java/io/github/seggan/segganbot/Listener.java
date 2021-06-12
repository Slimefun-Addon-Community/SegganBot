package io.github.seggan.segganbot;

import com.besaba.revonline.pastebinapi.Pastebin;
import com.besaba.revonline.pastebinapi.impl.factory.PastebinFactory;
import com.besaba.revonline.pastebinapi.paste.Paste;
import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import com.besaba.revonline.pastebinapi.response.Response;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.seggan.segganbot.commands.Command;
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
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

    private final Map<String, String> tags = new HashMap<>();
    private final Set<Warning> warnings = new HashSet<>();
    private final Map<String, Function<Command, MessageEmbed>> commands = new HashMap<>();

    private final MongoCollection<Document> warningDb;
    private final MongoCollection<Document> commandsDb;
    private final PastebinFactory factory = new PastebinFactory();
    private final Pastebin pastebin = factory.createPastebin(Main.config.get("pastebin").getAsString());

    public Listener() {
        MongoClient mongoClient = MongoClients.create(
            "mongodb+srv://SegganBot:" + Main.config.get("mongo").getAsString() + "@cluster0.9lcjl.mongodb.net/<dbname>?retryWrites=true&w=majority");
        MongoDatabase database = mongoClient.getDatabase("segganbot");

        warningDb = database.getCollection("warnings");

        for (Document document : warningDb.find()) {
            warnings.add(MongoUtil.deserializeWarning(document));
        }

        commands.put("!warn", CommandActions.warnCommand(this));
        commands.put("!warnings", CommandActions.warningsCommand(this));
        commands.put("!settag", CommandActions.setTagCommand(this));
        commands.put("!ban", CommandActions.banCommand());
        commands.put("!kick", CommandActions.kickCommand());
        commands.put("!mute", CommandActions.muteCommand());
        commands.put("?tags", CommandActions.tagsCommand(this));
        commands.put("?help", CommandActions.tagsCommand(this));

        commandsDb = database.getCollection("commands");
        for (Document document : commandsDb.find()) {
            tags.put(document.getString("_id"), document.getString("message"));
        }
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
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        if (processMutes(e)) {
            return;
        }

        Command command = Command.parse(e);
        System.out.println(command);
        if (command != null) {
            String result = tags.get(command.command());
            if (result == null) {
                Function<Command, MessageEmbed> function = commands.get(command.command());
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
