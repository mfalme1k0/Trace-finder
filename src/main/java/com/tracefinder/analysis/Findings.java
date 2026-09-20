package com.tracefinder.analysis;

import com.tracefinder.model.LogEntry;

import java.util.List;
import java.util.Map;

/**
 * The four findings produced by analysis. One object, four fields —
 * same "bundle multiple outputs" pattern as ParseResult.
 */
public class Findings {
    private final Map<String, Integer> activitySummary; // level -> count, in rulebook order
    private final List<LogEntry> flaggedEntries;         // sorted by severity, highest first
    private final List<IpActivity> suspiciousIps;        // sorted by entry count, highest first
    private final List<UnknownPattern> unknownPatterns;  // sorted by line number, ascending

    public Findings(Map<String, Integer> activitySummary,
                    List<LogEntry> flaggedEntries,
                    List<IpActivity> suspiciousIps,
                    List<UnknownPattern> unknownPatterns) {
        this.activitySummary = activitySummary;
        this.flaggedEntries = flaggedEntries;
        this.suspiciousIps = suspiciousIps;
        this.unknownPatterns = unknownPatterns;
    }

    public Map<String, Integer> getActivitySummary() { return activitySummary; }
    public List<LogEntry> getFlaggedEntries() { return flaggedEntries; }
    public List<IpActivity> getSuspiciousIps() { return suspiciousIps; }
    public List<UnknownPattern> getUnknownPatterns() { return unknownPatterns; }
}