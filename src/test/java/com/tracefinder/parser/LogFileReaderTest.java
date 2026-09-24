package com.tracefinder.parser;

import com.tracefinder.FileLimits;
import com.tracefinder.exception.LogFileNotARegularFileException;
import com.tracefinder.exception.LogFileNotFoundException;
import com.tracefinder.exception.LogFileTooLargeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LogFileReaderTest {

    private final LogFileReader reader = new LogFileReader();

    @TempDir
    Path tempDir;

    @Test
    void missingFileThrowsNotFound() {
        Path missing = tempDir.resolve("does-not-exist.log");
        assertThrows(LogFileNotFoundException.class, () -> reader.checkFileUsable(missing));
    }

    @Test
    void directoryPathThrowsNotARegularFile() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("a-directory"));
        assertThrows(LogFileNotARegularFileException.class, () -> reader.checkFileUsable(dir));
    }

    @Test
    void fileOneByteOverLimitThrowsTooLarge() throws IOException {
        Path path = tempDir.resolve("too-big.log");
        writeFileOfExactSize(path, FileLimits.MAX_LOG_FILE_BYTES + 1);

        assertThrows(LogFileTooLargeException.class, () -> reader.checkFileUsable(path));
    }

    @Test
    void fileExactlyAtLimitIsNotRejectedForSize() throws IOException {
        Path path = tempDir.resolve("exactly-at-limit.log");
        writeFileOfExactSize(path, FileLimits.MAX_LOG_FILE_BYTES);

        assertDoesNotThrow(() -> reader.checkFileUsable(path));
    }

    @Test
    void normalFileDoesNotThrow() throws IOException {
        Path path = tempDir.resolve("normal.log");
        Files.writeString(path, "line one\nline two\n");

        assertDoesNotThrow(() -> reader.checkFileUsable(path));
    }

    private void writeFileOfExactSize(Path path, long targetBytes) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            raf.setLength(targetBytes);
        }
    }
}
