package com.xinecraft.minetrax.bungee.hooks.skinsrestorer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.SkinProperty;

import java.util.function.Consumer;

public class SkinsRestorerHook implements Consumer<SkinApplyEvent> {
    @Override
    public void accept(SkinApplyEvent event) {
        LoggingUtil.debug("SkinsRestorerHook.onSkinApplyEvent");
        if (event.isCancelled()) {
            return;
        }
        ProxiedPlayer player = event.getPlayer(ProxiedPlayer.class);
        SkinProperty skinProperty = event.getProperty();

        if (player != null && !skinProperty.getValue().isEmpty()) {
            String skinPropertyJson = MinetraxBungee.getPlugin().getGson().toJson(skinProperty);
            String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
            String playerUuid = player.getUniqueId().toString();

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UpdatePlayerSkin");
            out.writeUTF(playerUuid);
            out.writeUTF(skinPropertyJson);
            out.writeUTF(skinTextureId);

            MinetraxBungee.getPlugin().getProxy().getScheduler().runAsync(MinetraxBungee.getPlugin(), () -> {
                player.getServer().sendData("BungeeCord", out.toByteArray());
            });
        }
    }
}
