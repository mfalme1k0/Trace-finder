package com.tracefinder.analysis;

import com.tracefinder.model.LogEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogAnalyzer {

    private static final int FLAG_THRESHOLD = 3;

    public Findings analyze(List<LogEntry> entries, Map<String, Integer> rulebook) {
        // Seed the summary with every level the rulebook defines, at 0,
        // so a level with no matching entries still shows up in the report.
        Map<String, Integer> activitySummary = new LinkedHashMap<>();
        for (String level : rulebook.keySet()) {
            activitySummary.put(level, 0);
        }

        List<LogEntry> flagged = new ArrayList<>();
        List<UnknownPattern> unknownPatterns = new ArrayList<>();
        Map<String, Integer> totalCountByIp = new HashMap<>();
        Set<String> suspiciousIps = new HashSet<>();

        for (LogEntry entry : entries) {
            // Every cleanly parsed entry counts toward its IP's total,
            // known level or not.
            totalCountByIp.merge(entry.getSourceIp(), 1, Integer::sum);

            Integer score = rulebook.get(entry.getLevel()); // exact match, case-sensitive

            if (score == null) {
                unknownPatterns.add(new UnknownPattern(entry.getLineNumber(), entry.getRawLine()));
                continue;
            }

            activitySummary.merge(entry.getLevel(), 1, Integer::sum);

            if (score >= FLAG_THRESHOLD) {
                flagged.add(entry);
                suspiciousIps.add(entry.getSourceIp());
            }
        }

        flagged.sort(Comparator.comparingInt((LogEntry e) -> rulebook.get(e.getLevel())).reversed());

        List<IpActivity> suspiciousIpActivity = new ArrayList<>();
        for (String ip : suspiciousIps) {
            suspiciousIpActivity.add(new IpActivity(ip, totalCountByIp.get(ip)));
        }
        suspiciousIpActivity.sort(Comparator.comparingInt(IpActivity::getEntryCount).reversed());

        unknownPatterns.sort(Comparator.comparingInt(UnknownPattern::getLineNumber));

        return new Findings(activitySummary, flagged, suspiciousIpActivity, unknownPatterns);
    }
}