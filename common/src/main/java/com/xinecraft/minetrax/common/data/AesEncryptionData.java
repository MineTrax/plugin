package com.xinecraft.minetrax.common.data;

import lombok.Data;

@Data
public class AesEncryptionData {
    public String iv;
    public String value;
    public String mac;

    public AesEncryptionData(String iv, String value, String mac) {
        this.iv = iv;
        this.value = value;
        this.mac = mac;
    }
}
