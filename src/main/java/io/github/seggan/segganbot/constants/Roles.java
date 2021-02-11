package io.github.seggan.segganbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Roles {
    ADDON_CREATORS(809182521316343818L),
    MUTED(809220467717177356L),
    STAFF(809182558796906507L);

    private final long id;
}
