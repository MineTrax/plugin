package com.xinecraft.minetrax.bukkit.hooks.skinsrestorer;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class SkinsRestorerHook implements Consumer<SkinApplyEvent> {
    @Override
    public void accept(SkinApplyEvent event) {
        LoggingUtil.info("SkinsRestorerHook.onSkinApplyEvent");
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer(Player.class);
        SkinProperty skinProperty = event.getProperty();

        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            LoggingUtil.warning("PlayerData not found while listening to SkinApplyEvent");
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        if (skinProperty.getValue().isEmpty()) {
            playerSessionIntelData.skin_property = null;
            playerSessionIntelData.skin_texture_id = null;
        } else {
            playerSessionIntelData.skin_property = MinetraxBukkit.getPlugin().getGson().toJson(skinProperty);
            playerSessionIntelData.skin_texture_id = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
        }
    }
}
