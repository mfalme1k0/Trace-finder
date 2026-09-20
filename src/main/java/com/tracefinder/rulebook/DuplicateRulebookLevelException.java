package com.tracefinder.rulebook;

public class DuplicateRulebookLevelException extends RulebookFormatException {
    public DuplicateRulebookLevelException(int lineNumber, String level) {
        super("Rulebook line " + lineNumber + " redefines level \"" + level
                + "\", already defined earlier in the file");
    }
}
