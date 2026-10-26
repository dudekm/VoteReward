package me.adixe.votereward.api;

import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.vote.Vote;

import java.time.Instant;
import java.util.List;

/**
 * Where votes come from.
 */
public interface VoteSource {
    /**
     * Votes cast for the site's server at or after {@code since}, oldest first.
     * May return only the oldest part of a large backlog; the caller continues from the
     * last returned vote on its next call.
     */
    List<Vote> votesSince(VoteSite site, Instant since) throws VoteSourceException;
}
