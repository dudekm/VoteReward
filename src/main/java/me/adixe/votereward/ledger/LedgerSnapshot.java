package me.adixe.votereward.ledger;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Plain copy of the {@link VoteLedger} state for saving and loading.
 *
 * @param cursors  read position per list name
 * @param rewarded per vote day and list name: nickname keys already rewarded that day
 * @param pending  rewards waiting for their player
 */
public record LedgerSnapshot(Map<String, SiteCursor> cursors, Map<LocalDate, Map<String, Set<String>>> rewarded,
                             List<PendingReward> pending) {
    public static final LedgerSnapshot EMPTY = new LedgerSnapshot(Map.of(), Map.of(), List.of());

    public LedgerSnapshot {
        cursors = Map.copyOf(cursors);
        rewarded = Map.copyOf(rewarded);
        pending = List.copyOf(pending);
    }
}
