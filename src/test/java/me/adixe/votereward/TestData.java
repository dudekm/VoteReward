package me.adixe.votereward;

import me.adixe.votereward.site.ListAddress;
import me.adixe.votereward.site.RewardCommand;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.vote.ServerId;

import java.util.List;

/**
 * Shared fixtures for tests.
 */
public final class TestData {
    public static final String SERVER_ID = "183413bc-4588-4265-8f06-346963e5d7df";

    private TestData() {
    }

    public static VoteSite site(String name) {
        return site(name, "https://lista-minecraft.pl");
    }

    public static VoteSite site(String name, String address) {
        return new VoteSite(name, ListAddress.of(address), ServerId.of(SERVER_ID),
                List.of(new RewardCommand("give %player_name% diamond")));
    }

    /** One vote in the shape the API returns it. */
    public static String voteJson(String id, String nickname, String createdAt) {
        String nick = nickname == null ? "null" : "\"" + nickname + "\"";

        return "{\"@id\":\"/api/v1/votes/" + id + "\",\"@type\":\"Vote\",\"uuid\":\"" + id
                + "\",\"nickname\":" + nick + ",\"createdAt\":\"" + createdAt + "\"}";
    }

    /** A JSON-LD collection page as API Platform 4 serves it. */
    public static String page(String next, String... votes) {
        String view = next == null
                ? "{\"@id\":\"/api/v1/servers/x/votes\",\"@type\":\"PartialCollectionView\"}"
                : "{\"@id\":\"/api/v1/servers/x/votes\",\"@type\":\"PartialCollectionView\",\"next\":\"" + next + "\"}";

        return "{\"@context\":\"/api/v1/contexts/Vote\",\"@id\":\"/api/v1/servers/x/votes\",\"@type\":\"Collection\","
                + "\"totalItems\":" + votes.length + ",\"member\":[" + String.join(",", votes) + "],\"view\":" + view + "}";
    }
}
