# TraceFinder

A command-line tool that reads a server access log, scores each entry's severity against a configurable rulebook, and writes a short report flagging what's worth a human's attention — so nobody has to read 40,000 log lines by hand to find the handful that matter.

## Why this exists

Systems record almost everything: every login, every file access, every failed attempt. If someone breaks in, the evidence is usually sitting in a log file somewhere. The problem isn't a lack of evidence — it's volume. TraceFinder doesn't understand *what happened* in any deep sense; it doesn't know that `/etc/passwd` is sensitive or that three failed logins in a row is a brute-force attempt. It only knows how to apply a scoring rulebook consistently, across every line, without getting tired or skipping the boring parts. That's the whole point: it does the tedious part so a human can do the judgment part.

## Running it

```
java -jar tracefinder.jar <log-file> <rulebook-file> <report-output-path>
```

Example:

```
java -jar tracefinder.jar logs.txt rules.csv report.txt
```

All three arguments are required, in that order. The names of the files don't matter — the *position* does.

## Input formats

### The log file (`logs.txt`)

Plain text, one entry per line, five fields separated by `|`:

```
timestamp | level | source IP | target | action
```

Example:

```
2024-03-15 02:14:08 | INFO  | 192.168.1.45  | /login        | success
2024-03-15 02:14:33 | WARN  | 203.0.113.42  | /login        | failed
2024-03-15 02:16:02 | ALERT | 203.0.113.42  | /etc/passwd   | read
```

A line is treated as **malformed** (recorded, then skipped — never fatal) if:
- it doesn't split into exactly 5 fields
- the delimiter isn't `|`
- the timestamp can't be parsed
- the line is empty
- it exceeds the 4,096-byte line length limit (see [Input limits](#input-limits)) — the line is recorded with a shortened preview instead of its full content, since the full content is deliberately never held in memory (see below)

Before the log file is read at all, the tool refuses it outright (fatal, no report written) if:
- the path doesn't exist
- the path isn't a regular file (e.g. it's a directory)
- the file is larger than the log file size limit
- the file contains invalid UTF-8 anywhere — including inside the discarded portion of an over-long line, past where the line length limit stopped the tool from keeping the rest

### The rulebook (`rules.csv`)

A CSV file with a header row and two columns:

```csv
level,severity_score
INFO,1
WARN,3
ERROR,5
ALERT,9
```

Unlike the log file, **the rulebook must be entirely valid or the tool refuses to run.** A broken rulebook means every scoring decision downstream would be unreliable, so there's no safe way to partially trust it.

Matching a log entry's level against the rulebook is **exact and case-sensitive** — `info` does not match `INFO`. Matching happens on *cleaned* values on both sides (see [Field cleaning](#field-cleaning) below) — so it's exact and case-sensitive, but tolerant of invisible characters and surrounding whitespace, which would otherwise make an entry silently fail to match a level that's really the same thing to a person reading it.

## Field cleaning

Before a field is used for matching, grouping, or duplicate-checking, it's cleaned: hidden/zero-width characters are stripped from anywhere in the string (not just the ends), then ordinary leading/trailing whitespace is trimmed. This applies to a log entry's **level** and **source IP**, and to a rulebook row's **level** — the fields this tool actually compares, groups, or counts by. `target` and `action` are trimmed but not cleaned, since nothing matches or groups on them.

**Why this matters:** an invisible character can make one value look like two. For example, `192.168.1.45` appearing on several lines, with some occurrences containing an invisible zero-width space, would otherwise be split across two rows in the report:

```
192.168.1.45: 6 entries
192.168.1.45: 3 entries
```

instead of correctly combining into one:

```
192.168.1.45: 9 entries
```

**The exact characters removed** (defined once, in `FieldCleaner.java`):

| Character | Name |
|---|---|
| U+200B | ZERO WIDTH SPACE |
| U+200C | ZERO WIDTH NON-JOINER |
| U+200D | ZERO WIDTH JOINER |
| U+FEFF | ZERO WIDTH NO-BREAK SPACE (byte-order mark) |
| U+2060 | WORD JOINER |
| U+00AD | SOFT HYPHEN |
| U+200E / U+200F | LEFT-TO-RIGHT MARK / RIGHT-TO-LEFT MARK |
| U+061C | ARABIC LETTER MARK |

This is a fixed, named list rather than "every Unicode format character." Stripping the entire Unicode "format" category would also remove characters that have real, visible layout effects in some contexts (e.g. bidi embedding/override controls) — a different and riskier change than removing characters that are invisible everywhere, regardless of surrounding text.

**Cleaned values are used for:** matching a log entry's level against the rulebook, grouping entries by IP, counting toward Activity Summary, and detecting duplicate rulebook levels — two levels that are only equal *after* cleaning (e.g. `WARN` and `WA` + zero-width-space + `RN`) are treated as duplicates, and the rulebook is refused.

**Original values are always what's displayed.** Flagged Entries, Unknown Patterns, and Malformed Lines all show the untouched original line — hidden characters and all — never the cleaned version. Only the *decision* (does this match, does this count as the same level) uses cleaned values; the report never silently rewrites what was actually in the input.

Before the rulebook is read at all, the tool refuses it outright (fatal, no report written) if:
- the path doesn't exist
- the path isn't a regular file (e.g. it's a directory)
- the file is larger than the rulebook size limit

Once past those checks, the file's bytes are read and decoded as strict UTF-8 before any row is parsed — if that decode fails, the tool refuses the rulebook the same way (fatal, no report written), even though this happens fractionally after the size check rather than before any reading at all. (The rulebook is small enough, under its own size limit, that reading it whole for this check doesn't need the streaming approach the log file uses — see [Input limits](#input-limits).)

## Input limits

TraceFinder enforces three size limits, all defined together in `FileLimits.java` so every check and every error message stays consistent with the others. Each is checked *before* any content is read — a file over its limit is refused outright, never partially processed.

| Limit | Value | Unit |
|---|---|---|
| Log file size | 10 MB | bytes |
| Rulebook file size | 100 KB | bytes |
| Single log line length | 4,096 bytes | UTF-8 encoded bytes, not characters |

**Why these values:**

- **Log file — 10 MB.** Large enough for a genuinely busy day of logs from a single source, small enough that refusing it and exiting is near-instant rather than a multi-second read for a file that was likely handed to the tool by mistake (wrong path, wrong system).
- **Rulebook — 100 KB.** A rulebook is a small, hand-maintained list of severity levels; even a few thousand levels fits comfortably under this. A rulebook anywhere near this size is far more likely to be the wrong file entirely (e.g. a log file passed in the wrong argument slot) than a legitimate rulebook.
- **Log line — 4,096 bytes, measured in bytes rather than characters.** The limit exists to bound memory use while streaming, and memory is consumed in bytes; a single UTF-8 character can take up to 4 bytes, so bounding by byte count is the unit that actually protects memory — bounding by character count would let a line of 4-byte characters use 4× the memory the limit was meant to cap. 4 KB comfortably fits this project's five-field, pipe-delimited line format many times over, while still catching a line that's missing its terminator and growing without bound.

A file **exactly** at its size limit is accepted; one byte over is refused. A line **exactly** at its byte limit is accepted; one byte over is treated as malformed (see below).

**The short form used for an over-long line:** the first 200 bytes of the line, decoded to text, followed by `... [truncated, line exceeded 4096-byte limit]`. Only those 200 bytes are ever held in memory for such a line — the rest is read and discarded (still checked for valid UTF-8 as it goes, just never stored), which is what keeps a single pathological line from growing memory use without bound.

## Output: the report

A plain-text file with five sections, always in this order, always present even when empty:

1. **Activity Summary** — entry counts per level the rulebook defines
2. **Flagged Entries** — every known entry scored 3 or higher, original line unchanged, highest severity first
3. **Suspicious Activity by IP** — any IP with at least one flagged entry, listed with its *total* entry count (not just the flagged ones), highest count first
4. **Unknown Patterns** — valid-looking entries whose level isn't in the rulebook, with original line number
5. **Malformed Lines** — lines that couldn't be parsed at all, with original line number

**⚠️ Open the report only in a plain-text editor.** It contains content taken directly from the input files — which may come from systems you don't control — reproduced verbatim in the Flagged Entries and Malformed Lines sections. Don't open it in a tool that auto-executes or auto-links content from plain text (e.g. don't paste it into something that renders it as HTML or Markdown, and be cautious with editors that have plugin/macro systems enabled).

## When the tool stops vs. keeps going

**Stops immediately** (prints a readable error, exits with a non-zero code):
- wrong number of command-line arguments
- rulebook or log file path doesn't exist, or isn't a regular file (e.g. it's a directory)
- rulebook or log file exceeds its size limit
- rulebook file is invalid CSV (missing/wrong header, wrong column count, blank level after cleaning, non-numeric or negative score, a level defined twice — including two levels equal only after cleaning)
- rulebook or log file contains invalid UTF-8 anywhere — for the log file, including inside the discarded remainder of an over-long line
- report path can't be written to

**Recorded and skipped, tool keeps going:**
- malformed log lines (including over-long ones — recorded with a shortened preview, see [Input limits](#input-limits))
- log levels not found in the rulebook ("unknown patterns")

## Architecture

Each part does one job and doesn't know the others exist. `Main` is the only piece that calls all of them, in order, passing the result of one into the next:

```
log file ──► Parser ──────► entries ─┐
                                       ├──► Analyzer ──► Findings ──► Reporter ──► report.txt
rules.csv ──► RulebookLoader ─► scores ┘
```

| Package | Responsibility |
|---|---|
| `com.tracefinder.model` | Data shapes: `LogEntry`, `MalformedLine`, `ParseResult` |
| `com.tracefinder.parser` | `LogParser` — turns raw text lines into `LogEntry` or records them as malformed; knows nothing about severity or rules. `LogFileReader` — the "refuse before reading" checks (exists / is a regular file / under the size limit). `Utf8LineReader` — streams the file line by line with bounded memory, validating UTF-8 across the whole file and enforcing the per-line byte limit; see [Input limits](#input-limits). `LogFileException` and its subclasses (`LogFileNotFoundException`, `LogFileNotARegularFileException`, `LogFileTooLargeException`, `InvalidUtf8Exception`) — one concrete type per refusal cause. |
| `com.tracefinder.rulebook` | `RulebookLoader` — reads `rules.csv` into a level → severity_score map. Knows nothing about the log file. |
| `com.tracefinder.analysis` | `LogAnalyzer` — the only piece that combines parsed entries with the rulebook to produce `Findings` (the four report sections' worth of data). |
| `com.tracefinder.report` | `ReportFormatter` (text-in, text-out) and `ReportWriter` (writes text to disk) |
| `com.tracefinder` (root) | `Main` — orchestrates the pipeline. `FatalErrorException` — abstract root of every condition that stops the tool, with one concrete subclass per specific cause (e.g. `InvalidArgumentCountException`, `RulebookLoadFailedException`, `LogFileReadException`) rather than one generic exception for everything. `RulebookException` — likewise the abstract root for everything that can go wrong loading a rulebook, with a concrete subclass per case (missing file, not a regular file, too large, invalid UTF-8, empty, bad header, bad column count, blank level, non-numeric or negative score, duplicate level). `FieldCleaner` — strips hidden/zero-width characters and surrounding whitespace from a field before it's matched, grouped, or compared; see [Field cleaning](#field-cleaning). `FileLimits` — the three size limits, defined once, used everywhere they're checked. |

Every class above except `Main` splits its **logic** (pure, testable, no file access) from its **file I/O** (thin, untested wrapper) — e.g. `RulebookLoader.parse(lines)` vs. `RulebookLoader.loadFromFile(path)`. This is what makes the test suite fast and independent of the filesystem.

## Running the tests

```
mvn test
```

Runs the full JUnit 5 suite, including the exception-path tests for both the fatal-error hierarchy and the rulebook hierarchy, and the streaming/UTF-8 tests for the log reader.

## Hostile input files

Nine files in `hostile-inputs/`, one per attack surface called for (whole log file, one log line, one field, the rulebook), each named for what it attacks. Run any of them against the tool directly to see the described result. Files attacking the log side are paired with `sample-data/rules.csv`; files attacking the rulebook side are paired with `sample-data/logs.txt`.

```
mvn clean package
java -jar target/tracefinder-1.0-SNAPSHOT.jar <log-file> <rulebook-file> report.txt
```

### Whole log file

| File | Attacks | Expected result |
|---|---|---|
| `log-file-oversized.log` | Log file size limit (10,486,788 bytes — 1,028 over the 10 MB limit) | Refused before any content is read. `Error: Could not read log file '...': Log file '...' is 10486788 bytes, exceeding the 10485760-byte limit`. Exit code 1. No `report.txt` written. |
| `log-file-truncated-utf8-at-eof.log` | Whole-file UTF-8 validity (ends on `0xC3`, the lead byte of a 2-byte sequence, with nothing after it) | Refused. `Error: Could not read log file '...': Log file contains invalid UTF-8 (detected while reading around byte offset 100)`. Exit code 1. No report written, even though the file's first line is perfectly valid. |

### One log line

| File | Attacks | Expected result |
|---|---|---|
| `log-line-too-long.log` | The 4,096-byte single-line limit (its middle line is 5,044 bytes — 948 over) | **Not fatal.** The tool runs to completion and writes a report. Line 1 (`WARN`) and line 3 (`INFO`) parse normally. Line 2 is recorded in Malformed Lines as `Line 2: <first 200 bytes>... [truncated, line exceeded 4096-byte limit]` — its full content is never held in memory. Because line 2 never became a real entry, `ALERT` shows `0` in Activity Summary despite being the level that line was using. |

### One field

| File | Attacks | Expected result |
|---|---|---|
| `log-field-delimiter-injection.log` | A field containing its own `\|` (line 2's target/action fields collapse into one field containing an extra `\|`, making 6 pieces instead of 5) | **Not fatal.** Lines 1 and 3 parse normally. Line 2 is recorded in Malformed Lines with its original content unchanged. Suspicious Activity by IP shows `203.0.113.42: 1` — only line 1's successfully-parsed entry counts; the malformed line, despite sharing the same IP, never becomes an entry and so is never counted. |
| `log-field-hidden-characters.log` | Field cleaning — line 2's source IP has a zero-width space (`U+200B`) between `203.0.113` and `.42` | **Not fatal.** Both lines parse as `ALERT` entries (score 9, both flagged). Suspicious Activity by IP shows a single combined row, `203.0.113.42: 2`, not two separate one-entry rows — this is the exact scenario described in [Field cleaning](#field-cleaning). Flagged Entries still shows both original lines verbatim, hidden character included, since display always uses the uncleaned original text. |

### The rulebook

| File | Attacks | Expected result |
|---|---|---|
| `rulebook-oversized.csv` | Rulebook size limit (103,431 bytes — 1,031 over the 100 KB limit) | Refused before any content is read. `Error: Could not load rulebook '...': Rulebook '...' is 103431 bytes, exceeding the 102400-byte limit`. Exit code 1. No report written. |
| `rulebook-duplicate-after-cleaning.csv` | Duplicate-level detection after cleaning (`WARN` on line 2, `WA` + zero-width-space + `RN` on line 3 — the same level once cleaned) | Refused. `Error: Could not load rulebook '...': Rulebook line 3 redefines level "WARN", already defined earlier in the file`. Exit code 1. No report written. |
| `rulebook-negative-score.csv` | Score validation (`WARN,-3` on line 3) | Refused. `Error: Could not load rulebook '...': Rulebook line 3 has a negative severity_score: -3`. Exit code 1. No report written. Note: a rulebook with `WARN,0` instead would **not** be refused — `0` is a valid score (see [Field cleaning](#field-cleaning) / Input formats above). |
| `rulebook-invalid-utf8.csv` | Rulebook UTF-8 validity (a raw `0x80` byte — an invalid lone continuation byte — inserted between `WA` and `RN` on line 3) | Refused. `Error: Could not load rulebook '...': Rulebook '...' contains invalid UTF-8`. Exit code 1. No report written. |

## Sample inputs

Sample input files are in `sample-data/`, exercising a normal login flow, a brute-force-then-breach scenario, a couple of malformed lines, and one log level (`CRIT`) that isn't in the rulebook:

```
mvn clean package
java -jar target/Trace-finder-1.0-SNAPSHOT.jar sample-data/logs.txt sample-data/rules.csv report.txt
```

Then open `report.txt` to see the result.

## Time window filtering

Two optional arguments let you restrict analysis to a slice of the log, on top of the original three:

```
java -jar target/tracefinder.jar logs.txt rules.csv report.txt "2024-03-15 02:00:00" "2024-03-15 03:30:00"
```

- **3 arguments** — analyse the whole file, as before.
- **5 arguments** — the 4th is the window's start, the 5th its end, in the same `yyyy-MM-dd HH:mm:ss` format the log itself uses.
- **Any other argument count** stops the tool with a message and a non-zero exit.
- **An unparseable start or end** stops the tool cleanly, naming which one failed.
- **A start later than the end** stops the tool cleanly — equal start and end is allowed and simply matches nothing.

### The boundary rule

The window is **start-inclusive, end-exclusive**: an entry timestamped exactly at the start is inside; one timestamped exactly at the end is not. This is the same convention most range APIs use (e.g. a `[start, end)` interval), and it's what makes a window with `start == end` well-defined — it's a valid, empty window rather than a special case to guard against.

All four findings — including suspicious IPs and their totals — are built only from entries inside the window. Malformed lines and unknown-pattern line numbers are unaffected by the window; they're recorded from the original file regardless of what's being analysed.

The report header gains one line, right under `Generated:`:

```
Time Window: 2024-03-15 02:00:00 to 2024-03-15 03:30:00
```

or, with no window given:

```
Time Window: full file
```

### Edge cases found, and why each earns a test

| Case | Why it matters |
|---|---|
| Entry timestamped exactly at the window's start | Confirms "inclusive" actually means inclusive — an off-by-one here silently drops the first real entry of interest. |
| Entry timestamped exactly at the window's end | Confirms "exclusive" actually excludes — without this test, a window and the very next window could double-count the boundary entry. |
| `start == end` | A window with zero duration is easy to treat as an error by accident; the spec says it's valid and simply empty, so this locks that in. |
| Window with no matching entries at all | Distinct from `start == end` — this checks the *filter*, not just the *window*, handles "nothing fell inside" without throwing or returning nulls. |
| Reversed window (`start` after `end`) | Must be rejected as a fatal, clean-stop condition rather than silently returning an empty result — an empty report for a mistyped command is worse than an error message. |
| Malformed lines and unknown-pattern line numbers under a window | These come from the *unfiltered* parse pass. Easy to accidentally re-derive them from filtered entries during a refactor, which would silently renumber or drop them — worth locking in explicitly. |