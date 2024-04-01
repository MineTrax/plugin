package com.xinecraft.minetrax.data;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ServerIntelData
{
    public String server_id;
    public String server_session_id;   // Unique every time the server is restarted
    public Number  online_players;
    public Number max_players;
    public Double tps;
    public Integer chunks_loaded;
    public Number max_memory;
    public Number total_memory;
    public Number free_memory;
    public Number available_cpu_count;
    public Double cpu_load;
    public Number uptime;
    public Number free_disk_in_kb;
    public String motd;
    public String server_version;
    public ArrayList<WorldData> world_data;
}
