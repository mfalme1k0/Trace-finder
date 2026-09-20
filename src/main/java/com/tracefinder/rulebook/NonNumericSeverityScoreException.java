package com.tracefinder.rulebook;

public class NonNumericSeverityScoreException extends RulebookFormatException {
    public NonNumericSeverityScoreException(int lineNumber, String scoreText) {
        super("Rulebook line " + lineNumber + " has a non-numeric severity_score: \""
                + scoreText + "\"");
    }
}
