package com.tracefinder;

import java.nio.file.Path;

public class ReportWriteException extends FatalErrorException {
    public ReportWriteException(Path reportPath, Throwable cause) {
        super("Could not write report to '" + reportPath + "': " + cause.getMessage());
    }
}
