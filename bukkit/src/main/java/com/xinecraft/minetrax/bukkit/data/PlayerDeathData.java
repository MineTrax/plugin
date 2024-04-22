package com.xinecraft.minetrax.bukkit.data;

import lombok.Data;

@Data
public class PlayerDeathData
{
    public String player_uuid;
    public String player_username;
    public String cause;
    public String killer_uuid;
    public String killer_username;

    public String killer_entity_id;
    public String killer_entity_name;

    public long died_at;

    public String session_uuid;
    public String world_name;
    public String world_location;
}
