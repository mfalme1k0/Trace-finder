package com.tracefinder.exception;

public class RulebookTooLargeException extends RulebookException {
    public RulebookTooLargeException(String path, long actualBytes, long limitBytes) {
        super("Rulebook '" + path + "' is " + actualBytes + " bytes, exceeding the "
                + limitBytes + "-byte limit");
    }
}