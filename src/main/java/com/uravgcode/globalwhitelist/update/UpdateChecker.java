package com.uravgcode.globalwhitelist.update;

import com.google.gson.JsonParser;
import com.uravgcode.globalwhitelist.BuildConstants;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class UpdateChecker {
    private final HttpClient httpClient;
    private final HttpRequest httpRequest;

    public UpdateChecker() {
        final var uri = URI.create("https://api.github.com/repos/UrAvgCode/global-whitelist/releases/latest");
        final var timeout = Duration.ofSeconds(5);

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();

        this.httpRequest = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build();
    }

    public CompletableFuture<String> fetchLatestVersion() {
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            final var json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.get("tag_name").getAsString();
        });
    }

    public void sendVersionInfo(Audience audience) {
        audience.sendMessage(Component.text("Checking version, please wait...").decorate(TextDecoration.ITALIC));

        fetchLatestVersion().thenAccept(latestVersion -> {
                audience.sendMessage(Component.text("global-whitelist version: ")
                    .append(Component.text(BuildConstants.VERSION, NamedTextColor.GREEN)));

                final var comparison = compareVersions(latestVersion);
                if (comparison == 0) {
                    audience.sendMessage(Component.text("You are running the latest version", NamedTextColor.GREEN));
                } else if (comparison > 0) {
                    audience.sendMessage(Component.text("Latest version: ")
                        .append(Component.text(latestVersion, NamedTextColor.GREEN)));
                    audience.sendMessage(Component.text("Download: ")
                        .append(Component.text("Github", TextColor.color(0x59636e))
                            .clickEvent(ClickEvent.openUrl("https://github.com/UrAvgCode/global-whitelist/releases")))
                        .append(Component.text(" Modrinth", TextColor.color(0x1bd96a))
                            .clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/global-whitelist"))));
                } else {
                    audience.sendMessage(Component.text("Latest version: ")
                        .append(Component.text(latestVersion, NamedTextColor.GREEN)));
                    audience.sendMessage(Component.text("You are running a newer version than the latest release", NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                audience.sendMessage(Component.text("global-whitelist version: ")
                    .append(Component.text(BuildConstants.VERSION, NamedTextColor.GREEN)));
                audience.sendMessage(Component.text("Failed to fetch latest version", NamedTextColor.RED));
                return null;
            });
    }

    private static int compareVersions(String latestVersion) {
        final var latestParts = latestVersion.split("\\.");
        final var currentParts = BuildConstants.VERSION.split("\\.");
        final var length = Math.max(latestParts.length, currentParts.length);

        for (int i = 0; i < length; ++i) {
            final int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            final int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;

            if (latestPart != currentPart) {
                return Integer.compare(latestPart, currentPart);
            }
        }

        return 0;
    }
}
