package com.uravgcode.globalwhitelist.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.uravgcode.globalwhitelist.config.MessagesConfig;
import com.uravgcode.globalwhitelist.config.WhitelistConfig;
import com.uravgcode.globalwhitelist.service.MinecraftProfileService;
import com.uravgcode.globalwhitelist.whitelist.Whitelist;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public final class WhitelistCommand {
    private static final String PERMISSION_BASE = "globalwhitelist";
    private static final String PERMISSION_ADMIN = "globalwhitelist.admin";

    public static BrigadierCommand createCommand(
        ProxyServer proxy,
        MinecraftProfileService profileService,
        Whitelist whitelist,
        WhitelistConfig config,
        MessagesConfig messages
    ) {
        var commandHandler = new WhitelistCommandHandler(proxy, profileService, whitelist, config, messages);

        var rootNode = BrigadierCommand.literalArgumentBuilder("globalwhitelist")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .executes(commandHandler::help)
            .then(buildAddCommand(commandHandler))
            .then(buildRemoveCommand(commandHandler))
            .then(buildListCommand(commandHandler))
            .then(buildReloadCommand(commandHandler))
            .then(buildOnCommand(commandHandler))
            .then(buildOffCommand(commandHandler))
            .then(buildEnforcedCommand(commandHandler))
            .then(buildUnenforcedCommand(commandHandler))
            .then(buildFloodgateRemoveCommand(commandHandler))
            .then(buildFloodgateAddCommand(commandHandler))
            .then(buildHelpCommand(commandHandler))
            .build();

        return new BrigadierCommand(rootNode);
    }

    private static LiteralArgumentBuilder<CommandSource> buildAddCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("add")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests(handler::suggestOnlinePlayers)
                .executes(handler::add));
    }

    private static LiteralArgumentBuilder<CommandSource> buildFloodgateAddCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("floodgate_add")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests(handler::suggestOnlinePlayers)
                .executes(handler::floodgate_add));
    }

    private static LiteralArgumentBuilder<CommandSource> buildRemoveCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("remove")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests(handler::suggestWhitelistedPlayers)
                .executes(handler::remove));
    }

    private static LiteralArgumentBuilder<CommandSource> buildFloodgateRemoveCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("floodgate_remove")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests(handler::suggestWhitelistedPlayers)
                .executes(handler::floodgate_remove));
    }

    private static LiteralArgumentBuilder<CommandSource> buildListCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("list")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::list);
    }

    private static LiteralArgumentBuilder<CommandSource> buildOnCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("on")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::on);
    }

    private static LiteralArgumentBuilder<CommandSource> buildOffCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("off")
            .requires(source -> source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::off);
    }

    private static LiteralArgumentBuilder<CommandSource> buildEnforcedCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("enforced")
            .requires(source -> source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::enforced);
    }

    private static LiteralArgumentBuilder<CommandSource> buildUnenforcedCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("unenforced")
            .requires(source -> source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::unenforced);
    }

    private static LiteralArgumentBuilder<CommandSource> buildReloadCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("reload")
            .requires(source -> source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::reload);
    }

    private static LiteralArgumentBuilder<CommandSource> buildHelpCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("help")
            .requires(source -> source.hasPermission(PERMISSION_BASE))
            .executes(handler::help);
    }
}
