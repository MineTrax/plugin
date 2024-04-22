package com.xinecraft.minetrax.bukkit.data;

import lombok.Data;

@Data
public class PlayerPvpKillData
{
    public String killer_uuid;
    public String killer_username;
    public String victim_uuid;
    public String victim_username;

    public long killed_at;
    public String weapon;

    public String session_uuid;
    public String world_name;
    public String world_location;
}
