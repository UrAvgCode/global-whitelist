package com.uravgcode.globalwhitelist;

import com.google.inject.Inject;
import com.uravgcode.globalwhitelist.command.WhitelistCommand;
import com.uravgcode.globalwhitelist.config.MessagesConfig;
import com.uravgcode.globalwhitelist.config.WhitelistConfig;
import com.uravgcode.globalwhitelist.service.FloodgateProfileService;
import com.uravgcode.globalwhitelist.service.MinecraftProfileService;
import com.uravgcode.globalwhitelist.service.ProfileService;
import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;
import com.uravgcode.globalwhitelist.whitelist.Whitelist;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Plugin(
    id = "global-whitelist",
    name = "global-whitelist",
    version = BuildConstants.VERSION,
    url = "https://github.com/uravgcode/global-whitelist",
    description = "velocity whitelist plugin",
    authors = {"UrAvgCode"},
    dependencies = {
        @Dependency(id = "floodgate", optional = true)
    }
)
public class GlobalWhitelistPlugin {
    private final ProxyServer proxy;
    private final Map<String, ProfileService> profileServices;

    private final Whitelist whitelist;
    private final WhitelistConfig config;
    private final MessagesConfig messages;

    @Inject
    public GlobalWhitelistPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = server;

        profileServices = new LinkedHashMap<>();
        profileServices.put("java", new MinecraftProfileService(logger));

        this.whitelist = new Whitelist(dataDirectory.resolve("whitelist.json"), logger);
        this.config = new WhitelistConfig(dataDirectory.resolve("config.properties"), logger);
        this.messages = new MessagesConfig(dataDirectory.resolve("messages.properties"), logger);

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            logger.error("failed to create plugin directory: {}", e.getMessage());
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (proxy.getPluginManager().getPlugin("floodgate").isPresent()) {
            profileServices.put("bedrock", new FloodgateProfileService());
        }

        config.reload();
        whitelist.reload();
        messages.reload();

        proxy.getEventManager().register(this, LoginEvent.class, loginEvent -> {
            if (!config.whitelistEnabled()) {
                return;
            }

            final var player = loginEvent.getPlayer();
            if (!whitelist.contains(new PlayerProfile(player.getUniqueId(), player.getUsername()))) {
                loginEvent.setResult(ResultedEvent.ComponentResult.denied(messages.getMessage(MessagesConfig.WHITELIST_REJECTED)));
            }
        });

        final var commandManager = proxy.getCommandManager();
        final var commandMeta = commandManager.metaBuilder("gwl").plugin(this).build();
        final var command = WhitelistCommand.createCommand(proxy, profileServices, whitelist, config, messages);
        commandManager.register(commandMeta, command);
    }
}
