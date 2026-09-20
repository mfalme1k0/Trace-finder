package com.tracefinder;

import java.time.format.DateTimeFormatter;

/**
 * The single timestamp format used everywhere in TraceFinder: log entries,
 * the --window arguments, and the report's "Generated" line. Pulled into
 * one place so the pattern string only ever needs to change in one spot.
 */
public final class TimestampFormats {

    public static final DateTimeFormatter LOG_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimestampFormats() {
        // no instances; this is just a holder for the constant
    }
}