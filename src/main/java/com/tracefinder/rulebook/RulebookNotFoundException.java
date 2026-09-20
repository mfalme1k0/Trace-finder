package com.tracefinder.rulebook;

import com.tracefinder.RulebookException;

public class RulebookNotFoundException extends RulebookException {

    public RulebookNotFoundException(String message) {
        super(message);
    }
}