package com.tracefinder;

import com.tracefinder.analysis.Findings;
import com.tracefinder.analysis.LogAnalyzer;
import com.tracefinder.analysis.TimeWindow;
import com.tracefinder.analysis.TimeWindowFilter;
import com.tracefinder.model.LogEntry;
import com.tracefinder.model.ParseResult;
import com.tracefinder.parser.LogParser;
import com.tracefinder.report.ReportFormatter;
import com.tracefinder.report.ReportWriter;
import com.tracefinder.rulebook.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;


public class Main {

    public static void main(String[] args) {
        try {
            run(args);
        } catch (FatalErrorException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws FatalErrorException {
        if (args.length != 3 && args.length != 5) {
            throw new FatalErrorException(
                    "Expected 3 arguments (log file, rulebook file, report file) "
                            + "or 5 arguments (the same three, plus a start and end timestamp "
                            + "for a time window), got " + args.length);
        }

        Path logPath = Path.of(args[0]);
        Path rulebookPath = Path.of(args[1]);
        Path reportPath = Path.of(args[2]);
        TimeWindow window = (args.length == 5) ? parseWindow(args[3], args[4]) : null;

        Map<String, Integer> rulebook = loadRulebook(rulebookPath);
        List<String> logLines = readLogFile(logPath);

        ParseResult parseResult = new LogParser().parse(logLines);
        List<LogEntry> entriesInWindow = new TimeWindowFilter().filter(parseResult.getEntries(), window);
        Findings findings = new LogAnalyzer().analyze(entriesInWindow, rulebook);

        String timeWindowDescription = (window == null) ? "full file" : window.describe();
        String reportText = new ReportFormatter()
                .format(findings, parseResult.getMalformedLines(), LocalDateTime.now(), timeWindowDescription);

        writeReport(reportText, reportPath);

        System.out.println("Report written to " + reportPath);
    }

    private static TimeWindow parseWindow(String startArg, String endArg) throws FatalErrorException {
        LocalDateTime start = parseTimestampArg(startArg, "start");
        LocalDateTime end = parseTimestampArg(endArg, "end");

        if (start.isAfter(end)) {
            throw new FatalErrorException(
                    "Start timestamp '" + startArg + "' must not be after end timestamp '" + endArg + "'");
        }

        return new TimeWindow(start, end);
    }

    private static LocalDateTime parseTimestampArg(String value, String label) throws FatalErrorException {
        try {
            return LocalDateTime.parse(value, TimestampFormats.LOG_TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            throw new FatalErrorException(
                    "Could not read " + label + " timestamp '" + value
                            + "'; expected format yyyy-MM-dd HH:mm:ss");
        }
    }

    private static Map<String, Integer> loadRulebook(Path rulebookPath)
            throws FatalErrorException {

        try {
            return new RulebookLoader().loadFromFile(rulebookPath);

        } catch (RulebookException | IOException e) {
            throw new FatalErrorException(
                    "Could not load rulebook '" + rulebookPath + "': " + e.getMessage()
            );
        }
    }

    private static List<String> readLogFile(Path logPath) throws FatalErrorException {
        try {
            return Files.readAllLines(logPath);
        } catch (IOException e) {
            throw new FatalErrorException("Could not read log file '" + logPath + "': " + e.getMessage());
        }
    }

    private static void writeReport(String reportText, Path reportPath) throws FatalErrorException {
        try {
            new ReportWriter().write(reportText, reportPath);
        } catch (IOException e) {
            throw new FatalErrorException("Could not write report to '" + reportPath + "': " + e.getMessage());
        }
    }
}