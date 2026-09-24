package com.tracefinder.parser;

import com.tracefinder.FileLimits;
import com.tracefinder.exception.LogFileException;
import com.tracefinder.exception.LogFileNotARegularFileException;
import com.tracefinder.exception.LogFileNotFoundException;
import com.tracefinder.exception.LogFileTooLargeException;

import java.nio.file.Files;
import java.nio.file.Path;

public class LogFileReader {

    public void checkFileUsable(Path path) throws LogFileException {
        if (!Files.exists(path)) {
            throw new LogFileNotFoundException(path.toString());
        }

        if (!Files.isRegularFile(path)) {
            throw new LogFileNotARegularFileException(path.toString());
        }

        long sizeInBytes;
        try {
            sizeInBytes = Files.size(path);
        } catch (java.io.IOException e) {
            throw new LogFileNotFoundException(path.toString());
        }

        if (sizeInBytes > FileLimits.MAX_LOG_FILE_BYTES) {
            throw new LogFileTooLargeException(path.toString(), sizeInBytes, FileLimits.MAX_LOG_FILE_BYTES);
        }
    }
}
