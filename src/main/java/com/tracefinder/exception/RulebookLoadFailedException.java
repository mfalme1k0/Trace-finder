package com.tracefinder.exception;

import java.nio.file.Path;

public class RulebookLoadFailedException extends FatalErrorException {
    public RulebookLoadFailedException(Path rulebookPath, Throwable cause) {
        super("Could not load rulebook '" + rulebookPath + "': " + cause.getMessage());
    }
}
