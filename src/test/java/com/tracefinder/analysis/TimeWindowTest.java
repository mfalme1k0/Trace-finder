package com.tracefinder.analysis;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeWindowTest {

    private final LocalDateTime start = LocalDateTime.of(2024, 3, 15, 2, 0, 0);
    private final LocalDateTime end = LocalDateTime.of(2024, 3, 15, 3, 30, 0);

    @Test
    void timestampExactlyAtStartIsInside() {
        TimeWindow window = new TimeWindow(start, end);
        assertTrue(window.contains(start));
    }

    @Test
    void timestampExactlyAtEndIsNotInside() {
        TimeWindow window = new TimeWindow(start, end);
        assertFalse(window.contains(end));
    }

    @Test
    void timestampJustBeforeEndIsInside() {
        TimeWindow window = new TimeWindow(start, end);
        assertTrue(window.contains(end.minusSeconds(1)));
    }

    @Test
    void timestampJustBeforeStartIsNotInside() {
        TimeWindow window = new TimeWindow(start, end);
        assertFalse(window.contains(start.minusSeconds(1)));
    }

    @Test
    void equalStartAndEndHoldsNoTimestamps() {
        TimeWindow window = new TimeWindow(start, start); // start == end

        // Not even the boundary timestamp itself is inside: it's
        // start-inclusive but also end-exclusive, and here start IS end.
        assertFalse(window.contains(start));
    }

    @Test
    void reversedWindowIsRejectedByConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new TimeWindow(end, start));
    }

    @Test
    void describeFormatsBothTimestamps() {
        TimeWindow window = new TimeWindow(start, end);
        assertEquals("2024-03-15 02:00:00 to 2024-03-15 03:30:00", window.describe());
    }
}