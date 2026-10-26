package me.adixe.votereward.api;

import me.adixe.votereward.vote.Vote;

import java.util.List;
import java.util.Optional;

/**
 * One page of the votes collection.
 *
 * @param next link to the following page as given by the API, usually a path with a query
 */
public record VotePage(List<Vote> votes, Optional<String> next) {
    public VotePage {
        votes = List.copyOf(votes);
    }
}
