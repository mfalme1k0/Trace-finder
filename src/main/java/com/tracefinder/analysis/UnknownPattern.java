package com.tracefinder.analysis;

public class UnknownPattern {
    private final int lineNumber;
    private final String rawLine;

    public UnknownPattern(int lineNumber, String rawLine) {
        this.lineNumber = lineNumber;
        this.rawLine = rawLine;
    }

    public int getLineNumber() { return lineNumber; }
    public String getRawLine() { return rawLine; }
}