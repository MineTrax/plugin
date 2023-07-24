package com.xinecraft.data;

import lombok.Data;

import java.util.HashMap;

@Data
public class PlayerSessionIntelData
{
    public String uuid; // player uuid
    public String username;
    public String player_id;    // id of player in website
    // Session
    public String server_id;    // done
    public String session_uuid; // done
    public String ip_address;   // done
    public String join_address; // server hostname using which the player joined. Can be null.
    public String display_name; // done
    public Boolean is_op;   // done
    public long session_started_at; // done
    public long session_ended_at;
    public int mob_kills;   // done
    public int player_kills;    // done
    public int deaths;  // done
    public int afk_time;    // total time player is afk this session
    public int play_time;   // total time player is online (including afk) for this session
    public Boolean is_kicked = false;
    public Boolean is_banned = false;

    // xmin -> 5min
    public int mob_kills_xmin;  // done
    public int player_kills_xmin;   // done
    public int deaths_xmin; // done
    public int items_used_xmin; // done
    public int items_mined_xmin;    // done
    public int items_picked_up_xmin;   // done
    public int items_dropped_xmin; // done
    public int items_broken_xmin;   // done
    public int items_crafted_xmin;  // done
    public int items_placed_xmin;   // done
    public int items_consumed_xmin;  // done
    public int afk_time_xmin;

    public int fish_caught_xmin;
    public int items_enchanted_xmin; // TESTIT
    public int times_slept_in_bed_xmin;
    public int jumps_xmin; // TODO
    public int raids_won_xmin;  // TESTIT
    public double distance_traveled_xmin;
    public double distance_traveled_on_land_xmin;
    public double distance_traveled_on_water_xmin;
    public double distance_traveled_on_air_xmin;
    public double pvp_damage_given_xmin; // Damage given to other players
    public double pvp_damage_taken_xmin; // Damage taken from other players

    public int player_ping;   // done

    public String world_location;   // Hold world location when the intel was last reported
    public String world_name;       // Hold world name when the intel was last reported

    // Plugins
    public double vault_balance;
    public String[] vault_groups;

    public void resetXminKeys() {
        this.mob_kills_xmin = 0;
        this.player_kills_xmin = 0;
        this.deaths_xmin = 0;
        this.items_used_xmin = 0;
        this.items_mined_xmin = 0;
        this.items_picked_up_xmin = 0;
        this.items_dropped_xmin = 0;
        this.items_broken_xmin = 0;
        this.items_crafted_xmin = 0;
        this.items_placed_xmin = 0;
        this.items_consumed_xmin = 0;
        this.afk_time_xmin = 0;

        this.fish_caught_xmin = 0;
        this.items_enchanted_xmin = 0;
        this.times_slept_in_bed_xmin = 0;
        this.jumps_xmin = 0;
        this.raids_won_xmin = 0;
        this.distance_traveled_xmin = 0;
        this.distance_traveled_on_land_xmin = 0;
        this.distance_traveled_on_water_xmin = 0;
        this.distance_traveled_on_air_xmin = 0;
        this.pvp_damage_given_xmin = 0;
        this.pvp_damage_taken_xmin = 0;
    }

    public HashMap<String, PlayerWorldStatsIntelData> players_world_stat_intel;
}
