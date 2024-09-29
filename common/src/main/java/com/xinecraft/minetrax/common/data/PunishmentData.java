package com.xinecraft.minetrax.common.data;

import lombok.Data;

@Data
public class PunishmentData {
    public String plugin_name;
    public String plugin_punishment_id;
    public String type;
    public String uuid;
    public String ip_address;
    public boolean is_ipban;
    public boolean is_active;

    public long start_at;
    public long end_at;

    public String reason;
    public String notes;

    public String server_scope;
    public String origin_server_name;

    public String creator_uuid;
    public String creator_username;

    public String remover_uuid;
    public String remover_username;
    public String removed_reason;
    public long removed_at;
}
