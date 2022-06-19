package me.hsgamer.authmeloginsign.signgui;

@FunctionalInterface
public interface SignCompleteHandler {
    void onSignClose(SignCompletedEvent event);
}
