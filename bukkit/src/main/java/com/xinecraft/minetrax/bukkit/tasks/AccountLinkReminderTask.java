package com.xinecraft.minetrax.bukkit.tasks;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.entity.Player;

import java.util.List;

public class AccountLinkReminderTask implements Runnable {
    @Override
    public void run() {
        Boolean isAlreadyLinkedReminderEnabled = MinetraxBukkit.getPlugin().getIsRemindPlayerWhenAlreadyLinkedEnabled();

        for (Player player : MinetraxBukkit.getPlugin().getServer().getOnlinePlayers()) {
            PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());

            if (playerData == null) {
                continue;
            }
            if (playerData.is_verified && !isAlreadyLinkedReminderEnabled) {
                continue;
            }

            List<String> messageList = playerData.is_verified ? MinetraxBukkit.getPlugin().getRemindPlayerWhenAlreadyLinkedMessage() : MinetraxBukkit.getPlugin().getRemindPlayerToLinkMessage();
            for (String line : messageList) {
                line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                line = line.replace("{PROFILE_URL}", playerData.profile_link);
                MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
            }
        }
    }
}
