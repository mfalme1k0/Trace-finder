package com.tracefinder.parser;

public class LogFileTooLargeException extends LogFileException {
    public LogFileTooLargeException(String path, long actualBytes, long limitBytes) {
        super("Log file '" + path + "' is " + actualBytes + " bytes, exceeding the "
                + limitBytes + "-byte limit");
    }
}
