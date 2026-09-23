package com.tracefinder.analysis;

import com.tracefinder.model.LogEntry;

import java.util.ArrayList;
import java.util.List;


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