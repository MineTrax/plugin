package com.xinecraft.minetrax.velocity.hooks.skinsrestorer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
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
        Player player = event.getPlayer(Player.class);
        SkinProperty skinProperty = event.getProperty();

        if (player != null && !skinProperty.getValue().isEmpty()) {
            String skinPropertyJson = MinetraxVelocity.getPlugin().getGson().toJson(skinProperty);
            String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
            String playerUuid = player.getUniqueId().toString();

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UpdatePlayerSkin");
            out.writeUTF(playerUuid);
            out.writeUTF(skinPropertyJson);
            out.writeUTF(skinTextureId);

            MinetraxVelocity.getPlugin().getCommon().getScheduler().runAsync(() -> {
                player.getCurrentServer().ifPresent(connection -> {
                    connection.getServer().sendPluginMessage(MinetraxVelocity.PLUGIN_MESSAGE_CHANNEL, out.toByteArray());
                });
            });
        }
    }
}
