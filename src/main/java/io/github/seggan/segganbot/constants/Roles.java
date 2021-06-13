package io.github.seggan.segganbot.constants;

import io.github.seggan.segganbot.Main;
import net.dv8tion.jda.api.entities.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Roles {
    ADDON_CREATORS(809182521316343818L),
    MUTED(809220467717177356L),
    ADMIN(809219516411805747L),
    STAFF(809182558796906507L);

    private final long id;

    public Role getRole() {
        return Main.jda.getRoleById(id);
    }
}
