package me.adixe.votereward.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationsTest {
    @ParameterizedTest
    @CsvSource({
            "30, 30",
            "30s, 30",
            "5m, 300",
            "1h, 3600",
            "1d, 86400",
            "1h30m, 5400",
            "'1m 30s', 90",
            "2M, 120"
    })
    void parses(String raw, long seconds) {
        assertEquals(Duration.ofSeconds(seconds), Durations.parse(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "30x", "s30", "30s abc", "-5s"})
    void rejectsInvalid(String raw) {
        assertThrows(IllegalArgumentException.class, () -> Durations.parse(raw));
    }

    @Test
    void formatsShortestForm() {
        assertEquals("30s", Durations.format(Duration.ofSeconds(30)));
        assertEquals("1m30s", Durations.format(Duration.ofSeconds(90)));
        assertEquals("1d2h", Durations.format(Duration.ofHours(26)));
    }
}
