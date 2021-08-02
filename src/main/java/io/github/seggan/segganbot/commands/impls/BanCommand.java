package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class BanCommand extends AbstractAdminCommand {

    public BanCommand() {
        super("ban", "<user:int> <days:int> <reason:string...>");
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull ListOrderedMap<String, String> args, @NotNull Member member) {
        int days;
        try {
            days = Integer.parseInt(args.get("days"));
        } catch (NumberFormatException e) {
            days = 0;
        }

        Member toBeBanned = message.getGuild().getMemberById(args.get("user"));
        if (toBeBanned == null) {
            message.getChannel().sendMessage("Invalid user id").queue();
            return;
        }

        member.ban(days, args.get("reason")).queue();

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("User Banned!")
            .setDescription(String.format(
                "%s has been banned by %s for: `%s`\n\nThe user is now gone forever!",
                toBeBanned.getAsMention(),
                member.getAsMention(),
                args.get("reason")
            ))
            .setColor(Color.RED);
        message.getChannel().sendMessage(builder.build()).queue();
    }
}
