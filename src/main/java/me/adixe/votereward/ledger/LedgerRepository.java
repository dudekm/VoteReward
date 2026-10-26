package me.adixe.votereward.ledger;

import java.io.IOException;

/**
 * Persists the {@link VoteLedger} between restarts.
 */
public interface LedgerRepository {
    LedgerSnapshot load() throws IOException;

    void save(LedgerSnapshot snapshot) throws IOException;
}
