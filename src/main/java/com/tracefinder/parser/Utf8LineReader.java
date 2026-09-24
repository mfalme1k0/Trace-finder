package com.tracefinder.parser;

import com.tracefinder.FileLimits;
import com.tracefinder.exception.InvalidUtf8Exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;


class Utf8LineReader implements AutoCloseable {

    private static final int CHUNK_SIZE = 8192;
    private static final int SHORT_FORM_PREVIEW_BYTES = 200;

    static final class Line {
        final int lineNumber;
        final boolean overLong;
        final String content;   // populated when !overLong
        final String shortForm; // populated when overLong

        private Line(int lineNumber, boolean overLong, String content, String shortForm) {
            this.lineNumber = lineNumber;
            this.overLong = overLong;
            this.content = content;
            this.shortForm = shortForm;
        }

        static Line normal(int lineNumber, String content) {
            return new Line(lineNumber, false, content, null);
        }

        static Line overLong(int lineNumber, String shortForm) {
            return new Line(lineNumber, true, null, shortForm);
        }
    }

    private final InputStream in;
    private final byte[] chunk = new byte[CHUNK_SIZE];

    // --- continuous, whole-file UTF-8 validity check state ---
    private final CharsetDecoder validityDecoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
    private final ByteBuffer validityIn = ByteBuffer.allocate(CHUNK_SIZE);
    private final CharBuffer validityOut = CharBuffer.allocate(CHUNK_SIZE);
    private long totalBytesConsumed = 0;
    private boolean validityFlushed = false;

    // --- line-splitting / byte-limit state (current, in-progress line) ---
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream(512);
    private long currentLineByteCount = 0;
    private int currentLineNumber = 1;

    // lines decoded from the most recently read chunk, drained by nextLine()
    private final Deque<Line> pending = new ArrayDeque<>();
    private boolean fileEndReached = false;

    Utf8LineReader(Path path) throws IOException {
        this.in = Files.newInputStream(path);
    }

    /** Returns the next line, or null at end of file. */
    Line nextLine() throws IOException, InvalidUtf8Exception {
        while (pending.isEmpty()) {
            if (fileEndReached) {
                return null;
            }
            readNextChunk();
        }
        return pending.poll();
    }

    private void readNextChunk() throws IOException, InvalidUtf8Exception {
        int bytesRead = in.read(chunk);

        if (bytesRead == -1) {
            validateBytes(chunk, 0, true); // flush the validity decoder at true EOF
            fileEndReached = true;
            if (currentLineByteCount > 0) {
                emitCurrentLine(); // file didn't end with a line terminator
            }
            return;
        }

        validateBytes(chunk, bytesRead, false);
        splitIntoLines(chunk, bytesRead);
        totalBytesConsumed += bytesRead;
    }

    private void validateBytes(byte[] buf, int len, boolean endOfInput) throws InvalidUtf8Exception {
        int offset = 0;

        while (offset < len) {
            int room = Math.min(len - offset, validityIn.remaining());
            validityIn.put(buf, offset, room);
            offset += room;

            validityIn.flip();
            CoderResult result = validityDecoder.decode(validityIn, validityOut, false);
            validityOut.clear();
            validityIn.compact();

            if (result.isError()) {
                throw new InvalidUtf8Exception(totalBytesConsumed + offset);
            }
        }

        if (endOfInput && !validityFlushed) {
            validityIn.flip();
            CoderResult result = validityDecoder.decode(validityIn, validityOut, true);
            validityOut.clear();
            if (!result.isError()) {
                result = validityDecoder.flush(validityOut);
                validityOut.clear();
            }
            validityFlushed = true;
            if (result.isError()) {
                throw new InvalidUtf8Exception(totalBytesConsumed);
            }
        }
    }

    private void splitIntoLines(byte[] buf, int len) {
        for (int i = 0; i < len; i++) {
            byte b = buf[i];
            if (b == '\n') {
                emitCurrentLine();
            } else {
                currentLineByteCount++;
                if (lineBuffer.size() < FileLimits.MAX_LOG_LINE_BYTES) {
                    lineBuffer.write(b);
                }
            }
        }
    }

    private void emitCurrentLine() {
        int lineNumber = currentLineNumber++;
        byte[] captured = lineBuffer.toByteArray();
        lineBuffer.reset();
        long byteCountForThisLine = currentLineByteCount;
        currentLineByteCount = 0;

        if (byteCountForThisLine <= FileLimits.MAX_LOG_LINE_BYTES) {

            String content = stripTrailingCarriageReturn(new String(captured, StandardCharsets.UTF_8));
            pending.add(Line.normal(lineNumber, content));
        } else {
            int previewLength = Math.min(captured.length, SHORT_FORM_PREVIEW_BYTES);

            String preview = new String(captured, 0, previewLength, StandardCharsets.UTF_8);
            String shortForm = preview + "... [truncated, line exceeded "
                    + FileLimits.MAX_LOG_LINE_BYTES + "-byte limit]";
            pending.add(Line.overLong(lineNumber, shortForm));
        }
    }

    private static String stripTrailingCarriageReturn(String s) {
        return s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
