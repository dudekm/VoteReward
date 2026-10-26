package me.adixe.votereward.vote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NicknameTest {
    @Test
    void equalityIgnoresLetterCase() {
        assertEquals(Nickname.of("Steve"), Nickname.of("sTEVE"));
        assertEquals(Nickname.of("Steve").hashCode(), Nickname.of("steve").hashCode());
        assertNotEquals(Nickname.of("Steve"), Nickname.of("Alex"));
    }

    @Test
    void keepsTheOriginalSpellingAndTrimsSpaces() {
        Nickname nickname = Nickname.of("  Steve_01 ");

        assertEquals("Steve_01", nickname.value());
        assertEquals("steve_01", nickname.key());
    }

    @Test
    void rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> Nickname.of(" "));
        assertThrows(IllegalArgumentException.class, () -> Nickname.of(null));
    }
}
