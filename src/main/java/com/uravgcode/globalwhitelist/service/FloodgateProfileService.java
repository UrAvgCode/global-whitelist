package com.uravgcode.globalwhitelist.service;

import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.concurrent.CompletableFuture;

public class FloodgateProfileService {
    private final FloodgateApi api;

    public FloodgateProfileService() {
        this.api = FloodgateApi.getInstance();
    }

    public boolean isProfile(String playerName) {
        final var prefix = api.getPlayerPrefix();
        return playerName.startsWith(prefix);
    }

    public CompletableFuture<PlayerProfile> getProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("player name cannot be null or empty"));
        }

        final var prefix = api.getPlayerPrefix();
        if (!playerName.startsWith(prefix)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("player name doesn't start with prefix"));
        }

        var gamertag = playerName.substring(prefix.length());
        return api.getUuidFor(gamertag).thenApply(uuid -> {
            if (uuid == null) {
                return null;
            }
            return new PlayerProfile(uuid, playerName);
        });
    }
}
