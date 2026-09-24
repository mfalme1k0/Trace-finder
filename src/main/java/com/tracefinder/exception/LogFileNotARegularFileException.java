package com.tracefinder.exception;

public class LogFileNotARegularFileException extends LogFileException {
    public LogFileNotARegularFileException(String path) {
        super("Log file path is not a regular file: " + path);
    }
}
