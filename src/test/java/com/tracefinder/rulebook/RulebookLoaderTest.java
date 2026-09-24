package com.tracefinder.rulebook;

import com.tracefinder.FileLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.tracefinder.exception.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RulebookLoaderTest {

    private final RulebookLoader loader = new RulebookLoader();

    @Test
    void validCsvParsesIntoLevelToScoreMap() throws RulebookFormatException {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,1",
                "WARN,3",
                "ERROR,5",
                "ALERT,9"
        );

        Map<String, Integer> scores = loader.parse(lines);

        assertEquals(4, scores.size());
        assertEquals(1, scores.get("INFO"));
        assertEquals(3, scores.get("WARN"));
        assertEquals(5, scores.get("ERROR"));
        assertEquals(9, scores.get("ALERT"));
    }

    @Test
    void wrongHeaderThrows() {
        List<String> lines = List.of(
                "severity,level", // columns swapped/renamed
                "INFO,1"
        );

        assertThrows(InvalidRulebookHeaderException.class, () -> loader.parse(lines));
    }

    @Test
    void rowWithWrongColumnCountThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,1,extra"
        );

        assertThrows(InvalidColumnCountException.class, () -> loader.parse(lines));
    }

    @Test
    void nonNumericScoreThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,high" // not a number
        );

        assertThrows(NonNumericSeverityScoreException.class, () -> loader.parse(lines));
    }

    @Test
    void emptyFileThrows() {
        assertThrows(RulebookIsEmptyException.class, () -> loader.parse(List.of()));
    }

    @Test
    void blankLevelThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                ",5"
        );

        assertThrows(BlankRulebookLevelException.class, () -> loader.parse(lines));
    }

    @Test
    void zeroScoreIsValid() throws RulebookFormatException {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,0"
        );

        Map<String, Integer> scores = loader.parse(lines);

        assertEquals(0, scores.get("INFO"));
    }

    @Test
    void negativeScoreThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,-1"
        );

        assertThrows(NegativeSeverityScoreException.class, () -> loader.parse(lines));
    }

    @Test
    void duplicateLevelThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                "WARN,3",
                "WARN,7"
        );

        assertThrows(DuplicateRulebookLevelException.class, () -> loader.parse(lines));
    }

    @Test
    void levelsEqualOnlyAfterCleaningCountAsDuplicates() {
        // Second "WARN" has a zero-width space between the R and N -
        // invisible, but must still be caught as the same level.
        List<String> lines = List.of(
                "level,severity_score",
                "WARN,3",
                "WA\u200BRN,7"
        );

        assertThrows(DuplicateRulebookLevelException.class, () -> loader.parse(lines));
    }

    @Test
    void levelThatIsOnlyHiddenCharactersIsBlank() {
        List<String> lines = List.of(
                "level,severity_score",
                "\u200B\u200B,3" // looks like content, cleans to empty
        );

        assertThrows(BlankRulebookLevelException.class, () -> loader.parse(lines));
    }

    @Test
    void levelIsClearedOfSurroundingWhitespaceAndHiddenCharacters() throws RulebookFormatException {
        List<String> lines = List.of(
                "level,severity_score",
                "  \u200BWARN\u200B  ,3"
        );

        Map<String, Integer> scores = loader.parse(lines);

        assertEquals(1, scores.size());
        assertEquals(3, scores.get("WARN"));
    }

    @Test
    void trailingBlankLineIsTolerated() throws RulebookFormatException {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,1",
                ""
        );

        Map<String, Integer> scores = loader.parse(lines);

        assertEquals(1, scores.size());
    }

    // --- loadFromFile: existence / regular-file / size checks ---

    @TempDir
    Path tempDir;

    @Test
    void missingFileThrowsNotFound() {
        Path missing = tempDir.resolve("does-not-exist.csv");
        assertThrows(RulebookNotFoundException.class, () -> loader.loadFromFile(missing));
    }

    @Test
    void directoryPathThrowsNotARegularFile() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("a-directory"));
        assertThrows(RulebookNotARegularFileException.class, () -> loader.loadFromFile(dir));
    }

    @Test
    void fileOneByteOverLimitThrowsTooLarge() throws IOException {
        Path path = tempDir.resolve("too-big.csv");
        writeFileOfExactSize(path, FileLimits.MAX_RULEBOOK_FILE_BYTES + 1);

        assertThrows(RulebookTooLargeException.class, () -> loader.loadFromFile(path));
    }

    @Test
    void fileExactlyAtLimitIsNotRejectedForSize() throws IOException {
        Path path = tempDir.resolve("exactly-at-limit.csv");
        writeValidRulebookOfExactSize(path, FileLimits.MAX_RULEBOOK_FILE_BYTES);

        // Should pass the size gate - and since the padded content is a
        // real, valid rulebook, it should load successfully too.
        assertDoesNotThrow(() -> loader.loadFromFile(path));
    }

    @Test
    void invalidUtf8ThrowsRulebookInvalidUtf8() throws IOException {
        Path path = tempDir.resolve("invalid-utf8.csv");
        byte[] prefixBytes = "level,severity_score\nINFO,1\nWARN"
                .getBytes(StandardCharsets.UTF_8);
        byte[] fileBytes = new byte[prefixBytes.length + 1];
        System.arraycopy(prefixBytes, 0, fileBytes, 0, prefixBytes.length);
        fileBytes[prefixBytes.length] = (byte) 0x80; // invalid: lone continuation byte
        Files.write(path, fileBytes);

        assertThrows(RulebookInvalidUtf8Exception.class, () -> loader.loadFromFile(path));
    }

    /**
     * Writes a file of exactly targetBytes, content irrelevant, purely to
     * exercise the size-limit gate (which runs before content is parsed).
     */
    private void writeFileOfExactSize(Path path, long targetBytes) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            raf.setLength(targetBytes);
        }
    }

    /**
     * Builds a real, parseable rulebook ("level,severity_score\nINFO,1\n")
     * then pads it to exactly targetBytes with a trailing line of spaces.
     * A space-only line is blank, so RulebookLoader.parse() skips it - the
     * padding affects only file size, not the parsed content.
     */
    private void writeValidRulebookOfExactSize(Path path, long targetBytes) throws IOException {
        String base = "level,severity_score\nINFO,1\n";
        int baseBytes = base.getBytes(StandardCharsets.UTF_8).length;
        long paddingSpaces = targetBytes - baseBytes;
        if (paddingSpaces < 0) {
            throw new IllegalArgumentException("targetBytes too small to hold a valid rulebook");
        }
        String content = base + " ".repeat((int) paddingSpaces);
        Files.writeString(path, content);
    }
}