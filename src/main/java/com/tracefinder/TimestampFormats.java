package com.tracefinder;

import java.time.format.DateTimeFormatter;


public final class TimestampFormats {

    public static final DateTimeFormatter LOG_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimestampFormats() {
        // no instances; this is just a holder for the constant
    }
}