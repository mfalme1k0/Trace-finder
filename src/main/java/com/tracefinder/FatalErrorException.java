package com.tracefinder;

public class FatalErrorException extends TraceFinderException {
    public FatalErrorException(String message) {
        super(message);
    }
}