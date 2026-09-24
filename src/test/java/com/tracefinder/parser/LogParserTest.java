package com.tracefinder.parser;

import com.tracefinder.model.LogEntry;
import com.tracefinder.model.MalformedLine;
import com.tracefinder.model.ParseResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogParserTest {

    private final LogParser parser = new LogParser();

    @Test
    void validLineParsesIntoAnEntry() {
        String line = "2024-03-15 02:14:08|WARN|192.168.1.45|/login|failed";
        ParseResult result = parser.parse(List.of(line));

        assertEquals(1, result.getEntries().size());
        assertTrue(result.getMalformedLines().isEmpty());

        LogEntry entry = result.getEntries().get(0);
        assertEquals("WARN", entry.getLevel());
        assertEquals("192.168.1.45", entry.getSourceIp());
        assertEquals(line, entry.getRawLine());
    }

    @Test
    void hiddenCharactersInSourceIpAreCleanedForGroupingButNotForDisplay() {
        // Zero-width space sits between the second and third octet.
        String line = "2024-03-15 02:14:08|WARN|192.168\u200B.1.45|/login|failed";
        ParseResult result = parser.parse(List.of(line));

        LogEntry entry = result.getEntries().get(0);

        // Cleaned for matching/grouping:
        assertEquals("192.168.1.45", entry.getSourceIp());

        // But the original line - hidden character and all - is preserved
        // verbatim for report sections that show original content.
        assertEquals(line, entry.getRawLine());
        assertTrue(entry.getRawLine().contains("\u200B"));
    }

    @Test
    void hiddenCharactersInLevelAreCleanedForMatching() {
        String line = "2024-03-15 02:14:08|WA\u200BRN|192.168.1.45|/login|failed";
        ParseResult result = parser.parse(List.of(line));

        LogEntry entry = result.getEntries().get(0);
        assertEquals("WARN", entry.getLevel());
        assertEquals(line, entry.getRawLine());
    }

    @Test
    void surroundingWhitespaceAroundFieldsIsTrimmed() {
        String line = "2024-03-15 02:14:08 | WARN | 192.168.1.45 | /login | failed";
        ParseResult result = parser.parse(List.of(line));

        LogEntry entry = result.getEntries().get(0);
        assertEquals("WARN", entry.getLevel());
        assertEquals("192.168.1.45", entry.getSourceIp());
    }

    @Test
    void wrongFieldCountIsMalformed() {
        ParseResult result = parser.parse(List.of("only|three|fields"));

        assertTrue(result.getEntries().isEmpty());
        assertEquals(1, result.getMalformedLines().size());
        MalformedLine malformed = result.getMalformedLines().get(0);
        assertEquals(1, malformed.getLineNumber());
    }

    @Test
    void unparsableTimestampIsMalformed() {
        String line = "not-a-timestamp|WARN|192.168.1.45|/login|failed";
        ParseResult result = parser.parse(List.of(line));

        assertTrue(result.getEntries().isEmpty());
        assertEquals(1, result.getMalformedLines().size());
    }
}
