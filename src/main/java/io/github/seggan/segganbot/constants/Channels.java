package io.github.seggan.segganbot.constants;

import io.github.seggan.segganbot.Main;
import net.dv8tion.jda.api.entities.TextChannel;

import lombok.Getter;

@Getter
public enum Channels {
    CHANGELOGS(809186242746253363L),
    ANNOUNCEMENTS(809186153139798066L),
    BOT_TESTING(809225805322125394L),
    WELCOMES(809183642651328563L);

    private final long id;
    private final TextChannel channel;

    Channels(long id) {
        this.id = id;
        this.channel = Main.jda.getTextChannelById(this.id);
    }
}
