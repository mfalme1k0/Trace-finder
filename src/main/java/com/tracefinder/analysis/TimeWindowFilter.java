package com.tracefinder.analysis;

import com.tracefinder.model.LogEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters entries by an optional time window. A null window means "no
 * filtering" (the whole-file case) — kept as the one place in the code
 * that has to think about "window or no window", so TimeWindow itself
 * never has to represent an absent state.
 */
public class TimeWindowFilter {

    public List<LogEntry> filter(List<LogEntry> entries, TimeWindow window) {
        if (window == null) {
            return entries;
        }

        List<LogEntry> filtered = new ArrayList<>();
        for (LogEntry entry : entries) {
            if (window.contains(entry.getTimestamp())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }
}