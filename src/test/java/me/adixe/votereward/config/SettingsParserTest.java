package me.adixe.votereward.config;

import me.adixe.votereward.site.VoteSite;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsParserTest {
    private static final String UUID = "183413bc-4588-4265-8f06-346963e5d7df";

    private final SettingsParser parser = new SettingsParser();

    @Test
    void readsAFullConfiguration() {
        SettingsParser.Result result = parser.parse(config(Map.of("interval", "1m"),
                site("lista-minecraft", "https://lista-minecraft.pl", UUID, List.of("give %player_name% diamond"))));

        assertEquals(Duration.ofMinutes(1), result.settings().pollInterval());
        assertEquals(List.of(), result.warnings());

        VoteSite site = result.settings().sites().find("lista-minecraft").orElseThrow();
        assertEquals("https://lista-minecraft.pl", site.address().toString());
        assertEquals(UUID, site.serverId().toString());
        assertEquals("give %player_name% diamond", site.rewardCommands().get(0).template());
    }

    @Test
    void pollIntervalDefaultsTo30SecondsWhenMissing() {
        // Configuration files from VoteReward 3.x have no "polling" section.
        SettingsParser.Result result = parser.parse(config(null,
                site("a", "https://a.pl", UUID, List.of("say hi"))));

        assertEquals(Duration.ofSeconds(30), result.settings().pollInterval());
        assertEquals(List.of(), result.warnings());
    }

    @Test
    void pollIntervalBelowMinimumIsRaised() {
        SettingsParser.Result result = parser.parse(config(Map.of("interval", "1s"),
                site("a", "https://a.pl", UUID, List.of("say hi"))));

        assertEquals(Settings.MIN_POLL_INTERVAL, result.settings().pollInterval());
        assertEquals(1, result.warnings().size());
    }

    @Test
    void invalidPollIntervalFallsBackToDefault() {
        SettingsParser.Result result = parser.parse(config(Map.of("interval", "often"),
                site("a", "https://a.pl", UUID, List.of("say hi"))));

        assertEquals(Settings.DEFAULT_POLL_INTERVAL, result.settings().pollInterval());
        assertTrue(result.warnings().get(0).startsWith("polling.interval"));
    }

    @Test
    void invalidSiteIsSkippedAndTheOthersStay() {
        Map<String, Object> servers = new LinkedHashMap<>();
        servers.putAll(site("broken", "https://a.pl", "not-a-uuid", List.of("say hi")));
        servers.putAll(site("good", "https://b.pl", UUID, List.of("say hi")));

        SettingsParser.Result result = parser.parse(config(null, servers));

        assertEquals(1, result.settings().sites().all().size());
        assertTrue(result.settings().sites().find("good").isPresent());
        assertTrue(result.warnings().get(0).startsWith("servers.broken skipped"));
    }

    @Test
    void warnsAboutMissingRewardCommands() {
        SettingsParser.Result result = parser.parse(config(null, site("a", "https://a.pl", UUID, List.of())));

        assertEquals(1, result.settings().sites().all().size());
        assertTrue(result.warnings().get(0).contains("no reward-commands"));
    }

    @Test
    void warnsWhenNothingIsConfigured() {
        SettingsParser.Result result = parser.parse(new MapConfigView(Map.of()));

        assertTrue(result.settings().sites().isEmpty());
        assertEquals(1, result.warnings().size());
    }

    private static Map<String, Object> site(String name, String address, String uuid, List<String> commands) {
        Map<String, Object> site = new LinkedHashMap<>();
        site.put("address", address);
        site.put("uuid", uuid);
        site.put("reward-commands", commands);

        return Map.of(name, site);
    }

    private static MapConfigView config(Map<String, Object> polling, Map<String, Object> servers) {
        Map<String, Object> root = new LinkedHashMap<>();

        if (polling != null) {
            root.put("polling", polling);
        }

        root.put("servers", servers);

        return new MapConfigView(root);
    }
}
