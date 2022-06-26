package com.xinecraft.data;

import lombok.Data;

@Data
public class PlayerData
{
    public String uuid;

    public String username;

    public String player_id;

    public String session_uuid; // current session uuid

    // If player has linked his player on web
    public boolean is_verified;

    // If player has claimed daily reward. Can be claimed by going to https://domain.com/daily-rewards
    public long daily_rewards_claimed_at;

    public long last_active_timestamp;  // Time at which the player last made some activity. Used for AFK calculation
}
