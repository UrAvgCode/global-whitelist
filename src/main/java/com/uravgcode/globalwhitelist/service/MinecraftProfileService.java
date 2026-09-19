package com.uravgcode.globalwhitelist.service;

import com.google.gson.JsonParser;
import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MinecraftProfileService implements ProfileService {
    private static final URI API_BASE_URL = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/");
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final Logger logger;
    private final HttpClient httpClient;

    public MinecraftProfileService(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    }

    public boolean isProfile(String playerName) {
        return playerName != null && playerName.matches("[A-Za-z0-9_]{1,16}");
    }

    public CompletableFuture<Optional<PlayerProfile>> getProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("player name cannot be null or empty"));
        }

        if (!isProfile(playerName)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        try {
            var request = HttpRequest.newBuilder()
                .uri(API_BASE_URL.resolve(playerName))
                .timeout(TIMEOUT)
                .GET()
                .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> switch (response.statusCode()) {
                    case 200 -> parseProfile(response.body());
                    case 404 -> Optional.empty();
                    default -> throw new RuntimeException("profile lookup failed");
                });

        } catch (IllegalArgumentException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private Optional<PlayerProfile> parseProfile(String jsonString) {
        try {
            var jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            if (!jsonObject.has("id") || !jsonObject.has("name")) {
                logger.warn("json is missing required fields:\n{}", jsonString);
                return Optional.empty();
            }

            String uuidString = jsonObject.get("id").getAsString();
            String name = jsonObject.get("name").getAsString();

            return parseUUID(uuidString).map(uuid -> new PlayerProfile(uuid, name));

        } catch (Exception e) {
            logger.error("failed to parse json:\n{}\nerror: {}", jsonString, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<UUID> parseUUID(String uuidString) {
        try {
            long mostSigBits = Long.parseUnsignedLong(uuidString.substring(0, 16), 16);
            long leastSigBits = Long.parseUnsignedLong(uuidString.substring(16), 16);
            return Optional.of(new UUID(mostSigBits, leastSigBits));
        } catch (NumberFormatException e) {
            logger.warn("invalid uuid format: {}", uuidString);
            return Optional.empty();
        }
    }
}
