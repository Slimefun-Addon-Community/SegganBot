package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.Util;
import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends AbstractAdminCommand {

    public MuteCommand() {
        super("mute", "<user:int> <time:string>");
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull Map<String, String> args, @NotNull String content, @NotNull Member member) {
        Guild guild = message.getGuild();
        if (!member.getRoles().contains(guild.getRoleById(Roles.STAFF.getId()))) {
            return;
        }

        long time;
        try {
            time = Util.getMillisFromString(args.get("time"));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            time = Long.MAX_VALUE;
        }

        Member toBeMuted = guild.getMemberById(Long.parseLong(args.get("user")));
        assert toBeMuted != null;

        Role muted = Roles.MUTED.getRole();
        guild.addRoleToMember(toBeMuted, muted).queue();

        if (time != Long.MAX_VALUE) {
            guild.removeRoleFromMember(toBeMuted, muted)
                .queueAfter(time, TimeUnit.MILLISECONDS);
        }

        String timeString = args.get("time");

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("User Muted!")
            .setDescription(String.format(
                "%s has been muted by %s.\nDuration: %s %s",
                toBeMuted.getAsMention(),
                member.getAsMention(),
                timeString.substring(0, timeString.length() - 1),
                Util.getTimeUnitName(timeString.charAt(timeString.length() - 1))
            ))
            .setColor(Color.RED);

        message.getChannel().sendMessage(builder.build()).queue();
    }
}