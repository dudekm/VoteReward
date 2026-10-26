package me.adixe.votereward.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import me.adixe.votereward.vote.Vote;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads a page of {@code GET /api/v1/servers/{uuid}/votes}.
 *
 * <p>Accepts the JSON-LD (Hydra) collection — {@code member} and {@code view.next}, or their
 * {@code hydra:}-prefixed names used by older API Platform versions — as well as a plain
 * JSON array. An entry without an ID or a readable date is skipped rather than failing the page.
 */
public final class VotePageParser {
    public VotePage parse(String body) throws VoteSourceException {
        JsonElement root;

        try {
            root = JsonParser.parseString(body);
        } catch (JsonParseException exception) {
            throw new VoteSourceException("The vote list returned invalid JSON.", exception);
        }

        if (root.isJsonArray()) {
            return new VotePage(votes(root.getAsJsonArray()), Optional.empty());
        }

        if (!root.isJsonObject()) {
            throw new VoteSourceException("The vote list returned an unexpected response.");
        }

        JsonObject object = root.getAsJsonObject();
        JsonElement members = first(object, "member", "hydra:member");

        if (members == null || !members.isJsonArray()) {
            throw new VoteSourceException("The vote list response has no \"member\" list.");
        }

        return new VotePage(votes(members.getAsJsonArray()), next(object));
    }

    private static List<Vote> votes(JsonArray entries) {
        List<Vote> votes = new ArrayList<>(entries.size());

        for (JsonElement entry : entries) {
            if (!entry.isJsonObject()) {
                continue;
            }

            JsonObject object = entry.getAsJsonObject();
            String id = string(object, "uuid");
            Instant castAt = instant(string(object, "createdAt"));

            if (id == null || id.isBlank() || castAt == null) {
                continue;
            }

            votes.add(Vote.of(id, string(object, "nickname"), castAt));
        }

        return votes;
    }

    private static Optional<String> next(JsonObject collection) {
        JsonElement view = first(collection, "view", "hydra:view");

        if (view == null || !view.isJsonObject()) {
            return Optional.empty();
        }

        return Optional.ofNullable(first(view.getAsJsonObject(), "next", "hydra:next"))
                .filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsString)
                .filter(next -> !next.isBlank());
    }

    private static Instant instant(String value) {
        if (value == null) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // The lists store UTC; a date without an offset is read as UTC.
        }

        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String string(JsonObject object, String key) {
        JsonElement element = object.get(key);

        return element == null || !element.isJsonPrimitive() ? null : element.getAsString();
    }

    private static JsonElement first(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return object.get(key);
            }
        }

        return null;
    }
}
