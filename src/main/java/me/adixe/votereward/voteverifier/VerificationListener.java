package me.adixe.votereward.voteverifier;

public interface VerificationListener {
    void success();

    void notFound();

    void exceptionCaught(Exception exception);
}
