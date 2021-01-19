package io.github.seggan.segganbot;

import com.besaba.revonline.pastebinapi.paste.Paste;
import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import com.besaba.revonline.pastebinapi.response.Response;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Listener extends ListenerAdapter {
    private static final Pattern INCORRECT_SLIMEFUN_PATTERN = Pattern.compile("[Ss]lime(?:F|( [Ff]))un");
    private static final Pattern ERROR_PATTERN = Pattern.compile("(\\..+(Exception|Error): ')[\\s\\S]+(at .+(\\(.+\\.java:[1-9]+\\)))");

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
        if (ERROR_PATTERN.matcher(e.getMessage().getContentRaw()).find()) {
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
        Matcher matcher = INCORRECT_SLIMEFUN_PATTERN.matcher(msg);
        while (matcher.find()) {
            Util.sendMessage(e.getChannel(), String.format(
                "%s It's Slimefun, not \"%s\"",
                e.getAuthor().getAsMention(),
                matcher.group()
            ));
        }
    }

    private static void processWalshbot(MessageReceivedEvent e) {
        if (e.getMessage().getContentRaw().toLowerCase().contains("walshbot")) {
            Util.sendMessage(e.getChannel(), "Pfft, I am *obviously* better than that rusty old robot blob.");
        }

        MessageChannel channel = e.getChannel();
        User author = e.getAuthor();

        Member member = e.getMember();
        if (!e.getGuild().getSelfMember().canInteract(member)) return;

        if (member.getEffectiveName().toLowerCase().contains("walshbot")) {
            member.modifyNickname(author.getName()).queue();
            channel.deleteMessageById(e.getMessageId()).queue();
            Util.sendMessage(channel, "Hey, who let you in here?");
        }
    }

    private void processUpdateCommand(MessageReceivedEvent e) {
        String text = e.getMessage().getContentRaw();
        TextChannel channel = e.getGuild().getTextChannelById(800907598051541002L);
        assert channel != null;
        if (!(text.startsWith("!update") ||
            e.getMember().getRoles().contains(e.getGuild().getRoleById(799303481433260082L)))) {
            return;
        }

        EmbedBuilder embedObj = Util.parseMessage(null, text.replace("!update", ""));

        channel.sendMessage(embedObj.build()).queue();
    }
}
