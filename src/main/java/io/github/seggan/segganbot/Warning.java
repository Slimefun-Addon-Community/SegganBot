package io.github.seggan.segganbot;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public final class Warning {
    private final long playerId;
    private final Instant time;
    private final String reason;
}
