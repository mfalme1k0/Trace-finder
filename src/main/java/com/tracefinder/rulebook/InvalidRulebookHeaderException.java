package com.tracefinder.rulebook;

public class InvalidRulebookHeaderException extends RulebookFormatException {
    public InvalidRulebookHeaderException(String expectedHeader, String actualHeader) {
        super("Rulebook header must be \"" + expectedHeader + "\", found: \"" + actualHeader + "\"");
    }
}
