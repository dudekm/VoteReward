package me.adixe.votereward.config;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses human-friendly durations from the configuration: {@code 30s}, {@code 5m}, {@code 1h},
 * {@code 7d}, combinations such as {@code 1h30m}, or a bare number of seconds ({@code 30}).
 */
public final class Durations {
    private static final Pattern PART = Pattern.compile("(\\d+)\\s*(ms|s|m|h|d)");

    private Durations() {
    }

    public static Duration parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Duration is empty.");
        }

        String value = raw.trim().toLowerCase(Locale.ROOT);

        if (value.chars().allMatch(Character::isDigit)) {
            return Duration.ofSeconds(Long.parseLong(value));
        }

        Matcher matcher = PART.matcher(value);
        Duration total = Duration.ZERO;
        int consumed = 0;

        while (matcher.find()) {
            if (!value.substring(consumed, matcher.start()).isBlank()) {
                break;
            }

            long amount = Long.parseLong(matcher.group(1));

            total = total.plus(switch (matcher.group(2)) {
                case "ms" -> Duration.ofMillis(amount);
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                default -> Duration.ofDays(amount);
            });
            consumed = matcher.end();
        }

        if (consumed == 0 || !value.substring(consumed).isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid duration \"" + raw + "\" — use e.g. 30s, 5m, 1h, 7d.");
        }

        return total;
    }

    /** The shortest readable form, e.g. {@code 90s} → {@code 1m30s}. */
    public static String format(Duration duration) {
        long seconds = duration.getSeconds();

        if (seconds == 0) {
            return duration.toMillis() + "ms";
        }

        StringBuilder builder = new StringBuilder();
        long days = seconds / 86_400;
        long hours = seconds % 86_400 / 3_600;
        long minutes = seconds % 3_600 / 60;
        long rest = seconds % 60;

        if (days > 0) {
            builder.append(days).append('d');
        }

        if (hours > 0) {
            builder.append(hours).append('h');
        }

        if (minutes > 0) {
            builder.append(minutes).append('m');
        }

        if (rest > 0) {
            builder.append(rest).append('s');
        }

        return builder.toString();
    }
}
