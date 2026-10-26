package me.adixe.votereward.api;

/**
 * The vote list could not be queried or answered with something unreadable.
 */
public class VoteSourceException extends Exception {
    public VoteSourceException(String message) {
        super(message);
    }

    public VoteSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
