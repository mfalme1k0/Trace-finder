package com.tracefinder.exception;

public class RulebookIsEmptyException extends RulebookFormatException {

    public RulebookIsEmptyException() {
        super("Rulebook is empty");
    }
}