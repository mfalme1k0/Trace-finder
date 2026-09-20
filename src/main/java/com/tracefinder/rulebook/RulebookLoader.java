package com.tracefinder.rulebook;

import com.tracefinder.RulebookException;

import java.io.IOException;
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

            String level = columns[0].trim();

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

            if (score <= 0) {
                throw new NonPositiveSeverityScoreException(lineNumber, score);
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

        try {
            List<String> lines = Files.readAllLines(path);
            return parse(lines);

        } catch (NoSuchFileException e) {
            throw new RulebookNotFoundException(
                    "Rulebook file not found: " + path
            );

        }
    }
}