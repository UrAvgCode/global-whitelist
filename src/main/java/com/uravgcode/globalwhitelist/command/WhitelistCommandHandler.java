package com.uravgcode.globalwhitelist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.uravgcode.globalwhitelist.config.MessagesConfig;
import com.uravgcode.globalwhitelist.config.WhitelistConfig;
import com.uravgcode.globalwhitelist.service.MinecraftProfileService;
import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;
import com.uravgcode.globalwhitelist.whitelist.Whitelist;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.concurrent.CompletableFuture;

public record WhitelistCommandHandler(
    ProxyServer proxy,
    MinecraftProfileService profileService,
    Whitelist whitelist,
    WhitelistConfig config,
    MessagesConfig messages
) {
    public CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSource> ignored, SuggestionsBuilder builder) {
        proxy.getAllPlayers().forEach(player -> builder.suggest(player.getUsername()));
        return builder.buildFuture();
    }

    public CompletableFuture<Suggestions> suggestWhitelistedPlayers(CommandContext<CommandSource> ignored, SuggestionsBuilder builder) {
        whitelist.list().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public int help(CommandContext<CommandSource> context) {
        context.getSource().sendMessage(messages.getMessage(MessagesConfig.WHITELIST_HELP));
        return Command.SINGLE_SUCCESS;
    }

    public int add(CommandContext<CommandSource> context) {
        var source = context.getSource();
        var playerName = context.getArgument("player", String.class);

        profileService.getProfile(playerName, false).ifPresentOrElse(player -> {
            if (whitelist.add(player)) {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ADD_SUCCESS, playerName));
            } else {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ADD_ALREADY_WHITELISTED, playerName));
            }
        }, () -> source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_PLAYER_DOES_NOT_EXIST, playerName)));

        return Command.SINGLE_SUCCESS;
    }

    public int floodgate_add(CommandContext<CommandSource> context) {
        var source = context.getSource();
        var playerName = context.getArgument("player", String.class);

        profileService.getProfile(playerName, true).ifPresentOrElse(player -> {
            if (whitelist.add(player)) {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ADD_SUCCESS, playerName));
            } else {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ADD_ALREADY_WHITELISTED, playerName));
            }
        }, () -> source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_PLAYER_DOES_NOT_EXIST, playerName)));

        return Command.SINGLE_SUCCESS;
    }

    public int remove(CommandContext<CommandSource> context) {
        var source = context.getSource();
        var playerName = context.getArgument("player", String.class);

        profileService.getProfile(playerName, false).ifPresentOrElse(player -> {
            if (whitelist.remove(player)) {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_REMOVE_SUCCESS, playerName));
                if (config.whitelistEnabled() && config.enforceWhitelistEnabled()) {
                    proxy.getPlayer(playerName).ifPresent(p -> p.disconnect(messages.getMessage(MessagesConfig.WHITELIST_REJECTED)));
                }
            } else {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_REMOVE_NOT_WHITELISTED, playerName));
            }
        }, () -> source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_PLAYER_DOES_NOT_EXIST, playerName)));

        return Command.SINGLE_SUCCESS;
    }

    public int floodgate_remove(CommandContext<CommandSource> context) {
        var source = context.getSource();
        var playerName = context.getArgument("player", String.class);

        profileService.getProfile(playerName, true).ifPresentOrElse(player -> {
            if (whitelist.remove(player)) {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_REMOVE_SUCCESS, playerName));
                if (config.whitelistEnabled() && config.enforceWhitelistEnabled()) {
                    proxy.getPlayer(playerName).ifPresent(p -> p.disconnect(messages.getMessage(MessagesConfig.WHITELIST_REJECTED)));
                }
            } else {
                source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_REMOVE_NOT_WHITELISTED, playerName));
            }
        }, () -> source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_PLAYER_DOES_NOT_EXIST, playerName)));

        return Command.SINGLE_SUCCESS;
    }

    public int list(CommandContext<CommandSource> context) {
        var source = context.getSource();
        var playerList = whitelist.list();

        if (playerList.isEmpty()) {
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_LIST_EMPTY));
        } else {
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_LIST, playerList));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int on(CommandContext<CommandSource> context) {
        var source = context.getSource();

        if (config.whitelistEnabled()) {
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ON_ALREADY));
        } else {
            config.setWhitelistEnabled(true);
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ON));
            if (config.enforceWhitelistEnabled()) {
                for (var player : proxy.getAllPlayers()) {
                    if (!whitelist.contains(new PlayerProfile(player.getUniqueId(), player.getUsername()))) {
                        player.disconnect(messages.getMessage(MessagesConfig.WHITELIST_REJECTED));
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public int off(CommandContext<CommandSource> context) {
        var source = context.getSource();

        if (config.whitelistEnabled()) {
            config.setWhitelistEnabled(false);
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_OFF));
        } else {
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_OFF_ALREADY));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int enforced(CommandContext<CommandSource> context) {
        var source = context.getSource();

        if (config.enforceWhitelistEnabled()) {
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ENFORCED_ALREADY));
        } else {
            config.setEnforceWhitelistEnabled(true);
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_ENFORCED));
            if (config.whitelistEnabled()) {
                for (var player : proxy.getAllPlayers()) {
                    if (!whitelist.contains(new PlayerProfile(player.getUniqueId(), player.getUsername()))) {
                        player.disconnect(messages.getMessage(MessagesConfig.WHITELIST_REJECTED));
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public int unenforced(CommandContext<CommandSource> context) {
        var source = context.getSource();

        if (config.enforceWhitelistEnabled()) {
            config.setEnforceWhitelistEnabled(false);
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_UNENFORCED));
        } else {
            source.sendMessage(messages.getMessage(MessagesConfig.WHITELIST_UNENFORCED_ALREADY));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int reload(CommandContext<CommandSource> context) {
        whitelist.reload();
        config.reload();
        messages.reload();
        context.getSource().sendMessage(messages.getMessage(MessagesConfig.WHITELIST_RELOAD));
        return Command.SINGLE_SUCCESS;
    }
}
