package com.tracefinder;

public class InvalidArgumentCountException extends FatalErrorException {
    public InvalidArgumentCountException(int actualCount) {
        super("Expected 3 arguments (log file, rulebook file, report file) "
                + "or 5 arguments (the same three, plus a start and end timestamp "
                + "for a time window), got " + actualCount);
    }
}
