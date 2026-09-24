package com.tracefinder.exception;

import java.nio.file.Path;

public class LogFileReadException extends FatalErrorException {
    public LogFileReadException(Path logPath, Throwable cause) {
        super("Could not read log file '" + logPath + "': " + cause.getMessage());
    }
}
