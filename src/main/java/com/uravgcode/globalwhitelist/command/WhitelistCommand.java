package com.uravgcode.globalwhitelist.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.uravgcode.globalwhitelist.config.MessagesConfig;
import com.uravgcode.globalwhitelist.config.WhitelistConfig;
import com.uravgcode.globalwhitelist.service.ProfileService;
import com.uravgcode.globalwhitelist.whitelist.Whitelist;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Map;

public final class WhitelistCommand {
    private static final String PERMISSION_BASE = "globalwhitelist";
    private static final String PERMISSION_ADMIN = "globalwhitelist.admin";

    public static BrigadierCommand createCommand(
        ProxyServer proxy,
        Map<String, ProfileService> profileServices,
        Whitelist whitelist,
        WhitelistConfig config,
        MessagesConfig messages
    ) {
        final var handler = new WhitelistCommandHandler(proxy, profileServices, whitelist, config, messages);

        final var node = BrigadierCommand.literalArgumentBuilder("globalwhitelist")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::help)
            .then(buildHelpCommand(handler))
            .then(buildAddCommand(handler))
            .then(buildRemoveCommand(handler))
            .then(buildListCommand(handler))
            .then(buildOnCommand(handler))
            .then(buildOffCommand(handler))
            .then(buildEnforcedCommand(handler))
            .then(buildUnenforcedCommand(handler))
            .then(buildReloadCommand(handler))
            .then(buildVersionCommand(handler))
            .build();

        return new BrigadierCommand(node);
    }

    private static LiteralArgumentBuilder<CommandSource> buildHelpCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("help")
            .requires(source -> source.hasPermission(PERMISSION_BASE))
            .executes(handler::help);
    }

    private static LiteralArgumentBuilder<CommandSource> buildAddCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("add")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests(handler::suggestOnlinePlayers)
                .executes(handler::add)
                .then(BrigadierCommand.requiredArgumentBuilder("platform", StringArgumentType.word())
                    .suggests(handler::suggestPlatforms)
                    .executes(handler::add))
            );
    }

    private static LiteralArgumentBuilder<CommandSource> buildRemoveCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("remove")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests(handler::suggestWhitelistedPlayers)
                .executes(handler::remove)
                .then(BrigadierCommand.requiredArgumentBuilder("platform", StringArgumentType.word())
                    .suggests(handler::suggestPlatforms)
                    .executes(handler::remove))
            );
    }

    private static LiteralArgumentBuilder<CommandSource> buildListCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("list")
            .requires(source -> source.hasPermission(PERMISSION_BASE) || source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::list);
    }

    private static LiteralArgumentBuilder<CommandSource> buildOnCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("on")
            .requires(source -> source.hasPermission(PERMISSION_ADMIN))
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

    private static LiteralArgumentBuilder<CommandSource> buildVersionCommand(WhitelistCommandHandler handler) {
        return BrigadierCommand.literalArgumentBuilder("version")
            .requires(source -> source.hasPermission(PERMISSION_ADMIN))
            .executes(handler::version);
    }
}
