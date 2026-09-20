package com.tracefinder.parser;

import com.tracefinder.model.LogEntry;
import com.tracefinder.model.MalformedLine;
import com.tracefinder.model.ParseResult;

import com.tracefinder.TimestampFormats;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


public class LogParser {

    private static final int EXPECTED_FIELD_COUNT = 5;


    public ParseResult parse(List<String> lines) {
        List<LogEntry> entries = new ArrayList<>();
        List<MalformedLine> malformedLines = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);

            LogEntry entry = parseLine(line, lineNumber);
            if (entry != null) {
                entries.add(entry);
            } else {
                malformedLines.add(new MalformedLine(lineNumber, line));
            }
        }

        return new ParseResult(entries, malformedLines);
    }


    private LogEntry parseLine(String line, int lineNumber) {
        if (line == null || line.isEmpty()) {
            return null;
        }

        String[] fields = line.split("\\|", -1); // -1 keeps trailing empty fields
        if (fields.length != EXPECTED_FIELD_COUNT) {
            return null;
        }

        String timestampRaw = fields[0].trim();
        String level = fields[1].trim();
        String sourceIp = fields[2].trim();
        String target = fields[3].trim();
        String action = fields[4].trim();

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(timestampRaw, TimestampFormats.LOG_TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }

        return new LogEntry(timestamp, level, sourceIp, target, action, line, lineNumber);
    }
}