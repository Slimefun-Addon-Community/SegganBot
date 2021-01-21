package io.github.seggan.segganbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Roles {
    ADDON_CREATORS(799303481433260082L),
    MUTED(801882883949723668L),
    STAFF(801108958156685322L);

    private final long id;
}
