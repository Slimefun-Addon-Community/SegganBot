package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class KickCommand extends AbstractAdminCommand {

    public KickCommand() {
        super("kick", "<user:int> <reason:string...>");
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull ListOrderedMap<String, String> args, @NotNull Member member) {
        Member toBeKicked = message.getGuild().getMemberById(args.get("user"));
        if (toBeKicked == null) {
            message.getChannel().sendMessage("Invalid user id").queue();
            return;
        }

        member.kick(args.get("reason")).queue();

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("User Kicked!")
            .setDescription(String.format(
                "%s has been kicked by %s for: `%s`",
                toBeKicked.getAsMention(),
                member.getAsMention(),
                args.get("reason")
            ))
            .setColor(Color.RED);
        message.getChannel().sendMessage(builder.build()).queue();
    }
}
