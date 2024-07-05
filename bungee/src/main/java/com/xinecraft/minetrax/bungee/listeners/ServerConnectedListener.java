package com.xinecraft.minetrax.bungee.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.bungee.utils.BungeeSkinUtil;
import com.xinecraft.minetrax.common.MinetraxCommon;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.property.SkinProperty;

public class ServerConnectedListener implements Listener {
    @EventHandler
    public void sendUpdateSkinMessageOnServerConnected(ServerConnectedEvent event) {
        if (!MinetraxBungee.getPlugin().getHasSkinsRestorer()) {
            return;
        }

        ProxiedPlayer player = event.getPlayer();
        MinetraxBungee.getPlugin().getProxy().getScheduler().runAsync(MinetraxBungee.getPlugin(), () -> {
            SkinProperty skinProperty = BungeeSkinUtil.getSkinForPlayer(player.getUniqueId(), player.getName());
            if (skinProperty != null) {
                String skinPropertyJson = MinetraxBungee.getPlugin().getGson().toJson(skinProperty);
                String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
                String playerUuid = player.getUniqueId().toString();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("UpdatePlayerSkin");
                out.writeUTF(playerUuid);
                out.writeUTF(skinPropertyJson);
                out.writeUTF(skinTextureId);
                event.getServer().sendData(MinetraxCommon.PLUGIN_MESSAGE_CHANNEL, out.toByteArray());
            }
        });
    }
}
