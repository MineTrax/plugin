package com.xinecraft.minetrax.bukkit.threads.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class QueryResponseData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    public QueryResponseData(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
