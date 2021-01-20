package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Listener;
import io.github.seggan.segganbot.constants.Channels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;
import java.util.function.Function;

public final class Welcomes {
    private Welcomes() {
    }

    public static Function<GuildMemberJoinEvent, MessageEmbed> onJoin(Listener listener) {
        return e -> {
            EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Welcome " + e.getUser().getAsTag() + "!")
                .setThumbnail(e.getUser().getEffectiveAvatarUrl())
                .setColor(Color.GREEN)
                .setDescription("Welcome to the Slimefun Addon Community Server!\n\nIf you want help with a specific addon, go to its respective channel. Addon updates can be found in " + Channels.ADDON_ANNOUNCEMENTS.getChannel().getAsMention());
            return builder.build();
        };
    }
}
