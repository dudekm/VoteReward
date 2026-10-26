package me.adixe.votereward.vote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerIdTest {
    @Test
    void parsesUuid() {
        assertEquals("183413bc-4588-4265-8f06-346963e5d7df",
                ServerId.of(" 183413bc-4588-4265-8f06-346963e5d7df ").toString());
    }

    @Test
    void rejectsAnythingElse() {
        assertThrows(IllegalArgumentException.class, () -> ServerId.of("not-a-uuid"));
        assertThrows(IllegalArgumentException.class, () -> ServerId.of(""));
        assertThrows(IllegalArgumentException.class, () -> ServerId.of(null));
    }
}
