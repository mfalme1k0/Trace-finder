package com.tracefinder.parser;

public abstract class LogFileException extends Exception {
    protected LogFileException(String message) {
        super(message);
    }
}
