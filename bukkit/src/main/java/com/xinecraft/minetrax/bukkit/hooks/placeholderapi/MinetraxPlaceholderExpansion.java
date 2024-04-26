package com.xinecraft.minetrax.bukkit.hooks.placeholderapi;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinetraxPlaceholderExpansion extends PlaceholderExpansion {
    private final MinetraxBukkit plugin;

    public MinetraxPlaceholderExpansion(MinetraxBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "minetrax";
    }

    @Override
    public @NotNull String getAuthor() {
        return "xinecraft";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        // Get playerData from HashMap
        PlayerData playerData = plugin.playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) return "";

        if(params.equalsIgnoreCase("player_id")) {
            return playerData.player_id;
        }

        if(params.equalsIgnoreCase("player_session_uuid")) {
            return playerData.session_uuid;
        }

        if(params.equalsIgnoreCase("player_is_verified")) {
            return String.valueOf(playerData.is_verified);
        }

        if(params.equalsIgnoreCase("player_daily_rewards_claimed_at")) {
            return String.valueOf(playerData.daily_rewards_claimed_at);
        }

        if(params.equalsIgnoreCase("player_last_active_timestamp")) {
            return String.valueOf(playerData.last_active_timestamp);
        }

        if(params.equalsIgnoreCase("player_rating")) {
            return String.valueOf(playerData.rating);
        }

        if(params.equalsIgnoreCase("player_total_score")) {
            return String.valueOf(playerData.total_score);
        }

        if(params.equalsIgnoreCase("player_position")) {
            return String.valueOf(playerData.position);
        }

        if(params.equalsIgnoreCase("player_first_seen_at")) {
            return playerData.first_seen_at;
        }

        if(params.equalsIgnoreCase("player_last_seen_at")) {
            return playerData.last_seen_at;
        }

        if(params.equalsIgnoreCase("player_profile_link")) {
            return playerData.profile_link;
        }

        if(params.equalsIgnoreCase("player_country_id")) {
            return playerData.country != null ? playerData.country.id : "";
        }

        if(params.equalsIgnoreCase("player_country_name")) {
            return playerData.country != null ? playerData.country.name : "";
        }

        if(params.equalsIgnoreCase("player_country_iso_code")) {
            return playerData.country != null ? playerData.country.iso_code : "";
        }

        if(params.equalsIgnoreCase("player_rank_id")) {
            return playerData.rank != null ? playerData.rank.id : "";
        }

        if(params.equalsIgnoreCase("player_rank_shortname")) {
            return playerData.rank != null ? playerData.rank.shortname : "";
        }

        if(params.equalsIgnoreCase("player_rank_name")) {
            return playerData.rank != null ? playerData.rank.name : "";
        }

        return null;
    }
}
