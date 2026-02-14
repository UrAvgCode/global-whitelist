package com.uravgcode.globalwhitelist.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class MessagesConfig {
    public static final String WHITELIST_HELP = "whitelist.help";

    public static final String WHITELIST_ADD_SUCCESS = "whitelist.add.success";
    public static final String WHITELIST_ADD_ALREADY_WHITELISTED = "whitelist.add.already_whitelisted";

    public static final String WHITELIST_REMOVE_SUCCESS = "whitelist.remove.success";
    public static final String WHITELIST_REMOVE_NOT_WHITELISTED = "whitelist.remove.not_whitelisted";

    public static final String WHITELIST_PLAYER_DOES_NOT_EXIST = "whitelist.player_does_not_exist";

    public static final String WHITELIST_LIST = "whitelist.list";
    public static final String WHITELIST_LIST_EMPTY = "whitelist.list.empty";

    public static final String WHITELIST_ON = "whitelist.on";
    public static final String WHITELIST_ON_ALREADY = "whitelist.on.already";
    public static final String WHITELIST_OFF = "whitelist.off";
    public static final String WHITELIST_OFF_ALREADY = "whitelist.off.already";

    public static final String WHITELIST_ENFORCED = "whitelist.enforced";
    public static final String WHITELIST_ENFORCED_ALREADY = "whitelist.enforced.already";
    public static final String WHITELIST_UNENFORCED = "whitelist.unenforced";
    public static final String WHITELIST_UNENFORCED_ALREADY = "whitelist.unenforced.already";

    public static final String WHITELIST_RELOAD = "whitelist.reload";

    public static final String WHITELIST_REJECTED = "whitelist.rejected";

    private final Logger logger;
    private final Properties messages;
    private final File messagesFile;
    private final MiniMessage miniMessage;

    public MessagesConfig(
        final Path messagesPath,
        final Logger logger
    ) {
        this.messagesFile = messagesPath.toFile();
        this.logger = logger;
        this.messages = new Properties();
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void reload() {
        if (!messagesFile.exists()) {
            loadDefaults();
            save();
            return;
        }

        try (FileInputStream in = new FileInputStream(messagesFile)) {
            messages.load(in);
        } catch (IOException e) {
            logger.error("failed to load messages config: {}", e.getMessage());
        }
    }

    public void save() {
        try (FileOutputStream out = new FileOutputStream(messagesFile)) {
            messages.store(out, "messages config");
        } catch (IOException e) {
            logger.error("failed to save messages config: {}", e.getMessage());
        }
    }

    private void loadDefaults() {
        messages.setProperty(WHITELIST_HELP, "<red>/globalwhitelist <add|remove|list|on|off|enforce|unenforce|reload|floodgate_add|floodgate_remove>");

        messages.setProperty(WHITELIST_ADD_SUCCESS, "Added <player> to the whitelist");
        messages.setProperty(WHITELIST_ADD_ALREADY_WHITELISTED, "<red>Player is already whitelisted");

        messages.setProperty(WHITELIST_REMOVE_SUCCESS, "Removed <player> from the whitelist");
        messages.setProperty(WHITELIST_REMOVE_NOT_WHITELISTED, "<red>Player is not whitelisted");

        messages.setProperty(WHITELIST_PLAYER_DOES_NOT_EXIST, "<red>That player does not exist");

        messages.setProperty(WHITELIST_LIST, "There are <count> whitelisted player(s): <players>");
        messages.setProperty(WHITELIST_LIST_EMPTY, "There are no whitelisted players");

        messages.setProperty(WHITELIST_ON, "Whitelist is now turned on");
        messages.setProperty(WHITELIST_ON_ALREADY, "<red>Whitelist is already turned on");
        messages.setProperty(WHITELIST_OFF, "Whitelist is now turned off");
        messages.setProperty(WHITELIST_OFF_ALREADY, "<red>Whitelist is already turned off");

        messages.setProperty(WHITELIST_ENFORCED, "Enforce whitelist is now turned on");
        messages.setProperty(WHITELIST_ENFORCED_ALREADY, "<red>Enforce whitelist is already turned on");
        messages.setProperty(WHITELIST_UNENFORCED, "Enforce whitelist is now turned off");
        messages.setProperty(WHITELIST_UNENFORCED_ALREADY, "<red>Enforce whitelist is already turned off");

        messages.setProperty(WHITELIST_RELOAD, "Reloaded the whitelist");

        messages.setProperty(WHITELIST_REJECTED, "<red>You are not whitelisted on this server!");
    }

    public String get(String key) {
        return messages.getProperty(key, "");
    }

    public Component getMessage(String key) {
        return miniMessage.deserialize(get(key));
    }

    public Component getMessage(String key, String player) {
        return miniMessage.deserialize(get(key), Placeholder.component("player", Component.text(player)));
    }

    public Component getMessage(String key, List<String> players) {
        return miniMessage.deserialize(
            get(key),
            Placeholder.component("count", Component.text(players.size())),
            Placeholder.component("players", Component.text(String.join(", ", players)))
        );
    }
}
