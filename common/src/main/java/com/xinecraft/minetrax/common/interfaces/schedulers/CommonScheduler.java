package com.xinecraft.minetrax.common.interfaces.schedulers;

public interface CommonScheduler {
    void run(Runnable runnable);
    void runAsync(Runnable runnable);
}
