package com.xinecraft.data;

import lombok.Data;

@Data
public class WorldData {
    public String world_name;
    public Number world_border;
    public String environment;
    public Integer chunks_loaded;
    public Number game_time;
    public Number online_players;
}
