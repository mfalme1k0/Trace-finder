package com.tracefinder.rulebook;

import org.junit.jupiter.api.Test;

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

        assertThrows(RulebookFormatException.class, () -> loader.parse(lines));
    }

    @Test
    void rowWithWrongColumnCountThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,1,extra"
        );

        assertThrows(RulebookFormatException.class, () -> loader.parse(lines));
    }

    @Test
    void nonNumericScoreThrows() {
        List<String> lines = List.of(
                "level,severity_score",
                "INFO,high" // not a number
        );

        assertThrows(RulebookFormatException.class, () -> loader.parse(lines));
    }

    @Test
    void emptyFileThrows() {
        assertThrows(RulebookFormatException.class, () -> loader.parse(List.of()));
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
}