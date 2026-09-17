package com.uravgcode.globalwhitelist.whitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Whitelist {
    private final Logger logger;
    private final File whitelistFile;
    private final Set<PlayerProfile> whitelist;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Whitelist(Path whitelistPath, Logger logger) {
        this.logger = logger;
        this.whitelistFile = whitelistPath.toFile();
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

    public void reload() {
        try {
            if (whitelistFile.createNewFile()) {
                whitelist.clear();
                return;
            }

            try (FileReader reader = new FileReader(whitelistFile)) {
                final var listType = new TypeToken<List<PlayerProfile>>() {
                }.getType();
                final List<PlayerProfile> profiles = gson.fromJson(reader, listType);

                if (profiles != null) {
                    whitelist.clear();
                    whitelist.addAll(profiles);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save() {
        try (FileWriter writer = new FileWriter(whitelistFile)) {
            gson.toJson(List.copyOf(whitelist), writer);
        } catch (IOException e) {
            logger.error("failed to save whitelist: {}", e.getMessage());
        }
    }
}
