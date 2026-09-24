package com.tracefinder.exception;

public abstract class RulebookException extends Exception {
    protected RulebookException(String message) {
        super(message);
    }
}