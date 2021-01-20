package io.github.seggan.segganbot.constants;

import io.github.seggan.segganbot.Main;
import lombok.Getter;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
public enum Channels {
    ADDON_ANNOUNCEMENTS(800907598051541002L),
    WELCOMES(799294416626909186L);

    private final long id;
    private final TextChannel channel;

    Channels(long id) {
        this.id = id;
        this.channel = Main.jda.getTextChannelById(this.id);
    }
}
