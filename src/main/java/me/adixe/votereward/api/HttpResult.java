package me.adixe.votereward.api;

/**
 * Status code and body of an HTTP response.
 */
public record HttpResult(int status, String body) {
    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }
}
