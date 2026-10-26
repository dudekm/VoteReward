package me.adixe.votereward.ledger;

import me.adixe.votereward.vote.Nickname;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Keeps the ledger in a YAML file ({@code plugins/VoteReward/data/ledger.yml}).
 * Uses SnakeYAML, which every Bukkit-based server ships.
 *
 * <pre>
 * cursors:
 *   lista-minecraft:
 *     position: '2026-10-25T09:00:00Z'
 *     recent-votes: {3f2c…: '2026-10-25T09:04:12Z'}
 * rewarded:
 *   '2026-10-25': {lista-minecraft: [steve]}
 * pending:
 *   - {vote: 3f2c…, site: lista-minecraft, nickname: Steve, cast-at: '2026-10-25T09:04:12Z'}
 * </pre>
 */
public final class YamlLedgerRepository implements LedgerRepository {
    private final Path file;

    public YamlLedgerRepository(Path file) {
        this.file = file;
    }

    @Override
    public synchronized LedgerSnapshot load() throws IOException {
        if (!Files.exists(file)) {
            return LedgerSnapshot.EMPTY;
        }

        Object root = new Yaml().load(Files.readString(file, StandardCharsets.UTF_8));

        if (!(root instanceof Map<?, ?> data)) {
            return LedgerSnapshot.EMPTY;
        }

        try {
            return new LedgerSnapshot(cursors(data.get("cursors")), rewarded(data.get("rewarded")),
                    pending(data.get("pending")));
        } catch (RuntimeException exception) {
            throw new IOException("Vote data in " + file + " is damaged: " + exception.getMessage(), exception);
        }
    }

    @Override
    public synchronized void save(LedgerSnapshot snapshot) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cursors", encodeCursors(snapshot.cursors()));
        data.put("rewarded", encodeRewarded(snapshot.rewarded()));
        data.put("pending", encodePending(snapshot.pending()));

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, new Yaml(options).dump(data), StandardCharsets.UTF_8);

        // Write-then-move, so a crash mid-write never leaves a half-written file behind.
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, Object> encodeCursors(Map<String, SiteCursor> cursors) {
        Map<String, Object> encoded = new TreeMap<>();

        cursors.forEach((site, cursor) -> {
            Map<String, Object> recent = new TreeMap<>();
            cursor.recentVotes().forEach((id, castAt) -> recent.put(id, castAt.toString()));

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("position", cursor.position().toString());
            entry.put("recent-votes", recent);
            encoded.put(site, entry);
        });

        return encoded;
    }

    private static Map<String, Object> encodeRewarded(Map<LocalDate, Map<String, Set<String>>> rewarded) {
        Map<String, Object> encoded = new TreeMap<>();

        rewarded.forEach((day, sites) -> {
            Map<String, Object> perSite = new TreeMap<>();
            sites.forEach((site, nicknames) -> perSite.put(site, new ArrayList<>(new TreeSet<>(nicknames))));
            encoded.put(day.toString(), perSite);
        });

        return encoded;
    }

    private static List<Object> encodePending(List<PendingReward> pending) {
        List<Object> encoded = new ArrayList<>();

        for (PendingReward reward : pending) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("vote", reward.voteId());
            entry.put("site", reward.site());
            entry.put("nickname", reward.nickname().value());
            entry.put("cast-at", reward.castAt().toString());
            encoded.add(entry);
        }

        return encoded;
    }

    private static Map<String, SiteCursor> cursors(Object raw) {
        Map<String, SiteCursor> cursors = new HashMap<>();

        map(raw).forEach((site, value) -> {
            Map<String, Object> entry = map(value);
            Map<String, Instant> recent = new HashMap<>();
            map(entry.get("recent-votes")).forEach((id, castAt) -> recent.put(id, instant(castAt)));
            cursors.put(site, new SiteCursor(instant(entry.get("position")), recent));
        });

        return cursors;
    }

    private static Map<LocalDate, Map<String, Set<String>>> rewarded(Object raw) {
        Map<LocalDate, Map<String, Set<String>>> rewarded = new HashMap<>();

        map(raw).forEach((day, value) -> {
            Map<String, Set<String>> perSite = new HashMap<>();
            map(value).forEach((site, nicknames) -> perSite.put(site, strings(nicknames)));
            rewarded.put(LocalDate.parse(day), perSite);
        });

        return rewarded;
    }

    private static List<PendingReward> pending(Object raw) {
        List<PendingReward> pending = new ArrayList<>();

        if (raw instanceof Collection<?> entries) {
            for (Object value : entries) {
                Map<String, Object> entry = map(value);
                pending.add(new PendingReward(String.valueOf(entry.get("vote")), String.valueOf(entry.get("site")),
                        Nickname.of(String.valueOf(entry.get("nickname"))), instant(entry.get("cast-at"))));
            }
        }

        return pending;
    }

    private static Map<String, Object> map(Object raw) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (raw instanceof Map<?, ?> map) {
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
        }

        return result;
    }

    private static Set<String> strings(Object raw) {
        Set<String> result = new HashSet<>();

        if (raw instanceof Collection<?> values) {
            values.forEach(value -> result.add(String.valueOf(value)));
        }

        return result;
    }

    private static Instant instant(Object raw) {
        // A hand-edited file may hold an unquoted timestamp, which SnakeYAML reads as a Date.
        return raw instanceof Date date ? date.toInstant() : Instant.parse(String.valueOf(raw));
    }
}
