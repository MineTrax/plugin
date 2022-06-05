package com.xinecraft.data;

import lombok.Data;

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
    public String display_name; // done
    public Boolean is_op;   // done
    public long session_started_at; // done
    public long session_ended_at;
    public int mob_kills;   // done
    public int player_kills;    // done
    public int deaths;  // done
    public int afk_time;
    public Boolean is_kicked = false;
    public Boolean is_banned = false;

    // xmin -> 5min
    public int mob_kills_xmin;  // done
    public int player_kills_xmin;   // done
    public int deaths_xmin; // done
    public int items_used_xmin; // TODO: BlockBreakEvent, PlayerItemConsumeEvent, PlayerEggThrowEvent
    public int items_mined_xmin;    // done
    public int items_picked_up_xmin;   // done
    public int items_dropped_xmin; // done
    public int items_broken_xmin;   // done
    public int items_crafted_xmin;  //// TODO: CraftItemEvent & FurnaceExtractEvent
    public int items_placed_xmin;   // done
    public int items_consumed_xmin;  // done

    public int player_ping;   // done

    public int afk_time_xmin;   //// TODO: How? using last move/chat delta time? "last_active_timestamp"

    public String world_location;
    public long last_active_timestamp;  // TODO: will be used to calculate afk

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
    }

    // public ArrayList<PlayerWorldStatIntelData> players_world_stat_intel;
}
