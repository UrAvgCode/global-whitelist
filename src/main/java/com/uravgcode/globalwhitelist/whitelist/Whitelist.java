package com.uravgcode.globalwhitelist.whitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Whitelist {
    private final Logger logger;
    private final Path whitelistPath;
    private final Set<PlayerProfile> whitelist;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Whitelist(Path whitelistPath, Logger logger) {
        this.logger = logger;
        this.whitelistPath = whitelistPath;
        this.whitelist = ConcurrentHashMap.newKeySet();
    }

    public boolean contains(PlayerProfile profile) {
        return whitelist.contains(profile);
    }

    public List<String> list() {
        return whitelist.stream().map(PlayerProfile::name).toList();
    }

    public boolean add(PlayerProfile profile) {
        if (whitelist.add(profile)) {
            save();
            return true;
        }
        return false;
    }

    public boolean remove(PlayerProfile profile) {
        if (whitelist.remove(profile)) {
            save();
            return true;
        }
        return false;
    }

    public synchronized void reload() {
        try {
            if (Files.notExists(whitelistPath)) {
                Files.createDirectories(whitelistPath.getParent());
                Files.createFile(whitelistPath);
                whitelist.clear();
                return;
            }

            try (final var reader = Files.newBufferedReader(whitelistPath, StandardCharsets.UTF_8)) {
                final var listType = TypeToken.getParameterized(List.class, PlayerProfile.class).getType();
                final var profiles = gson.<List<PlayerProfile>>fromJson(reader, listType);

                whitelist.clear();
                if (profiles != null) {
                    whitelist.addAll(profiles);
                }
            }
        } catch (IOException exception) {
            logger.error("failed to reload whitelist", exception);
        }
    }

    private synchronized void save() {
        try {
            Files.createDirectories(whitelistPath.getParent());
            try (final var writer = Files.newBufferedWriter(whitelistPath, StandardCharsets.UTF_8)) {
                gson.toJson(List.copyOf(whitelist), writer);
            }
        } catch (IOException exception) {
            logger.error("failed to save whitelist", exception);
        }
    }
}
