package com.tracefinder.exception;

import com.tracefinder.exception.RulebookException;

public class RulebookInvalidUtf8Exception extends RulebookException {
    public RulebookInvalidUtf8Exception(String path) {
        super("Rulebook '" + path + "' contains invalid UTF-8");
    }
}