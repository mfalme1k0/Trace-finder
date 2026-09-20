package com.tracefinder.parser;

public class MalformedLineException extends RuntimeException {
    public MalformedLineException(String line, int lineNumber) {

        super("malformed line:" + lineNumber);
    }
}
