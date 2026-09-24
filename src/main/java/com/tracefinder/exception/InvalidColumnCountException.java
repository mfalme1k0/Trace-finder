package com.tracefinder.exception;

public class InvalidColumnCountException extends RulebookFormatException {
    public InvalidColumnCountException(int lineNumber, int expectedCount, String line) {
        super("Rulebook line " + lineNumber + " does not have "
                + expectedCount + " columns: \"" + line + "\"");
    }
}
