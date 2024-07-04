package com.xinecraft.minetrax.velocity.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
import com.xinecraft.minetrax.velocity.utils.VelocitySkinUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.property.SkinProperty;

public class ServerConnectedListener {
    @Subscribe
    public void sendUpdateSkinMessageOnServerConnected(ServerConnectedEvent event) {
        if (!MinetraxVelocity.getPlugin().getHasSkinsRestorer()) {
            return;
        }

        Player player = event.getPlayer();
        RegisteredServer server = event.getServer();
        MinetraxVelocity.getPlugin().getProxyServer().getScheduler().buildTask(MinetraxVelocity.getPlugin(), () -> {
            SkinProperty skinProperty = VelocitySkinUtil.getSkinForPlayer(player.getUniqueId(), player.getUsername());
            if (skinProperty != null) {
                String skinPropertyJson = MinetraxVelocity.getPlugin().getGson().toJson(skinProperty);
                String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
                String playerUuid = player.getUniqueId().toString();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("UpdatePlayerSkin");
                out.writeUTF(playerUuid);
                out.writeUTF(skinPropertyJson);
                out.writeUTF(skinTextureId);
                server.sendPluginMessage(MinetraxVelocity.PLUGIN_MESSAGE_CHANNEL, out.toByteArray());
            }
        }).delay(1, java.util.concurrent.TimeUnit.SECONDS).schedule();  // Run after 1 seconds as plugin message channel is not ready yet.
    }
}
