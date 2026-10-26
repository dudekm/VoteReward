package me.adixe.votereward.reward;

import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.ledger.VoteLedger;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.site.VoteSites;
import me.adixe.votereward.vote.Nickname;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Hands earned rewards to players: right away when they are online, otherwise when they join.
 */
public final class RewardService {
    private final VoteLedger ledger;
    private final VoteSites sites;
    private final PlayerDirectory players;
    private final RewardDispatcher dispatcher;
    private final Logger logger;

    public RewardService(VoteLedger ledger, VoteSites sites, PlayerDirectory players, RewardDispatcher dispatcher,
                         Logger logger) {
        this.ledger = ledger;
        this.sites = sites;
        this.players = players;
        this.dispatcher = dispatcher;
        this.logger = logger;
    }

    /**
     * Gives the player every reward waiting for them.
     *
     * @return how many rewards were handed to the dispatcher
     */
    public int deliver(Nickname player) {
        List<Reward> rewards = new ArrayList<>();

        for (PendingReward pending : ledger.takePending(player)) {
            Optional<VoteSite> site = sites.find(pending.site());

            if (site.isPresent()) {
                rewards.add(new Reward(pending, site.get()));
            } else {
                logger.warning("Dropped " + player + "'s reward for a vote on \"" + pending.site()
                        + "\": that list is no longer in settings.yml.");
            }
        }

        if (!rewards.isEmpty()) {
            dispatcher.dispatch(player, rewards, ledger::returnPending);
        }

        return rewards.size();
    }

    /** A check found new rewards: give them to the players who are online now. */
    public void rewardsEarned(List<PendingReward> earned) {
        Set<Nickname> voters = new LinkedHashSet<>();

        for (PendingReward reward : earned) {
            voters.add(reward.nickname());
        }

        for (Nickname voter : voters) {
            if (players.isOnline(voter)) {
                deliver(voter);
            }
        }
    }
}
