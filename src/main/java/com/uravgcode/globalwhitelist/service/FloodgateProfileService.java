package com.uravgcode.globalwhitelist.service;

import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class FloodgateProfileService implements ProfileService {
    private final FloodgateApi api;

    public FloodgateProfileService() {
        this.api = FloodgateApi.getInstance();
    }

    public boolean isProfile(String playerName) {
        final var prefix = api.getPlayerPrefix();
        return playerName.startsWith(prefix);
    }

    public CompletableFuture<Optional<PlayerProfile>> getProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("player name cannot be null or empty"));
        }

        final var prefix = api.getPlayerPrefix();
        if (!playerName.startsWith(prefix)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        var gamertag = playerName.substring(prefix.length());
        return api.getUuidFor(gamertag)
            .thenApply(uuid -> Optional.ofNullable(uuid)
                .map(id -> new PlayerProfile(id, playerName)))
            .exceptionallyCompose(throwable -> {
                final var cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;

                if (cause instanceof IllegalStateException) {
                    return CompletableFuture.completedFuture(Optional.empty());
                }

                return CompletableFuture.failedFuture(cause);
            });
    }
}
