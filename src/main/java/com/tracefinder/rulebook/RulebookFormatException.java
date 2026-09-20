package com.tracefinder.rulebook;

/**
 * Thrown when rules.csv exists and is readable, but its content doesn't
 * match the expected format (wrong header, wrong number of columns,
 * a severity score that isn't a number, etc.).

 * This is a *fatal* condition for the tool — unlike a malformed log
 * line, there's no safe way to keep going with a broken rulebook, since
 * every later scoring decision depends on it being correct.
 */
public class RulebookFormatException extends Exception {
    public RulebookFormatException(String message) {
        super(message);
    }
}