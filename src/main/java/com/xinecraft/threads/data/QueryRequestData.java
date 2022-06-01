package com.xinecraft.threads.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class QueryRequestData {
    @SerializedName("secret")
    public String secret;

    @SerializedName("type")
    public String type;

    @SerializedName("params")
    public String params;
}
