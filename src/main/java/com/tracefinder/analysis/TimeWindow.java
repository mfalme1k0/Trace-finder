package com.tracefinder.analysis;

import com.tracefinder.TimestampFormats;

import java.time.LocalDateTime;


public class TimeWindow {

    private final LocalDateTime start;
    private final LocalDateTime end;

    public TimeWindow(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            // Should already have been rejected by Main; this just stops
            // an invalid TimeWindow from silently existing if that ever changes.
            throw new IllegalArgumentException("start must not be after end");
        }
        this.start = start;
        this.end = end;
    }

    public boolean contains(LocalDateTime timestamp) {
        boolean atOrAfterStart = !timestamp.isBefore(start);
        boolean beforeEnd = timestamp.isBefore(end);
        return atOrAfterStart && beforeEnd;
    }

    public String describe() {
        return start.format(TimestampFormats.LOG_TIMESTAMP_FORMAT)
                + " to " + end.format(TimestampFormats.LOG_TIMESTAMP_FORMAT);
    }
}