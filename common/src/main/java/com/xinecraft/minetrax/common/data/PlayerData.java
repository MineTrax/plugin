package com.xinecraft.minetrax.common.data;

import lombok.Data;

@Data
public class PlayerData {
    public String uuid;
    public String username;
    public String player_id;
    public String session_uuid;                 // current session uuid
    public boolean is_verified;                 // If player has linked his player on web
    public long daily_rewards_claimed_at;       // Not implemented yet. If player has claimed daily reward. Can be claimed by going to https://domain.com/daily-rewards

    public float rating;
    public int total_score;
    public int position;
    public long total_mob_kills;
    public long total_player_kills;
    public long total_deaths;
    public long play_time;                      // in seconds
    public long afk_time;                       // in seconds
    public String first_seen_at;                // String diffForHumans()
    public String last_seen_at;                 // String diffForHumans()
    public String profile_link;                 // Link to player's profile on web

    public CountryData country;
    public PlayerRankData rank;

    public long last_active_timestamp;          // Time at which the player last made some activity. Used for AFK calculation
}
