package com.xinecraft.minetrax.common.banwarden;

import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;

public class BanWarden {
    private final BanWardenHook hook;

    public BanWarden(BanWardenHook hook) {
        this.hook = hook;
    }

    public void sync(BanWardenSyncType type) {
        hook.sync(type);
    }
}
