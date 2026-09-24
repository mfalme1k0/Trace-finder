package com.tracefinder.exception;


public class NegativeSeverityScoreException extends RulebookFormatException {
    public NegativeSeverityScoreException(int lineNumber, int score) {
        super("Rulebook line " + lineNumber + " has a negative severity_score: " + score);
    }
}
