package com.xinecraft.minetrax.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiColor {
    private static final String COLOR_PATTERN = "\u001b[38;5;%dm";
    private static final String FORMAT_PATTERN = "\u001b[%dm";

    /**
     * Converts a String with Minecraft-ColorCodes into Ansi-Colors.
     * Returns null if the message is null
     * @author Timeout
     *
     * @param message the message. Can be null
     * @param colorFormatter the formatter to translate messages
     * @return the converted message or null if the message is null
     */
    public static String convertStringMessage(String message, char colorFormatter) {
        // Continur if String is neither not null nor empty
        if(message != null && !message.isEmpty()) {
            // copy of string
            String messageCopy = String.copyValueOf(message.toCharArray()) + ConsoleColor.RESET.ansiColor;
            // create Matcher to search for colorcodes
            Matcher matcher = Pattern.compile(String.format("(%c[0-9a-fk-or])(?!.*\1)", colorFormatter)).matcher(message);
            // run through result
            while(matcher.find()) {
                // get Result
                String result = matcher.group(1);
                // get ColorCode
                ConsoleColor color = ConsoleColor.getColorByCode(result.charAt(1));
                // replace color
                messageCopy = messageCopy.replace(result, color.getAnsiColor());
            }
            // return converted String
            return messageCopy;
        }
        // return message for nothing to compile
        return message;
    }

    /**
     * Represents a set of Minecraft-ColorCodes and their ANSI-Codes
     * @author Timeout
     *
     */
    private enum ConsoleColor {

        BLACK('0', COLOR_PATTERN, 0),
        DARK_GREEN('2', COLOR_PATTERN, 2),
        DARK_RED('4', COLOR_PATTERN, 1),
        GOLD('6', COLOR_PATTERN, 172),
        DARK_GREY('8', COLOR_PATTERN, 8),
        GREEN('a', COLOR_PATTERN, 10),
        RED('c', COLOR_PATTERN, 9),
        YELLOW('e', COLOR_PATTERN, 11),
        DARK_BLUE('1', COLOR_PATTERN, 4),
        DARK_AQUA('3', COLOR_PATTERN, 30),
        DARK_PURPLE('5', COLOR_PATTERN, 54),
        GRAY('7', COLOR_PATTERN, 246),
        BLUE('9', COLOR_PATTERN, 4),
        AQUA('b', COLOR_PATTERN, 51),
        LIGHT_PURPLE('d', COLOR_PATTERN, 13),
        WHITE('f', COLOR_PATTERN, 15),
        STRIKETHROUGH('m', FORMAT_PATTERN, 9),
        ITALIC('o', FORMAT_PATTERN, 3),
        BOLD('l', FORMAT_PATTERN, 1),
        UNDERLINE('n', FORMAT_PATTERN, 4),
        RESET('r', FORMAT_PATTERN, 0),
        OBFUSCATED('k', FORMAT_PATTERN, 9);


        private char bukkitColor;
        private String ansiColor;

        private ConsoleColor(char bukkitColor, String pattern, int ansiCode) {
            this.bukkitColor = bukkitColor;
            this.ansiColor = String.format(pattern, ansiCode);
        }

        /**
         * Searches if the code is a valid colorcode and returns the right enum
         * @author Timeout
         *
         * @param code the Minecraft-ColorCode without Formatter-Char
         * @return the Color enum or null if no enum can be found
         */
        public static ConsoleColor getColorByCode(char code) {
            // run trough colors
            for(ConsoleColor color: values()) {
                // check code
                if(color.bukkitColor == code) return color;
            }
            // return null for not found
            throw new IllegalArgumentException("Color with code " + code + " does not exists");
        }

        /**
         * Returns the ANSI-ColorCode of the Colorcode
         * @author Timeout
         *
         * @return the Ansi-ColorCode
         */
        public String getAnsiColor() {
            return ansiColor;
        }
    }
}
