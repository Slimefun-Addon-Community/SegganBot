package io.github.seggan.segganbot;

import java.time.Instant;

public record Warning(long playerId, Instant time, String reason) {
}
