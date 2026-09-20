package com.tracefinder.rulebook;

public class RulebookIsEmptyException extends RulebookFormatException {

    public RulebookIsEmptyException() {
        super("Rulebook is empty");
    }
}