package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.Main;
import io.github.seggan.segganbot.MongoUtil;
import io.github.seggan.segganbot.Warning;
import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class WarnCommand extends AbstractAdminCommand {

    public WarnCommand() {
        super("warn", "<user:int> <reason:string...>");
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull ListOrderedMap<String, String> args, @NotNull Member member) {
        Guild guild = message.getGuild();
        Member toBeWarned = guild.getMemberById(args.get("user"));
        if (toBeWarned == null) {
            message.getChannel().sendMessage("Invalid user id").queue();
            return;
        }

        Warning warning = new Warning(toBeWarned.getIdLong(), Instant.now(), args.get("reason"));

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("User Warned!")
            .setDescription(String.format(
                "%s has been warned by %s for: `%s`",
                toBeWarned.getAsMention(),
                member.getAsMention(),
                args.get("reason")
            ))
            .setColor(Color.RED);

        Main.warnings.add(warning);
        MongoUtil.addWarning(Main.warningDb, warning);

        message.getChannel().sendMessage(builder.build()).queue();
    }
}
