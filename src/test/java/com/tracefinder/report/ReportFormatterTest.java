package com.tracefinder.report;

import com.tracefinder.analysis.Findings;
import com.tracefinder.analysis.IpActivity;
import com.tracefinder.analysis.UnknownPattern;
import com.tracefinder.model.LogEntry;
import com.tracefinder.model.MalformedLine;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportFormatterTest {

    private final ReportFormatter formatter = new ReportFormatter();

    @Test
    void allFiveSectionsAppearInOrderWithContent() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("INFO", 42);
        summary.put("ALERT", 1);

        LogEntry flaggedEntry = new LogEntry(
                LocalDateTime.of(2024, 3, 15, 2, 16, 2), "ALERT", "203.0.113.42",
                "/etc/passwd", "read",
                "2024-03-15 02:16:02 | ALERT | 203.0.113.42  | /etc/passwd   | read", 8);

        Findings findings = new Findings(
                summary,
                List.of(flaggedEntry),
                List.of(new IpActivity("203.0.113.42", 8)),
                List.of(new UnknownPattern(89, "2024-03-15 03:42:11 | CRIT | 203.0.113.42 | /admin | escalated"))
        );

        List<MalformedLine> malformed = List.of(
                new MalformedLine(23, "2024-03-15 02:17:00 | WARN | 203.0.113.42 | /login"));

        String report = formatter.format(findings, malformed,
                LocalDateTime.of(2024, 3, 15, 9, 0, 0), "full file");

        int summaryIdx = report.indexOf("--- Activity Summary ---");
        int flaggedIdx = report.indexOf("--- Flagged Entries ---");
        int ipIdx = report.indexOf("--- Suspicious Activity by IP ---");
        int unknownIdx = report.indexOf("--- Unknown Patterns ---");
        int malformedIdx = report.indexOf("--- Malformed Lines ---");

        assertTrue(summaryIdx < flaggedIdx);
        assertTrue(flaggedIdx < ipIdx);
        assertTrue(ipIdx < unknownIdx);
        assertTrue(unknownIdx < malformedIdx);

        assertTrue(report.contains("=== Log Analysis Report ==="));
        assertTrue(report.contains("Generated: 2024-03-15 09:00:00"));
        assertTrue(report.contains("Time Window: full file"));
        assertTrue(report.contains("INFO: 42"));
        assertTrue(report.contains("ALERT: 1"));
        assertTrue(report.contains(flaggedEntry.getRawLine()));
        assertTrue(report.contains("203.0.113.42: 8 entries"));
        assertTrue(report.contains("Line 89: 2024-03-15 03:42:11 | CRIT | 203.0.113.42 | /admin | escalated"));
        assertTrue(report.contains("Line 23: 2024-03-15 02:17:00 | WARN | 203.0.113.42 | /login"));
    }

    @Test
    void emptySectionsShowNoneFoundPlaceholder() {
        Findings emptyFindings = new Findings(
                new LinkedHashMap<>(), List.of(), List.of(), List.of());

        String report = formatter.format(emptyFindings, List.of(),
                LocalDateTime.of(2024, 3, 15, 9, 0, 0), "full file");

        long noneFoundCount = report.lines().filter(line -> line.equals("None found")).count();
        assertEquals(5, noneFoundCount);
    }

    @Test
    void windowedDescriptionAppearsInHeader() {
        Findings emptyFindings = new Findings(new LinkedHashMap<>(), List.of(), List.of(), List.of());

        String report = formatter.format(emptyFindings, List.of(),
                LocalDateTime.of(2024, 3, 15, 9, 0, 0),
                "2024-03-15 02:00:00 to 2024-03-15 03:30:00");

        assertTrue(report.contains("Time Window: 2024-03-15 02:00:00 to 2024-03-15 03:30:00"));
    }

    @Test
    void timeWindowLineComesDirectlyAfterGenerated() {
        Findings emptyFindings = new Findings(new LinkedHashMap<>(), List.of(), List.of(), List.of());

        String report = formatter.format(emptyFindings, List.of(),
                LocalDateTime.of(2024, 3, 15, 9, 0, 0), "full file");

        List<String> lines = report.lines().toList();
        int generatedIdx = lines.indexOf("Generated: 2024-03-15 09:00:00");
        assertEquals("Time Window: full file", lines.get(generatedIdx + 1));
    }
}