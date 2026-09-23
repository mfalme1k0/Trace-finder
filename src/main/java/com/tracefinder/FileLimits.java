package com.tracefinder;

public final class FileLimits {

    public static final long MAX_LOG_FILE_BYTES = 10L * 1024 * 1024;       // 10 MB
    public static final long MAX_RULEBOOK_FILE_BYTES = 100L * 1024;        // 100 KB
    public static final int MAX_LOG_LINE_BYTES = 4096;                     // 4 KB, measured in UTF-8 bytes

    private FileLimits() {
        // no instances; this is just a holder for the constants
    }
}
