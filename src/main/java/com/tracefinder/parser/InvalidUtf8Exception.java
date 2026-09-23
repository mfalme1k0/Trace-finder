package com.tracefinder.parser;

public class InvalidUtf8Exception extends LogFileException {
    public InvalidUtf8Exception(long approxByteOffset) {
        super("Log file contains invalid UTF-8 (detected while reading around byte offset "
                + approxByteOffset + ")");
    }
}
