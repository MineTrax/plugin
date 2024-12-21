package com.xinecraft.minetrax.bukkit.commands;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.actions.LinkAccount;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountLinkCommand implements CommandExecutor {
    private final boolean isConfirmationEnabled = MinetraxBukkit.getPlugin().getIsPlayerLinkConfirmationEnabled();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            MinetraxBukkit.getPlugin().getLogger().info("Error: Only players can execute that command.");
            return false;
        }

        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());

        // Already linked
        if (playerData != null && playerData.is_verified) {
            for (String line : MinetraxBukkit.getPlugin().getPlayerLinkInitAlreadyLinkedMessage()) {
                line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
            }
            return true;
        }

        // Send Init message if only /link
        if (strings.length == 0) {
            List<String> withoutParamsMessage = MinetraxBukkit.getPlugin().getPlayerLinkInitMessage();
            for (String line : withoutParamsMessage) {
                line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
            }
            return true;
        }

        // Send Linking message if /link <otp>
        String otpCode = strings[0];
        if (isConfirmationEnabled) {
            ConcurrentHashMap<String, String> pendingVerifications = MinetraxBukkit.getPlugin().getPlayerLinkPendingVerificationMap();
            if (otpCode.equalsIgnoreCase("confirm")) {
                String pendingOtp = pendingVerifications.get(player.getUniqueId().toString());
                if (pendingOtp == null) {
                    MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(
                            MineDown.parse("&cNo OTP pending confirmation. Please enter your OTP first.")
                    );
                    return true;
                }
                String processingMessage = MinetraxBukkit.getPlugin().getProcessingMessage();
                MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(processingMessage));
                linkAccount(player, pendingOtp);
                pendingVerifications.remove(player.getUniqueId().toString());
            } else if (otpCode.equalsIgnoreCase("deny") || otpCode.equalsIgnoreCase("cancel")) {
                pendingVerifications.remove(player.getUniqueId().toString());
                String cancelledMessage = MinetraxBukkit.getPlugin().getCancelledMessage();
                MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(cancelledMessage));
            } else {
                pendingVerifications.put(player.getUniqueId().toString(), otpCode);

                String playerLinkConfirmationTitle = MinetraxBukkit.getPlugin().getPlayerLinkConfirmationTitle();
                String playerLinkConfirmationSubtitle = MinetraxBukkit.getPlugin().getPlayerLinkConfirmationSubtitle();
                if (!playerLinkConfirmationSubtitle.isBlank() || !playerLinkConfirmationTitle.isBlank()) {
                    final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(6000), Duration.ofMillis(1000));
                    final Title title = Title.title(MineDown.parse(playerLinkConfirmationTitle), MineDown.parse(playerLinkConfirmationSubtitle), times);
                    MinetraxBukkit.getPlugin().adventure().player(player).showTitle(title);
                }

                for (String line : MinetraxBukkit.getPlugin().getPlayerLinkConfirmationMessage()) {
                    line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                    line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                    MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                }
            }
        } else {
            linkAccount(player, otpCode);
        }

        return true;
    }

    private void linkAccount(Player player, String otpCode) {
        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), () -> {
            try {
                GenericApiResponse response = LinkAccount.link(
                        player.getUniqueId().toString(),
                        otpCode,
                        MinetraxBukkit.getPlugin().getApiServerId()
                );

                if (response.getCode() != 200) {
                    for (String line : MinetraxBukkit.getPlugin().getPlayerLinkErrorMessage()) {
                        line = line.replace("{ERROR_MESSAGE}", response.getMessage());
                        MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                    }
                } else {
                    for (String line : MinetraxBukkit.getPlugin().getPlayerLinkSuccessMessage()) {
                        line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                        line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                        MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                    }
                }
            } catch (Exception e) {
                for (String line : MinetraxBukkit.getPlugin().getPlayerLinkErrorMessage()) {
                    line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                    line = line.replace("{ERROR_MESSAGE}", "Unknown error! Please contact admin.");
                    MinetraxBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                }
            }
        });
    }
}
