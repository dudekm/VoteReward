package me.adixe.votereward.vote;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * The vote lists allow one vote per player per day and count that day in UTC,
 * so the "once a day" reward rule uses the same calendar.
 */
public final class VoteDay {
    private VoteDay() {
    }

    public static LocalDate of(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneOffset.UTC);
    }
}
