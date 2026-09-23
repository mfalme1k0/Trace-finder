package com.tracefinder;

public class InvalidTimeWindowException extends FatalErrorException {
    public InvalidTimeWindowException(String startArg, String endArg) {
        super("Start timestamp '" + startArg + "' must not be after end timestamp '" + endArg + "'");
    }
}
