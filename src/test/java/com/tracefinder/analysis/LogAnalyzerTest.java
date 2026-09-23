package com.tracefinder.analysis;

import com.tracefinder.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalyzerTest {

    private final LogAnalyzer analyzer = new LogAnalyzer();

    // Small helper so tests don't repeat the same seven-argument constructor.
    // Builds a plausible raw line so tests reading getRawLine() get something sane.
    private LogEntry entry(String level, String ip, int lineNumber) {
        String raw = "2024-03-15 02:00:00 | " + level + " | " + ip + " | /somewhere | action";
        return new LogEntry(LocalDateTime.of(2024, 3, 15, 2, 0, 0), level, ip,
                "/somewhere", "action", raw, lineNumber);
    }

    @Test
    void knownLevelIsCountedInActivitySummary() {
        Map<String, Integer> rulebook = Map.of("INFO", 1, "WARN", 3);
        List<LogEntry> entries = List.of(entry("INFO", "1.1.1.1", 1));

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals(1, findings.getActivitySummary().get("INFO"));
    }

    @Test
    void levelWithNoEntriesStillAppearsAtZero() {
        Map<String, Integer> rulebook = Map.of("INFO", 1, "ALERT", 9);
        List<LogEntry> entries = List.of(entry("INFO", "1.1.1.1", 1));

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals(0, findings.getActivitySummary().get("ALERT"));
    }

    @Test
    void differentlyCasedLevelIsUnknownNotAMatch() {
        Map<String, Integer> rulebook = Map.of("INFO", 1);
        List<LogEntry> entries = List.of(entry("info", "1.1.1.1", 5));

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals(1, findings.getUnknownPatterns().size());
        assertEquals(5, findings.getUnknownPatterns().get(0).getLineNumber());
        // "info" never touched the summary, since it's not a match
        assertFalse(findings.getActivitySummary().containsKey("info"));
    }

    @Test
    void entryBelowThresholdIsNotFlagged() {
        Map<String, Integer> rulebook = Map.of("INFO", 2);
        List<LogEntry> entries = List.of(entry("INFO", "1.1.1.1", 1));

        Findings findings = analyzer.analyze(entries, rulebook);

        assertTrue(findings.getFlaggedEntries().isEmpty());
        assertTrue(findings.getSuspiciousIps().isEmpty());
    }

    @Test
    void entryAtThresholdIsFlagged() {
        Map<String, Integer> rulebook = Map.of("WARN", 3);
        List<LogEntry> entries = List.of(entry("WARN", "1.1.1.1", 1));

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals(1, findings.getFlaggedEntries().size());
        assertEquals(1, findings.getSuspiciousIps().size());
    }

    @Test
    void suspiciousIpTotalIncludesUnknownAndUnflaggedEntriesForThatIp() {
        Map<String, Integer> rulebook = Map.of("INFO", 1, "ALERT", 9);
        List<LogEntry> entries = List.of(
                entry("ALERT", "9.9.9.9", 1),   // flagged -> makes 9.9.9.9 suspicious
                entry("INFO", "9.9.9.9", 2),    // same IP, low severity
                entry("CRIT", "9.9.9.9", 3)     // same IP, unknown level
        );

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals(1, findings.getSuspiciousIps().size());
        assertEquals("9.9.9.9", findings.getSuspiciousIps().get(0).getIp());
        // all three entries for this IP count toward its total, not just the flagged one
        assertEquals(3, findings.getSuspiciousIps().get(0).getEntryCount());
    }

    @Test
    void flaggedEntriesAreSortedBySeverityHighestFirst() {
        Map<String, Integer> rulebook = Map.of("WARN", 3, "ALERT", 9);
        List<LogEntry> entries = List.of(
                entry("WARN", "1.1.1.1", 1),
                entry("ALERT", "2.2.2.2", 2)
        );

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals("ALERT", findings.getFlaggedEntries().get(0).getLevel());
        assertEquals("WARN", findings.getFlaggedEntries().get(1).getLevel());
    }

    @Test
    void suspiciousIpsAreSortedByCountHighestFirst() {
        Map<String, Integer> rulebook = Map.of("ALERT", 9);
        List<LogEntry> entries = List.of(
                entry("ALERT", "1.1.1.1", 1),
                entry("ALERT", "2.2.2.2", 2),
                entry("ALERT", "2.2.2.2", 3) // 2.2.2.2 has 2 entries, 1.1.1.1 has 1
        );

        Findings findings = analyzer.analyze(entries, rulebook);

        assertEquals("2.2.2.2", findings.getSuspiciousIps().get(0).getIp());
        assertEquals(2, findings.getSuspiciousIps().get(0).getEntryCount());
        assertEquals("1.1.1.1", findings.getSuspiciousIps().get(1).getIp());
    }
}