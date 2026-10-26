package me.adixe.votereward.reward;

import me.adixe.votereward.vote.Nickname;

/**
 * Who is on the server right now.
 */
public interface PlayerDirectory {
    boolean isOnline(Nickname nickname);
}
