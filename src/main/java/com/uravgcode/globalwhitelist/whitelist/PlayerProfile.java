package com.uravgcode.globalwhitelist.whitelist;

import java.util.UUID;

public record PlayerProfile(UUID uuid, String name) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayerProfile other)) return false;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
