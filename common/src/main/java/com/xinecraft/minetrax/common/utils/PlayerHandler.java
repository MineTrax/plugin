package com.xinecraft.minetrax.common.utils;

import java.util.Optional;
import java.util.UUID;

public interface PlayerHandler {
    Optional<Object> getPlayer(UUID playerUuid);
    void applySkin(Object player);
}
