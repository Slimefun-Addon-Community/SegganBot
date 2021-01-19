package io.github.seggan.segganbot;

import com.besaba.revonline.pastebinapi.paste.Paste;
import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import com.besaba.revonline.pastebinapi.response.Response;
import io.github.seggan.segganbot.constants.Channels;
import io.github.seggan.segganbot.constants.Patterns;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;

public final class Listener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        // processWalshbot(e);
        if (e.getAuthor().isBot()) {
            return;
        }

        for (Map.Entry<String, String> entry : Main.tags.entrySet()) {
            if (e.getMessage().getContentRaw().startsWith(entry.getKey())) {
                Util.sendMessage(e.getChannel(), entry.getValue());
            }
        }

        processErrors(e);
        processIncorrectSlimefun(e);
        processUpdateCommand(e);
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

    private void processUpdateCommand(MessageReceivedEvent e) {
        String text = e.getMessage().getContentRaw();
        TextChannel channel = e.getGuild().getTextChannelById(Channels.ADDON_ANNOUNCEMENTS.getId());
        assert channel != null;
        if (!text.startsWith("!update") ||
            !e.getMember().getRoles().contains(e.getGuild().getRoleById(Roles.ADDON_CREATORS.getId()))) {
            return;
        }

        EmbedBuilder embedObj = Util.parseMessage(null, Patterns.UPDATE_COMMAND_PATTERN.matcher(text).replaceAll(""));

        channel.sendMessage(embedObj.build()).queue();
    }
}
