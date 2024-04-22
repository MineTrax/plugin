package com.xinecraft.minetrax.bukkit.utils;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class WhoisResponseData
{
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public Data data;

    public static class Data
    {
        @SerializedName("count")
        public int count;
        @SerializedName("players")
        public ArrayList<Player> players;
        @SerializedName("geo")
        public Geo geo;
    }

    public static class Player
    {
        @SerializedName("username")
        public String username;
        @SerializedName("uuid")
        public String uuid;
        @SerializedName("position")
        public String position;
        @SerializedName("rating")
        public String rating;
        @SerializedName("total_score")
        public String total_score;
        @SerializedName("last_seen_at")
        public String last_seen_at;
        @SerializedName("rank")
        public String rank;
        @SerializedName("country")
        public String country;
        @SerializedName("user")
        public String user;
        @SerializedName("url")
        public String url;
    }

    public static class Geo
    {
        @SerializedName("ip")
        public String ip;
        @SerializedName("iso_code")
        public String iso_code;
        @SerializedName("city")
        public String city;
        @SerializedName("state_name")
        public String state_name;
        @SerializedName("country")
        public String country;
    }
}
