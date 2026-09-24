package com.tracefinder.parser;

import com.tracefinder.FileLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Utf8LineReaderTest {

    @TempDir
    Path tempDir;

    // --- basic correctness ---

    @Test
    void shortLinesRoundTripExactly() throws IOException, InvalidUtf8Exception {
        Path path = writeBytes("first line\nsecond line\n");

        List<Utf8LineReader.Line> lines = readAll(path);

        assertEquals(2, lines.size());
        assertFalse(lines.get(0).overLong);
        assertEquals("first line", lines.get(0).content);
        assertEquals(1, lines.get(0).lineNumber);
        assertFalse(lines.get(1).overLong);
        assertEquals("second line", lines.get(1).content);
        assertEquals(2, lines.get(1).lineNumber);
    }

    @Test
    void fileNotEndingInNewlineStillCapturesFinalLine() throws IOException, InvalidUtf8Exception {
        Path path = writeBytes("only line, no trailing newline");

        List<Utf8LineReader.Line> lines = readAll(path);

        assertEquals(1, lines.size());
        assertEquals("only line, no trailing newline", lines.get(0).content);
    }

    @Test
    void emptyFileProducesNoLines() throws IOException, InvalidUtf8Exception {
        Path path = writeBytes("");
        assertTrue(readAll(path).isEmpty());
    }

    @Test
    void multiByteCharacterInShortLineDecodesExactly() throws IOException, InvalidUtf8Exception {
        String content = "caf\u00e9 \uD83D\uDE00 done"; // "café 😀 done"
        Path path = writeBytes(content + "\n");

        List<Utf8LineReader.Line> lines = readAll(path);

        assertEquals(1, lines.size());
        assertFalse(lines.get(0).overLong);
        assertEquals(content, lines.get(0).content);
    }

    // --- length-limit boundary ---

    @Test
    void lineExactlyAtLimitIsNotOverLong() throws IOException, InvalidUtf8Exception {
        String line = "a".repeat((int) FileLimits.MAX_LOG_LINE_BYTES);
        Path path = writeBytes(line + "\n");

        List<Utf8LineReader.Line> lines = readAll(path);

        assertEquals(1, lines.size());
        assertFalse(lines.get(0).overLong, "a line exactly at the byte limit must be accepted");
        assertEquals(line, lines.get(0).content);
    }

    @Test
    void lineOneByteOverLimitIsOverLong() throws IOException, InvalidUtf8Exception {
        String line = "a".repeat((int) FileLimits.MAX_LOG_LINE_BYTES + 1);
        Path path = writeBytes(line + "\n");

        List<Utf8LineReader.Line> lines = readAll(path);

        assertEquals(1, lines.size());
        assertTrue(lines.get(0).overLong);
        assertNotNull(lines.get(0).shortForm);
        assertTrue(lines.get(0).shortForm.contains("truncated"));
    }

    @Test
    void overLongLineDoesNotStopSubsequentLinesFromBeingRead() throws IOException, InvalidUtf8Exception {
        String overLong = "a".repeat((int) FileLimits.MAX_LOG_LINE_BYTES + 500);
        Path path = writeBytes(overLong + "\nnormal line after\n");

        List<Utf8LineReader.Line> lines = readAll(path);

        assertEquals(2, lines.size());
        assertTrue(lines.get(0).overLong);
        assertFalse(lines.get(1).overLong);
        assertEquals("normal line after", lines.get(1).content);
        assertEquals(2, lines.get(1).lineNumber);
    }

    // --- invalid UTF-8 ---

    @Test
    void invalidUtf8InNormalLineThrows() throws IOException {
        // 'A', a lone continuation byte (invalid on its own), 'B'
        byte[] bytes = {0x41, (byte) 0x80, 0x42, '\n'};
        Path path = tempDir.resolve("invalid.log");
        Files.write(path, bytes);

        assertThrows(InvalidUtf8Exception.class, () -> readAll(path));
    }

    @Test
    void invalidUtf8HiddenInDiscardedTailOfOverLongLineStillThrows() throws IOException {
        // This is the case the design exists to catch: the invalid byte
        // sits well past the byte-limit capture point, in the part of
        // the line that never gets buffered - only the separate,
        // continuous whole-file validity pass sees it.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < FileLimits.MAX_LOG_LINE_BYTES + 100; i++) {
            out.write('a');
        }
        out.write(0x80); // invalid: lone continuation byte, past the capture limit
        out.write('a');
        out.write('\n');
        Path path = tempDir.resolve("invalid-tail.log");
        Files.write(path, out.toByteArray());

        assertThrows(InvalidUtf8Exception.class, () -> readAll(path));
    }

    @Test
    void truncatedMultiByteSequenceAtEndOfFileThrows() throws IOException {
        // A valid lead byte for a 2-byte sequence (0xC3) with nothing
        // after it - incomplete at true EOF, which is itself invalid.
        byte[] bytes = {0x41, (byte) 0xC3};
        Path path = tempDir.resolve("truncated.log");
        Files.write(path, bytes);

        assertThrows(InvalidUtf8Exception.class, () -> readAll(path));
    }

    // --- chunk-boundary robustness ---
    // The reader's internal chunk size is 8192 bytes. A multi-byte
    // character positioned so its bytes straddle that boundary is the
    // scenario most likely to expose a bug in the incremental decode
    // carry-over logic.

    @Test
    void multiByteCharacterStraddlingChunkBoundaryDoesNotThrow() throws IOException {
        String content = "a".repeat(8190) + "\uD83D\uDE00" + "a".repeat(50) + "\n";
        Path path = writeBytes(content);

        // This line is well over the length limit (that's not what's
        // under test here) - the point is purely that decoding across
        // the chunk boundary must not be mistaken for invalid UTF-8.
        assertDoesNotThrow(() -> readAll(path));
    }

    // --- helpers ---

    private Path writeBytes(String content) throws IOException {
        Path path = tempDir.resolve("test-" + System.nanoTime() + ".log");
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private List<Utf8LineReader.Line> readAll(Path path) throws IOException, InvalidUtf8Exception {
        List<Utf8LineReader.Line> lines = new ArrayList<>();
        try (Utf8LineReader reader = new Utf8LineReader(path)) {
            Utf8LineReader.Line line;
            while ((line = reader.nextLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
