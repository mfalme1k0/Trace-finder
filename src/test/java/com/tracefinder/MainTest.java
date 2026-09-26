package com.tracefinder;

import com.tracefinder.exception.FatalErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class MainTest {

    @TempDir
    Path tempDir;

    @Test
    void oversizedLogFileIsRefusedBeforeReading_notLoadedIntoMemory() throws IOException {
        Path logPath = tempDir.resolve("oversized.log");
        writeFileOfExactSize(logPath, FileLimits.MAX_LOG_FILE_BYTES + 1);
        Path rulebookPath = writeMinimalRulebook();
        Path reportPath = tempDir.resolve("report.txt");

        FatalErrorException ex = assertThrows(FatalErrorException.class, () -> Main.run(new String[]{
                logPath.toString(), rulebookPath.toString(), reportPath.toString()
        }));

        assertTrue(
                ex.getMessage().contains("exceeding the " + FileLimits.MAX_LOG_FILE_BYTES + "-byte limit"),
                "Expected the documented size-limit message, got: " + ex.getMessage()
        );
        assertFalse(Files.exists(reportPath), "No report should be written on a fatal refusal");
    }

    @Test
    void logFileWithInvalidUtf8ByteIsRefusedWithTheDocumentedMessage() throws IOException {
        Path logPath = tempDir.resolve("bad-utf8.log");
        // A valid line followed by a lone 0x80 continuation byte, which is not
        // valid UTF-8 anywhere in a well-formed byte stream.
        Files.write(logPath, concat(
                "2024-03-15 12:00:00 | INFO | 10.0.0.1 | / | ok\n".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                new byte[]{(byte) 0x80},
                "\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        ));
        Path rulebookPath = writeMinimalRulebook();
        Path reportPath = tempDir.resolve("report.txt");

        FatalErrorException ex = assertThrows(FatalErrorException.class, () -> Main.run(new String[]{
                logPath.toString(), rulebookPath.toString(), reportPath.toString()
        }));

        assertTrue(
                ex.getMessage().contains("invalid UTF-8"),
                "Expected an invalid-UTF-8 message, got: " + ex.getMessage()
        );
        assertFalse(Files.exists(reportPath));
    }

    @Test
    void anOverLongLineIsTruncatedNotFatal_andRunCompletesEndToEnd() throws Exception {
        String longLine = "2024-03-15 12:00:00 | WARN | 10.0.0.1 | / | " + "a".repeat(5000);
        Path logPath = tempDir.resolve("logs.txt");
        Files.writeString(logPath,
                "2024-03-15 02:14:08 | INFO | 10.0.0.1 | /login | success\n"
                        + longLine + "\n"
                        + "2024-03-15 02:14:33 | ALERT | 203.0.113.42 | /etc/passwd | read\n");
        Path rulebookPath = writeMinimalRulebook();
        Path reportPath = tempDir.resolve("report.txt");

        assertDoesNotThrow(() -> Main.run(new String[]{
                logPath.toString(), rulebookPath.toString(), reportPath.toString()
        }));

        String report = Files.readString(reportPath);
        assertTrue(report.contains("[truncated, line exceeded " + FileLimits.MAX_LOG_LINE_BYTES + "-byte limit]"));
        assertTrue(report.contains("ALERT | 203.0.113.42 | /etc/passwd | read"));
        assertFalse(report.contains("a".repeat(5000)), "The full over-long line must never be held or printed in full");
    }

    @Test
    void validRunEndToEndWritesExpectedReport() throws Exception {
        Path logPath = tempDir.resolve("logs.txt");
        Files.writeString(logPath,
                "2024-03-15 02:14:08 | INFO | 10.0.0.1 | /login | success\n"
                        + "2024-03-15 02:14:33 | ALERT | 203.0.113.42 | /etc/passwd | read\n");
        Path rulebookPath = writeMinimalRulebook();
        Path reportPath = tempDir.resolve("report.txt");

        assertDoesNotThrow(() -> Main.run(new String[]{
                logPath.toString(), rulebookPath.toString(), reportPath.toString()
        }));

        String report = Files.readString(reportPath);
        assertTrue(report.contains("INFO: 1"));
        assertTrue(report.contains("ALERT: 1"));
        assertTrue(report.contains("ALERT | 203.0.113.42 | /etc/passwd | read"));
    }

    private Path writeMinimalRulebook() throws IOException {
        Path path = tempDir.resolve("rules.csv");
        Files.writeString(path, "level,severity_score\nINFO,1\nWARN,3\nERROR,5\nALERT,9\n");
        return path;
    }

    private void writeFileOfExactSize(Path path, long targetBytes) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            raf.setLength(targetBytes);
        }
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) total += p.length;
        byte[] out = new byte[total];
        int offset = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, offset, p.length);
            offset += p.length;
        }
        return out;
    }
}
