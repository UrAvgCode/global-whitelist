package com.uravgcode.globalwhitelist.service;

import com.google.gson.JsonObject;
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
import java.util.regex.Pattern;

public class MinecraftProfileService {
    private static final URI API_BASE_URL = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/");
    private static final Pattern UUID_PATTERN = Pattern.compile("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})");
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final Logger logger;
    private final HttpClient httpClient;

    public MinecraftProfileService(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    }

    public Optional<PlayerProfile> getProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            logger.warn("player name cannot be null or empty");
            return Optional.empty();
        }

        try {
            var request = HttpRequest.newBuilder()
                .uri(API_BASE_URL.resolve(playerName))
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
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            if (!jsonObject.has("id") || !jsonObject.has("name")) {
                logger.warn("missing 'id' or 'name' in json:\n{}", jsonString);
                return Optional.empty();
            }

            String uuidString = jsonObject.get("id").getAsString();
            String name = jsonObject.get("name").getAsString();

            var matcher = UUID_PATTERN.matcher(uuidString);
            if (!matcher.matches()) {
                logger.warn("invalid uuid format in json:\n{}", jsonString);
                return Optional.empty();
            }

            var uuid = UUID.fromString(matcher.replaceFirst("$1-$2-$3-$4-$5"));
            return Optional.of(new PlayerProfile(uuid, name));

        } catch (Exception e) {
            logger.error("failed to parse json:\n{}\nerror: {}", jsonString, e.getMessage());
            return Optional.empty();
        }
    }
}
