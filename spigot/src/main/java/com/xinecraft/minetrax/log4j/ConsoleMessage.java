package com.xinecraft.minetrax.log4j;

import lombok.Data;

@Data
public class ConsoleMessage {
    private final String timestamp;
    private final String level;
    private final String line;

    @Override
    public String toString() {
        return "[" + timestamp + " " + level + "] " + line;
    }
}
