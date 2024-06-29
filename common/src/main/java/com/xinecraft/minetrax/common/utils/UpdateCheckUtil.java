package com.xinecraft.minetrax.common.utils;

import com.xinecraft.minetrax.common.MinetraxCommon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheckUtil {

    private final int resourceId;

    public UpdateCheckUtil(int resourceId) {
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        MinetraxCommon.getInstance().getScheduler().runAsync(() -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                MinetraxCommon.getInstance().getLogger().error("Unable to check for updates: " + exception.getMessage());
            }
        });
    }

    public static void checkForUpdate(int resourceId, String currentVersion) {
        new UpdateCheckUtil(resourceId).getVersion(version -> {
            if (!currentVersion.equals(version)) {
                MinetraxCommon.getInstance().getLogger().warning("A new version of Minetrax is available. Please update to latest version " + version);
                MinetraxCommon.getInstance().getLogger().warning("Download https://www.github.com/minetrax/plugin/releases/latest");
            } else {
                MinetraxCommon.getInstance().getLogger().info("Yay! You are using the latest version of Minetrax.");
            }
        });
    }
}
