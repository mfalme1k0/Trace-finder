package com.tracefinder.report;

import com.tracefinder.analysis.Findings;
import com.tracefinder.analysis.IpActivity;
import com.tracefinder.analysis.UnknownPattern;
import com.tracefinder.model.LogEntry;
import com.tracefinder.TimestampFormats;
import com.tracefinder.model.MalformedLine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class ReportFormatter {

    private static final String NONE_FOUND = "None found";

    public String format(Findings findings, List<MalformedLine> malformedLines,
                         LocalDateTime generatedAt, String timeWindowDescription) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Log Analysis Report ===\n");
        sb.append("Generated: ").append(generatedAt.format(TimestampFormats.LOG_TIMESTAMP_FORMAT)).append("\n");
        sb.append("Time Window: ").append(timeWindowDescription).append("\n\n");

        appendActivitySummary(sb, findings.getActivitySummary());
        appendFlaggedEntries(sb, findings.getFlaggedEntries());
        appendSuspiciousIps(sb, findings.getSuspiciousIps());
        appendUnknownPatterns(sb, findings.getUnknownPatterns());
        appendMalformedLines(sb, malformedLines);

        return sb.toString();
    }

    private void appendActivitySummary(StringBuilder sb, Map<String, Integer> activitySummary) {
        sb.append("--- Activity Summary ---\n");
        if (activitySummary.isEmpty()) {
            sb.append(NONE_FOUND).append("\n");
        } else {
            for (Map.Entry<String, Integer> e : activitySummary.entrySet()) {
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        sb.append("\n");
    }

    private void appendFlaggedEntries(StringBuilder sb, List<LogEntry> flaggedEntries) {
        sb.append("--- Flagged Entries ---\n");
        if (flaggedEntries.isEmpty()) {
            sb.append(NONE_FOUND).append("\n");
        } else {
            for (LogEntry entry : flaggedEntries) {
                sb.append(entry.getRawLine()).append("\n");
            }
        }
        sb.append("\n");
    }

    private void appendSuspiciousIps(StringBuilder sb, List<IpActivity> suspiciousIps) {
        sb.append("--- Suspicious Activity by IP ---\n");
        if (suspiciousIps.isEmpty()) {
            sb.append(NONE_FOUND).append("\n");
        } else {
            for (IpActivity ip : suspiciousIps) {
                sb.append(ip.getIp()).append(": ").append(ip.getEntryCount()).append(" entries\n");
            }
        }
        sb.append("\n");
    }

    private void appendUnknownPatterns(StringBuilder sb, List<UnknownPattern> unknownPatterns) {
        sb.append("--- Unknown Patterns ---\n");
        if (unknownPatterns.isEmpty()) {
            sb.append(NONE_FOUND).append("\n");
        } else {
            for (UnknownPattern pattern : unknownPatterns) {
                sb.append("Line ").append(pattern.getLineNumber())
                        .append(": ").append(pattern.getRawLine()).append("\n");
            }
        }
        sb.append("\n");
    }

    private void appendMalformedLines(StringBuilder sb, List<MalformedLine> malformedLines) {
        sb.append("--- Malformed Lines ---\n");
        if (malformedLines.isEmpty()) {
            sb.append(NONE_FOUND).append("\n");
        } else {
            for (MalformedLine line : malformedLines) {
                sb.append("Line ").append(line.getLineNumber())
                        .append(": ").append(line.getRawContent()).append("\n");
            }
        }
    }
}