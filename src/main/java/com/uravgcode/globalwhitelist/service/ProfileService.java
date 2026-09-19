package com.uravgcode.globalwhitelist.service;

import com.uravgcode.globalwhitelist.whitelist.PlayerProfile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProfileService {
    boolean isProfile(String playerName);

    CompletableFuture<Optional<PlayerProfile>> getProfile(String playerName);
}
