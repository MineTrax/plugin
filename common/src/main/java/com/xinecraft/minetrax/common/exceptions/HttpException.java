package com.xinecraft.minetrax.common.exceptions;

import com.xinecraft.minetrax.common.responses.HttpResponse;

public class HttpException extends Exception {
    public HttpException(HttpResponse response, String message) {
        super(message + " : " + response.code() + " : " + response.message() + " : " + response.body());
    }
}
