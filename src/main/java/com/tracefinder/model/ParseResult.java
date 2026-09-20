package com.tracefinder.model;


import java.util.List;
//only check if log is valid, and has a valid time stamp
public class ParseResult {
    private final List<LogEntry> entries;
    private final List<MalformedLine> malformedLines;

    public ParseResult(List<LogEntry> entries, List<MalformedLine> malformedLines) {
        this.entries = entries;
        this.malformedLines = malformedLines;
    }

    public List<LogEntry> getEntries() { return entries; }
    public List<MalformedLine> getMalformedLines() { return malformedLines; }
}