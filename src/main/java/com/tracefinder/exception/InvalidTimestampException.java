package com.tracefinder.exception;

public class InvalidTimestampException extends FatalErrorException {
    public InvalidTimestampException(String label, String value) {
        super("Could not read " + label + " timestamp '" + value
                + "'; expected format yyyy-MM-dd HH:mm:ss");
    }
}
