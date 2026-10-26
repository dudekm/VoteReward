package me.adixe.votereward.config;

import me.adixe.votereward.site.VoteSites;

import java.time.Duration;
import java.util.Objects;

/**
 * Everything read from {@code settings.yml}.
 *
 * @param pollInterval how often every list is checked for new votes
 * @param sites        the configured vote lists
 */
public record Settings(Duration pollInterval, VoteSites sites) {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    /** The lists rate-limit their API; checking more often gains nothing and risks a block. */
    public static final Duration MIN_POLL_INTERVAL = Duration.ofSeconds(10);

    public Settings {
        Objects.requireNonNull(pollInterval, "pollInterval");
        Objects.requireNonNull(sites, "sites");

        if (pollInterval.compareTo(MIN_POLL_INTERVAL) < 0) {
            throw new IllegalArgumentException("polling.interval must be at least "
                    + Durations.format(MIN_POLL_INTERVAL) + ".");
        }
    }
}
