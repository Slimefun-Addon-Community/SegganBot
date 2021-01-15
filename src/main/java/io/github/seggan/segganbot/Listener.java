package io.github.seggan.segganbot;

import net.dv8tion.jda.api.entities.Member;
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
            Util.sendMessage(e.getChannel(), "Pfft, I am *obviously* better than that old rusty robot blob.");
        }

        Member member = e.getGuild().getMember(e.getAuthor());
        if (member == null || !e.getGuild().getSelfMember().canInteract(member)) return;

        if (member.getEffectiveName().toLowerCase().contains("walshbot")) {
            member.modifyNickname(e.getAuthor().getName()).queue();
            e.getChannel().deleteMessageById(e.getMessageId()).queue();
            Util.sendMessage(e.getChannel(), "Hey, who let you in here?");
        }
    }
}
