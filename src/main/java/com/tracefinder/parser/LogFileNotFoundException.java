package com.tracefinder.parser;

public class LogFileNotFoundException extends LogFileException {
    public LogFileNotFoundException(String path) {
        super("Log file not found: " + path);
    }
}
