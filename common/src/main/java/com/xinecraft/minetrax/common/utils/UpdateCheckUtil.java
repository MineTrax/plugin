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
}
