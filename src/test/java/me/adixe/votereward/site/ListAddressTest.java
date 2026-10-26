package me.adixe.votereward.site;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListAddressTest {
    @Test
    void stripsTrailingSlashes() {
        assertEquals("https://lista-minecraft.pl", ListAddress.of("https://lista-minecraft.pl//").toString());
    }

    @Test
    void resolvesApiPaths() {
        assertEquals("https://lista-minecraft.pl/api/v1/servers/x/votes",
                ListAddress.of("https://lista-minecraft.pl/").resolve("/api/v1/servers/x/votes").toString());
    }

    @Test
    void acceptsHttpForLocalDevelopment() {
        assertEquals("localhost", ListAddress.of("http://localhost:8080").host());
    }

    @Test
    void rejectsInvalidAddresses() {
        assertThrows(IllegalArgumentException.class, () -> ListAddress.of("lista-minecraft.pl"));
        assertThrows(IllegalArgumentException.class, () -> ListAddress.of("ftp://lista-minecraft.pl"));
        assertThrows(IllegalArgumentException.class, () -> ListAddress.of("https://lista-minecraft.pl/?a=b"));
        assertThrows(IllegalArgumentException.class, () -> ListAddress.of(""));
    }

    @Test
    void equalByAddress() {
        assertEquals(ListAddress.of("https://a.pl/"), ListAddress.of("https://a.pl"));
    }
}
