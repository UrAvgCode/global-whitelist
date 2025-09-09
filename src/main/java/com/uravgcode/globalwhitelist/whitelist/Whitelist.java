package com.uravgcode.globalwhitelist.whitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Whitelist {
    private final Logger logger;
    private final File whitelistFile;
    private final Set<PlayerProfile> whitelistPlayers;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Whitelist(Path whitelistPath, Logger logger) {
        this.logger = logger;
        this.whitelistFile = whitelistPath.toFile();
        this.whitelistPlayers = new HashSet<>();
    }

    public boolean contains(PlayerProfile player) {
        return whitelistPlayers.contains(player);
    }

    public boolean add(PlayerProfile player) {
        if (whitelistPlayers.add(player)) {
            save();
            return true;
        }
        return false;
    }

    public boolean remove(PlayerProfile player) {
        if (whitelistPlayers.remove(player)) {
            save();
            return true;
        }
        return false;
    }

    public List<String> list() {
        return whitelistPlayers.stream().map(PlayerProfile::name).toList();
    }

    public void reload() {
        try {
            if (whitelistFile.createNewFile()) {
                whitelistPlayers.clear();
                return;
            }

            try (FileReader reader = new FileReader(whitelistFile)) {
                Type listType = new TypeToken<List<PlayerProfile>>() {
                }.getType();
                List<PlayerProfile> players = gson.fromJson(reader, listType);

                if (players != null) {
                    whitelistPlayers.clear();
                    whitelistPlayers.addAll(players);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save() {
        try (FileWriter writer = new FileWriter(whitelistFile)) {
            gson.toJson(List.copyOf(whitelistPlayers), writer);
        } catch (IOException e) {
            logger.error("failed to save whitelist: {}", e.getMessage());
        }
    }
}
