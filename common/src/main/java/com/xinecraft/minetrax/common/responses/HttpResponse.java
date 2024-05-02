package com.xinecraft.minetrax.common.responses;

public class HttpResponse {
    private final int code;
    private final Boolean isSuccessful;
    private final String body;
    private final String message;

    public HttpResponse(int code, Boolean isSuccessful, String message, String body) {
        this.code = code;
        this.isSuccessful = isSuccessful;
        this.body = body;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public Boolean isSuccessful() {
        return isSuccessful;
    }

    public String body() {
        return body;
    }

    public String message() {
        return message;
    }
}
