package me.adixe.votereward.ledger;

import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlLedgerRepositoryTest {
    private static final Instant NOW = Instant.parse("2026-10-25T12:00:00Z");

    @TempDir
    Path folder;

    @Test
    void missingFileMeansEmptyLedger() throws IOException {
        assertEquals(LedgerSnapshot.EMPTY, new YamlLedgerRepository(folder.resolve("ledger.yml")).load());
    }

    @Test
    void savesAndLoadsEverything() throws IOException {
        Path file = folder.resolve("data").resolve("ledger.yml");
        YamlLedgerRepository repository = new YamlLedgerRepository(file);

        VoteLedger ledger = VoteLedger.empty();
        ledger.record("lista-minecraft", List.of(Vote.of("u1", "Steve", NOW)), NOW);
        repository.save(ledger.snapshot());

        assertTrue(Files.exists(file));
        assertFalse(Files.exists(file.resolveSibling("ledger.yml.tmp")));

        LedgerSnapshot loaded = repository.load();
        assertEquals(ledger.snapshot(), loaded);

        VoteLedger restored = VoteLedger.restore(loaded);
        assertEquals("Steve", restored.pendingFor(Nickname.of("steve")).get(0).nickname().value());
        assertTrue(restored.wasRewarded("lista-minecraft", Nickname.of("STEVE"), LocalDate.of(2026, 10, 25)));
    }

    @Test
    void readsUnquotedTimestampsFromHandEditedFiles() throws IOException {
        Path file = folder.resolve("ledger.yml");
        Files.writeString(file, """
                cursors:
                  a:
                    position: 2026-10-25T12:00:00Z
                    recent-votes: {}
                pending: []
                """);

        assertEquals(NOW, new YamlLedgerRepository(file).load().cursors().get("a").position());
    }

    @Test
    void damagedFileIsReported() throws IOException {
        Path file = folder.resolve("ledger.yml");
        Files.writeString(file, "cursors:\n  a:\n    position: not-a-date\n");

        assertThrows(IOException.class, () -> new YamlLedgerRepository(file).load());
    }
}
