package com.tracefinder.rulebook;

import com.tracefinder.exception.*;
import com.tracefinder.FileLimits;
import com.tracefinder.FieldCleaner;
import com.tracefinder.exception.RulebookInvalidUtf8Exception;
import com.tracefinder.exception.RulebookNotFoundException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RulebookLoader {

    private static final String EXPECTED_HEADER = "level,severity_score";
    private static final int EXPECTED_COLUMN_COUNT = 2;


    public Map<String, Integer> parse(List<String> lines) throws RulebookFormatException {
        if (lines.isEmpty()) {
            throw new RulebookIsEmptyException();
        }

        String header = lines.get(0).trim();
        if (!header.equals(EXPECTED_HEADER)) {
            throw new InvalidRulebookHeaderException(EXPECTED_HEADER, header);
        }

        Map<String, Integer> scoresByLevel = new LinkedHashMap<>();

        for (int i = 1; i < lines.size(); i++) {
            int lineNumber = i + 1; // 1-indexed, matches the file
            String line = lines.get(i);

            if (line.isBlank()) {
                continue; // tolerate trailing blank lines at end of file
            }

            String[] columns = line.split(",", -1);
            if (columns.length != EXPECTED_COLUMN_COUNT) {
                throw new InvalidColumnCountException(lineNumber, EXPECTED_COLUMN_COUNT, line);
            }

            String level = FieldCleaner.clean(columns[0]);

            if (level.isEmpty()) {
                throw new BlankRulebookLevelException(lineNumber, line);
            }


            String scoreText = columns[1].trim();
            int score;
            try {
                score = Integer.parseInt(scoreText);
            } catch (NumberFormatException e) {
                throw new NonNumericSeverityScoreException(lineNumber, scoreText);
            }

            if (score < 0) {
                throw new NegativeSeverityScoreException(lineNumber, score);
            }

            if (scoresByLevel.containsKey(level)) {
                throw new DuplicateRulebookLevelException(lineNumber, level);
            }


            scoresByLevel.put(level, score);
        }

        return scoresByLevel;
    }


    public Map<String, Integer> loadFromFile(Path path)
            throws IOException, RulebookException {

        if (!Files.exists(path)) {
            throw new RulebookNotFoundException("Rulebook file not found: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new RulebookNotARegularFileException(path.toString());
        }

        long sizeInBytes = Files.size(path);
        if (sizeInBytes > FileLimits.MAX_RULEBOOK_FILE_BYTES) {
            throw new RulebookTooLargeException(path.toString(), sizeInBytes, FileLimits.MAX_RULEBOOK_FILE_BYTES);
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (NoSuchFileException e) {
            throw new RulebookNotFoundException(
                    "Rulebook file not found: " + path
            );
        }

        String content = decodeStrictUtf8(bytes, path);
        List<String> lines = content.lines().toList();

        return parse(lines);
    }

    private String decodeStrictUtf8(byte[] bytes, Path path) throws RulebookInvalidUtf8Exception {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new RulebookInvalidUtf8Exception(path.toString());
        }
    }
}