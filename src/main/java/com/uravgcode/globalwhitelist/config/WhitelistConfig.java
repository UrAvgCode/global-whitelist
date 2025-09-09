package com.uravgcode.globalwhitelist.config;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class WhitelistConfig {
    private static final String WHITE_LIST = "white-list";
    private static final String ENFORCE_WHITELIST = "enforce-whitelist";

    private final File configFile;
    private final Logger logger;
    private final Properties config;

    public WhitelistConfig(Path configPath, Logger logger) {
        this.configFile = configPath.toFile();
        this.logger = logger;
        this.config = new Properties();
    }

    public void reload() {
        if (!configFile.exists()) {
            config.setProperty(WHITE_LIST, "false");
            config.setProperty(ENFORCE_WHITELIST, "false");
            save();
            return;
        }

        try (FileInputStream in = new FileInputStream(configFile)) {
            config.load(in);
        } catch (IOException e) {
            logger.error("failed to load whitelist config: {}", e.getMessage());
        }
    }

    public void save() {
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            config.store(out, "whitelist config");
        } catch (IOException e) {
            logger.error("failed to save whitelist config: {}", e.getMessage());
        }
    }

    public boolean whitelistEnabled() {
        return Boolean.parseBoolean(config.getProperty(WHITE_LIST));
    }

    public void setWhitelistEnabled(boolean enabled) {
        config.setProperty(WHITE_LIST, Boolean.toString(enabled));
        save();
    }

    public boolean enforceWhitelistEnabled() {
        return Boolean.parseBoolean(config.getProperty(ENFORCE_WHITELIST));
    }

    public void setEnforceWhitelistEnabled(boolean enabled) {
        config.setProperty(ENFORCE_WHITELIST, Boolean.toString(enabled));
        save();
    }
}
