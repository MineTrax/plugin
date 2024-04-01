package com.xinecraft.minetrax.data;

import lombok.Data;

@Data
public class PlayerWorldStatsIntelData {
    public PlayerWorldStatsIntelData(String wName) {
        this.world_name = wName;
    }

    public String world_name;
    public int survival_time;
    public int creative_time;
    public int adventure_time;
    public int spectator_time;
}
