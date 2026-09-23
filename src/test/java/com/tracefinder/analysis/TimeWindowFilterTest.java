package com.tracefinder.analysis;

import com.tracefinder.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeWindowFilterTest {

    private final TimeWindowFilter filter = new TimeWindowFilter();

    private LogEntry entryAt(LocalDateTime timestamp, int lineNumber) {
        return new LogEntry(timestamp, "INFO", "1.1.1.1", "/x", "y", "raw", lineNumber);
    }

    @Test
    void nullWindowReturnsEveryEntryUnfiltered() {
        List<LogEntry> entries = List.of(
                entryAt(LocalDateTime.of(2024, 1, 1, 0, 0), 1),
                entryAt(LocalDateTime.of(2099, 1, 1, 0, 0), 2)
        );

        List<LogEntry> result = filter.filter(entries, null);

        assertEquals(2, result.size());
    }

    @Test
    void entriesOutsideWindowAreExcluded() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 15, 2, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 15, 3, 0, 0);
        TimeWindow window = new TimeWindow(start, end);

        List<LogEntry> entries = List.of(
                entryAt(start.minusMinutes(1), 1),  // before window
                entryAt(start.plusMinutes(1), 2),   // inside window
                entryAt(end, 3)                     // exactly at end, excluded
        );

        List<LogEntry> result = filter.filter(entries, window);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getLineNumber());
    }

    @Test
    void windowWithNoMatchingEntriesReturnsEmptyList() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 15, 2, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 15, 3, 0, 0);
        TimeWindow window = new TimeWindow(start, end);

        List<LogEntry> entries = List.of(entryAt(start.minusDays(1), 1));

        assertTrue(filter.filter(entries, window).isEmpty());
    }
}