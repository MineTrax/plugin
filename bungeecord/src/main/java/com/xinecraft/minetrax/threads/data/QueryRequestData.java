package com.xinecraft.minetrax.threads.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class QueryRequestData {
    @SerializedName("api_key")
    public String api_key;

    @SerializedName("type")
    public String type;

    @SerializedName("params")
    public String params;
}
