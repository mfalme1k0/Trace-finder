# Error Handling Refactor

## Why

The project originally had one exception, `FatalErrorException`, thrown from
every failure path in the program. That made every error message get built
by hand at the point where it was caught, made it impossible to tell two
different failures apart programmatically, and meant `Main` had to know how
to word a message for every possible failure in the system instead of each
failure describing itself.

The goal of this refactor: **give every distinct failure its own exception
class**, organized into small trees rooted at an abstract type, so that:

- each exception class owns its own message (built from the specific data
  it has, at the point it's thrown)
- callers can catch broadly (the root type) or narrowly (a specific
  subclass) depending on what they need
- adding a new failure case never means editing a `catch` block somewhere
  else in the program — it only means adding one new class

## What changed

### 1. Rulebook loading (`com.tracefinder.rulebook`)

```
RulebookException                          (abstract root)
├── RulebookNotFoundException               file doesn't exist
└── RulebookFormatException                 (abstract — content is wrong)
    ├── RulebookIsEmptyException            file has no lines at all
    ├── InvalidRulebookHeaderException      header row doesn't match
    ├── InvalidColumnCountException         a row has the wrong number of columns
    ├── BlankRulebookLevelException         a row's level column is empty
    ├── NonNumericSeverityScoreException    a row's score isn't a number
    ├── NonPositiveSeverityScoreException   a row's score is <= 0
    └── DuplicateRulebookLevelException     a level is defined more than once
```

`RulebookLoader.parse()` throws exactly one of these per problem, each
built with the specific line number and content that triggered it.
`RulebookLoader` itself never decides whether a problem is fatal — it just
reports what went wrong.

### 2. Program-level / CLI errors (`com.tracefinder`)

```
FatalErrorException                        (abstract root)
├── InvalidArgumentCountException           wrong number of CLI args
├── InvalidTimestampException                --start/--end won't parse
├── InvalidTimeWindowException               start timestamp is after end
├── RulebookLoadFailedException              rulebook couldn't be loaded at all
├── LogFileReadException                     log file couldn't be read
└── ReportWriteException                     report couldn't be written
```

`Main` is the only place that decides something is fatal. It does this by
catching the *specific* exceptions thrown deeper in the program
(`RulebookException`, `IOException`) and wrapping them into one of the
`FatalErrorException` subclasses above, which carries the original
exception as its `cause` so the underlying stack trace isn't lost.

`main()` itself still only needs one `catch (FatalErrorException e)` block,
regardless of how many concrete subclasses exist underneath it — that's
the point of the shared abstract root: the number of failure cases can grow
without the number of catch sites growing.

### 3. Fatal vs. non-fatal: the rule used throughout

A failure is **fatal** (an exception, ending the run) if the program cannot
produce a usable report without it — a missing rulebook, an unreadable log
file, an unwritable report path, bad CLI input. There is nothing sensible
to keep doing once one of these happens.

A failure is **non-fatal** (recorded, not thrown) if it affects one *item*
within a larger collection the program is processing, and the rest of that
collection is still meaningful on its own. This project has two of these
already: a malformed log line, and a log entry whose severity level isn't
in the rulebook. Both are recorded and reported, and the run continues.

Every rulebook validation case in this refactor came out fatal, because a
rulebook is loaded once, as a whole, before any log processing starts —
there's no "rest of the rulebook" to keep going with if one rule is wrong.

## Why a malformed-line exception would be redundant

It might seem inconsistent that malformed log lines *aren't* part of this
exception refactor — after all, they're clearly an error case. They were
deliberately left alone, for a few concrete reasons:

**They're not exceptional — they're expected.** A log file with thousands
of lines is very likely to have some that don't parse: truncated writes,
a stray blank line, a one-off tool that logs in a slightly different
format. That's a normal, anticipated shape for the input to take, not a
surprising breakdown of an invariant. Exceptions are for the latter.

**The current design already does the right thing, without exceptions.**
`LogParser.parse()` loops over every line, and for each one either produces
a `LogEntry` or records a `MalformedLine` — the loop never stops, and by
the end you have *both* the good entries and a full account of the bad
ones. That's exactly the outcome a "log it and continue" exception would
be trying to achieve, just without the mechanism.

**Using an exception here would need to be caught immediately anyway.**
If `parseLine()` threw, say, `MalformedLineException` instead of returning
`null`, the loop in `parse()` would have to wrap every iteration in a
`try/catch` and, inside the `catch`, do the exact same thing it does now —
build a `MalformedLine` and add it to the list. The exception wouldn't add
any capability; it would just be a more expensive, longer way to write a
conditional. Nothing downstream of `parse()` would ever see or need to
catch a `MalformedLineException`, because it would never be allowed to
leave the loop it was thrown in.

**Exceptions in the JVM aren't free.** Constructing one captures a stack
trace by default. Throwing one per bad line in a large log file, thousands
of times per run, is real, avoidable overhead for something that isn't an
exceptional event in the first place.

**A single throw can't represent "some succeeded, some didn't."** A
method that throws returns nothing on the failing path — it can't also
hand back a partial result. `parse()` needs to return *both* the entries
that worked and the lines that didn't, from a single call. An accumulator
(`ParseResult` holding two lists) does this naturally; an exception-per-line
approach doesn't, without reintroducing some other accumulator alongside it
to catch the exceptions into anyway.

So the two "non-fatal" cases in this program (`MalformedLine`,
`UnknownPattern`) are intentionally *not* exceptions, and that's treated as
a feature of the design, not a gap left over from this refactor. The
refactor's exception hierarchies exist specifically for the cases where
something needs to stop — not for every place a problem can occur.