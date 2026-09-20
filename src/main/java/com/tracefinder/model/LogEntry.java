package com.tracefinder.model;

import java.time.LocalDateTime;

/**
 * A single, successfully parsed log entry.
 * Immutable: once built, its fields never change.
 */
public class LogEntry {
    private final LocalDateTime timestamp;
    private final String level;
    private final String sourceIp;
    private final String target;
    private final String action;
    private final String rawLine; // original text, needed verbatim for the report
    private final int lineNumber; // 1-indexed position in the original file

    public LogEntry(LocalDateTime timestamp, String level, String sourceIp,
                    String target, String action, String rawLine, int lineNumber) {
        this.timestamp = timestamp;
        this.level = level;
        this.sourceIp = sourceIp;
        this.target = target;
        this.action = action;
        this.rawLine = rawLine;
        this.lineNumber = lineNumber;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getSourceIp() { return sourceIp; }
    public String getTarget() { return target; }
    public String getAction() { return action; }
    public String getRawLine() { return rawLine; }
    public int getLineNumber() { return lineNumber; }
}