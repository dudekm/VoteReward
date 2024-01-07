package me.adixe.votereward.votemanager;

import java.io.IOException;

public interface VerificationListener {
    void success();

    void notFound();

    void exceptionCaught(IOException exception);
}
