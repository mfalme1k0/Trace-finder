package com.tracefinder;

public abstract class TraceFinderException extends Exception {
    protected TraceFinderException(String message) { super(message); }
    protected TraceFinderException(String message, Throwable cause) { super(message, cause); }
}