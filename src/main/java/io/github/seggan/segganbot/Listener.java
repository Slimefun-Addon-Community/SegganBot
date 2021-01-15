package io.github.seggan.segganbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Listener extends ListenerAdapter {
    private static final Pattern INCORRECT_SLIMEFUN_PATTERN = Pattern.compile("[Ss]lime(?:F|( [Ff]))un");

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        processWalshbot(e);
        if (e.getAuthor().isBot()) {
            return;
        }

        if (e.getMessage().getContentRaw().equals("?ping")) {
            Util.sendMessage(e.getChannel(), "Pong!");
        }

        processIncorrectSlimefun(e);
    }

    private static void processIncorrectSlimefun(MessageReceivedEvent e) {
        String msg = e.getMessage().getContentRaw();
        Matcher matcher = INCORRECT_SLIMEFUN_PATTERN.matcher(msg);
        while (matcher.find()) {
            Util.sendMessage(e.getChannel(), String.format(
                "%s It's Slimefun, not \"%s\"",
                e.getAuthor().getAsTag(),
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
        Guild guild = e.getGuild();

        Member member = guild.getMember(author);
        if (member == null || !guild.getSelfMember().canInteract(member)) return;

        if (member.getEffectiveName().toLowerCase().contains("walshbot")) {
            member.modifyNickname(author.getName()).queue();
            channel.deleteMessageById(e.getMessageId()).queue();
            Util.sendMessage(channel, "Hey, who let you in here?");
        }
    }
}
