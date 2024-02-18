package me.adixe.votereward.votemanager;

public interface VerificationListener {
    void success();

    void notFound();

    void exceptionCaught(Exception exception);
}
