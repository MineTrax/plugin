package com.xinecraft.minetrax.utils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class SystemUtil {
    /**
     * Check how active the system is (CPU) or if not available, using system load average.
     * <p>
     * - On some OSes CPU usage information is not available, and system load average is used instead.
     * - On some OSes system load average is not available.
     *
     * @return 0.0 to 100.0 if CPU, or system load average, or -1 if nothing is available.
     */
    public static double getAverageCpuLoad() {
        double averageUsage;
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                averageUsage = nativeOsBean.getSystemCpuLoad();
            } else {
                int availableProcessors = osBean.getAvailableProcessors();
                averageUsage = osBean.getSystemLoadAverage() / availableProcessors;
            }
            if (averageUsage < 0) {
                averageUsage = -1;
            }
        } catch (UnsatisfiedLinkError e) {
            averageUsage = -1;
        }
        return averageUsage * 100.0;
    }

    public static long getFreeDiskSpaceInKiloBytes() {
        long freeDiskInKb = 0;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            try {
                FileStore store = Files.getFileStore(root);
                freeDiskInKb += store.getUsableSpace() / 1024;
            } catch (IOException e) {
                freeDiskInKb += 0;
            }
        }
        return freeDiskInKb;
    }
}
