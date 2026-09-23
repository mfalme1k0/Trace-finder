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

Matching a log entry's level against the rulebook is **exact and case-sensitive** — `info` does not match `INFO`.

Before the rulebook is read at all, the tool refuses it outright (fatal, no report written) if:
- the path doesn't exist
- the path isn't a regular file (e.g. it's a directory)
- the file is larger than the rulebook size limit

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
- rulebook file is invalid CSV (missing/wrong header, wrong column count, blank level, non-numeric or non-positive score, a level defined twice)
- log file contains invalid UTF-8 anywhere — including inside the discarded remainder of an over-long line
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
| `com.tracefinder` (root) | `Main` — orchestrates the pipeline. `FatalErrorException` — abstract root of every condition that stops the tool, with one concrete subclass per specific cause (e.g. `InvalidArgumentCountException`, `RulebookLoadFailedException`, `LogFileReadException`) rather than one generic exception for everything. `RulebookException` — likewise the abstract root for everything that can go wrong loading a rulebook, with a concrete subclass per case (missing file, not a regular file, too large, empty, bad header, bad column count, blank level, non-numeric or non-positive score, duplicate level). `FileLimits` — the three size limits, defined once, used everywhere they're checked. |

Every class above except `Main` splits its **logic** (pure, testable, no file access) from its **file I/O** (thin, untested wrapper) — e.g. `RulebookLoader.parse(lines)` vs. `RulebookLoader.loadFromFile(path)`. This is what makes the test suite fast and independent of the filesystem.

## Running the tests

```
mvn test
```

Runs the full JUnit 5 suite, including the exception-path tests for both the fatal-error hierarchy and the rulebook hierarchy, and the streaming/UTF-8 tests for the log reader.

## Hostile input files

> **Status: not yet built.** The size limits, streaming, and UTF-8 validation described above are implemented and unit-tested, but the project also calls for dedicated hostile *files* — real, adversarial inputs a reviewer can run the tool against directly — one per attack surface (whole log file, one log line, one field, the rulebook). Those files, this section's results table, and the demonstrations they support haven't been built yet. This section will be filled in once they are, in the same format as the rest of this README: each file's name, what it attacks, and its expected result.

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
