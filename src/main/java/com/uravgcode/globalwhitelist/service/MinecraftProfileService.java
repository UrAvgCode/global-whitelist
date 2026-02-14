package com.uravgcode.globalwhitelist.service;

import com.google.gson.JsonParser;
import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public class MinecraftProfileService {
    private static final URI MC_API_BASE_URL = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/");
    private static final URI FLOODGATE_API_BASE_URL = URI.create("https://api.geysermc.org/v2/utils/uuid/bedrock_or_java/");
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final Logger logger;
    private final HttpClient httpClient;

    public MinecraftProfileService(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    }

    public Optional<PlayerProfile> getProfile(String playerName, boolean floodgate) {
        if (playerName == null || playerName.isBlank()) {
            logger.warn("player name cannot be null or empty");
            return Optional.empty();
        }

        URI api_url;
        if (floodgate) {
            api_url = URI.create(FLOODGATE_API_BASE_URL+playerName+"?prefix="+playerName.charAt(0));
        } else {
            api_url = URI.create(MC_API_BASE_URL+playerName);
        }
        try {
            var request = HttpRequest.newBuilder()
                .uri(api_url)
                .timeout(TIMEOUT)
                .GET()
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> parseProfile(response.body());
                case 404 -> {
                    logger.warn("player with name {} not found", playerName);
                    yield Optional.empty();
                }
                default -> {
                    logger.warn("api request failed with status {}: {}", response.statusCode(), response.body());
                    logger.warn(api_url.toString());
                    yield Optional.empty();
                }
            };

        } catch (IOException e) {
            logger.error("network error while fetching profile for '{}': {}", playerName, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("request interrupted while fetching profile for '{}': {}", playerName, e.getMessage());
            return Optional.empty();
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
