package me.adixe.votereward.reward;

import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.vote.Nickname;

import java.util.List;
import java.util.function.Consumer;

/**
 * Gives rewards on the server: runs the reward commands and tells the player.
 * Implementations decide on which thread that happens.
 */
public interface RewardDispatcher {
    /**
     * @param undelivered called for every reward that could not be given (the player left
     *                    in the meantime), so it can wait for the next join
     */
    void dispatch(Nickname player, List<Reward> rewards, Consumer<PendingReward> undelivered);
}
