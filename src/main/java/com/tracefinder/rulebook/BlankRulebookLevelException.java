package com.tracefinder.rulebook;

public class BlankRulebookLevelException extends RulebookFormatException {
    public BlankRulebookLevelException(int lineNumber, String line) {
        super("Rulebook line " + lineNumber + " has a blank level: \"" + line + "\"");
    }
}
