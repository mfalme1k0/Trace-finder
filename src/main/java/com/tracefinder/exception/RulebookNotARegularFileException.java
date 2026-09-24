package com.tracefinder.exception;


public class RulebookNotARegularFileException extends RulebookException {
    public RulebookNotARegularFileException(String path) {
        super("Rulebook path is not a regular file: " + path);
    }
}
