package com.xinecraft.minetrax.common.responses;

import lombok.Data;

@Data
public class GenericApiResponse {
    int code;
    String type;
    String status;
    String message;
    Object data;
}
