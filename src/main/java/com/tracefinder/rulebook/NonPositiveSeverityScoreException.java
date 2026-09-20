package com.tracefinder.rulebook;

public class NonPositiveSeverityScoreException extends RulebookFormatException {
    public NonPositiveSeverityScoreException(int lineNumber, int score) {
        super("Rulebook line " + lineNumber + " has a non-positive severity_score: " + score);
    }
}
