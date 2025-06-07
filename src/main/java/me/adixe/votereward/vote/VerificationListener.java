package me.adixe.votereward.vote;

public interface VerificationListener {
    void success();

    void notFound();

    void rewardGranted();

    void exceptionCaught(Exception exception);
}
