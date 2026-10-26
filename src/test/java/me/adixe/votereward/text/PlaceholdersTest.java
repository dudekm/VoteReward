package me.adixe.votereward.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholdersTest {
    @Test
    void replacesKnownAndLeavesUnknown() {
        Placeholders placeholders = Placeholders.of("a", 1).with("b", "two");

        assertEquals("1 two %c%", placeholders.apply("%a% %b% %c%"));
    }

    @Test
    void isImmutable() {
        Placeholders base = Placeholders.of("a", 1);
        base.with("a", 2);

        assertEquals("1", base.apply("%a%"));
    }

    @Test
    void mergesWithLaterValuesWinning() {
        assertEquals("2", Placeholders.of("a", 1).with(Placeholders.of("a", 2)).apply("%a%"));
    }
}
