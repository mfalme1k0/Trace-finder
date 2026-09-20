package com.tracefinder;

/**
 * Represents any of the conditions that must stop the whole tool:
 * bad arguments, an unreadable or invalid rulebook, an unreadable log
 * file, or an unwritable report path. main() catches exactly this one
 * exception type in exactly one place, prints its message, and exits
 * non-zero — so "how do we fail" lives in a single spot instead of being
 * repeated at every call site.
 */
public class FatalErrorException extends Exception {
    public FatalErrorException(String message) {
        super(message);
    }
}