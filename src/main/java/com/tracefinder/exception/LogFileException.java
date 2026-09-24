package com.tracefinder.exception;

public abstract class LogFileException extends Exception {
    protected LogFileException(String message) {
        super(message);
    }
}
