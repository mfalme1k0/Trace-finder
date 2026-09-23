# TraceFinder — Learning & Documentation Checklist

A learning checklist based on the **TraceFinder**, **Time Window Filter**, **Securing Input**, and **Testing/Review** tasks.

Tick a box only when you have actually understood and/or practised the topic.

---

## 1. Java Architecture & Design

### Separation of Responsibilities

* [ ] Separation of concerns
* [ ] Single Responsibility Principle (SRP)
* [ ] Cohesion
* [ ] Coupling
* [ ] Orchestration vs business logic
* [ ] Separating I/O from processing logic
* [ ] Designing components with clear responsibilities
* [ ] Understanding why `main` should orchestrate rather than perform the work
* [ ] Understanding dependency direction

### Recommended reading

* [ ] Martin Fowler — *Refactoring*
* [ ] Oracle Java documentation — Classes & Objects
* [ ] Oracle Java documentation — Packages

---

## 2. Java Data Structures

Understand not only how each collection works, but **why you would choose it**.

* [ ] `List`
* [ ] `Set`
* [ ] `Map`
* [ ] `HashMap`
* [ ] `LinkedHashMap`
* [ ] `TreeMap`
* [ ] Arrays
* [ ] Choosing a collection based on access requirements
* [ ] Key/value relationships
* [ ] Mutability vs immutability
* [ ] Basic time-complexity considerations for collections

### Recommended reading

* [ ] Java Collections Framework — Oracle
* [ ] `List` documentation
* [ ] `Set` documentation
* [ ] `Map` documentation
* [ ] `HashMap` documentation
* [ ] `TreeMap` documentation

---

# 3. Java Exceptions

## Exception Fundamentals

* [ ] What an exception represents
* [ ] Exception hierarchy
* [ ] `Throwable`
* [ ] `Exception`
* [ ] `RuntimeException`
* [ ] `Error`
* [ ] `throw`
* [ ] `throws`
* [ ] `try`
* [ ] `catch`
* [ ] `finally`

## Custom Exceptions

* [ ] Creating custom exception classes
* [ ] Giving exceptions meaningful names
* [ ] Exceptions as part of an application's API
* [ ] Distinguishing failures by exception type
* [ ] Recoverable vs unrecoverable failures

## Exception Propagation

* [ ] How an exception travels up the call stack
* [ ] Where an exception should be created
* [ ] Where an exception should be handled
* [ ] Why low-level code should not print user-facing errors
* [ ] Why `main` can be the appropriate place to handle CLI failures

## Exception Chaining

* [ ] Understanding exception causes
* [ ] Wrapping an existing exception
* [ ] Preserving the original exception
* [ ] `Throwable.getCause()`
* [ ] Understanding this pattern:

```java
throw new LogFileException("Could not read log file", e);
```

### Checked vs Unchecked

* [ ] Checked exceptions
* [ ] Unchecked exceptions
* [ ] `extends Exception`
* [ ] `extends RuntimeException`
* [ ] When checked exceptions are useful
* [ ] When unchecked exceptions are useful
* [ ] Choosing one consistently
* [ ] Being able to defend the choice

### Recommended reading

* [ ] Oracle — Java Exceptions
* [ ] Oracle — Catching and Handling Exceptions
* [ ] Oracle — The `throw` Statement
* [ ] Oracle — The `throws` Statement
* [ ] Oracle — Creating Exception Classes
* [ ] Oracle — Chained Exceptions
* [ ] Effective Java — Item 70: Use checked exceptions for recoverable conditions and runtime exceptions for programming errors

---

# 4. Automated Testing

## Testing Fundamentals

* [ ] Why automated tests exist
* [ ] Unit tests
* [ ] Integration tests
* [ ] System/end-to-end tests
* [ ] Testing behaviour rather than implementation details
* [ ] Arrange–Act–Assert

## JUnit

* [ ] `@Test`
* [ ] `assertEquals`
* [ ] `assertTrue`
* [ ] `assertFalse`
* [ ] `assertNull`
* [ ] `assertNotNull`
* [ ] `assertThrows`
* [ ] Testing exception types
* [ ] Testing exception properties
* [ ] Naming tests after the behaviour they verify

## Test Design

* [ ] Tests are independent
* [ ] Tests can run in any order
* [ ] Test data is created inside the test
* [ ] Tests don't depend on external files when testing logic
* [ ] Normal cases
* [ ] Edge cases
* [ ] Negative cases
* [ ] Regression tests
* [ ] Strong assertions
* [ ] Avoiding weakened assertions just to make tests pass

### Recommended reading

* [ ] JUnit 5 User Guide
* [ ] JUnit assertions documentation
* [ ] JUnit `assertThrows` documentation

---

# 5. Boundary & Edge-Case Testing

## Boundary Value Analysis

* [ ] What boundary-value analysis is
* [ ] Why boundary conditions cause bugs
* [ ] Testing values immediately before a boundary
* [ ] Testing the boundary itself
* [ ] Testing values immediately after a boundary

## Time Window

Understand:

```text
start <= timestamp < end
```

* [ ] Entry before start → excluded
* [ ] Entry exactly at start → included
* [ ] Entry inside window → included
* [ ] Entry exactly at end → excluded
* [ ] Entry after end → excluded
* [ ] Start equals end
* [ ] Start after end
* [ ] Empty time window
* [ ] Invalid timestamp
* [ ] Three-argument legacy behaviour
* [ ] Five-argument windowed behaviour

## Other Boundaries

* [ ] File exactly at size limit
* [ ] File one byte over limit
* [ ] Line exactly at length limit
* [ ] Line one unit over limit
* [ ] Score `0`
* [ ] Score `3`
* [ ] Score `-1`
* [ ] Score `2.5`
* [ ] Empty input
* [ ] Missing field
* [ ] Extra field

### Recommended reading

* [ ] Boundary Value Analysis
* [ ] Equivalence Partitioning
* [ ] JUnit parameterized tests

---

# 6. Java File & I/O APIs

## Paths and Files

* [ ] `Path`
* [ ] `Files`
* [ ] Checking whether a path exists
* [ ] Checking whether a path is a regular file
* [ ] File permissions
* [ ] File size
* [ ] File creation
* [ ] File writing

## Reading Files

* [ ] `InputStream`
* [ ] `Reader`
* [ ] `BufferedReader`
* [ ] `Files.newBufferedReader()`
* [ ] Reading one line at a time
* [ ] Difference between byte-oriented and character-oriented I/O
* [ ] Resource management
* [ ] try-with-resources

### Recommended reading

* [ ] Java `Path` documentation
* [ ] Java `Files` documentation
* [ ] Java `BufferedReader` documentation
* [ ] Java `InputStreamReader` documentation
* [ ] Java try-with-resources documentation

---

# 7. Streaming & Memory Management

The securing-input task introduces resource-exhaustion concerns.

## Streaming

* [ ] Understand why `Files.readAllLines()` can be dangerous for huge logs
* [ ] Understand line-by-line processing
* [ ] Process → analyse/store necessary result → continue
* [ ] Avoid storing the entire raw log
* [ ] Understand buffering
* [ ] Understand what data must remain in memory
* [ ] Understand what can be discarded after processing

## Complexity

* [ ] Understand memory complexity
* [ ] Understand `O(n)` memory vs bounded memory
* [ ] Distinguish raw input memory from analysis-result memory
* [ ] Understand why a streaming reader doesn't necessarily mean the entire program uses constant memory

### Recommended reading

* [ ] `BufferedReader`
* [ ] `Files.newBufferedReader`
* [ ] Java I/O documentation
* [ ] Basic space complexity / Big-O

---

# 8. Character Encoding & UTF-8

This is one of the most important areas of the Securing Input task.

## Fundamentals

* [ ] Bytes
* [ ] Characters
* [ ] Unicode
* [ ] Unicode code points
* [ ] Character encoding
* [ ] UTF-8
* [ ] UTF-8 byte sequences
* [ ] Decoding
* [ ] Encoding
* [ ] Malformed byte sequences

Understand:

```text
bytes
  ↓
UTF-8 decoder
  ↓
characters
```

## Java UTF-8 Handling

* [ ] `StandardCharsets.UTF_8`
* [ ] `CharsetDecoder`
* [ ] `CodingErrorAction`
* [ ] `REPORT`
* [ ] Difference between reporting malformed input and silently replacing it
* [ ] Invalid UTF-8 during streaming
* [ ] Why a separate full-file UTF-8 scan violates the intended design
* [ ] Invalid UTF-8 inside an oversized line must still be detected

### Recommended reading

* [ ] Java `Charset`
* [ ] Java `CharsetDecoder`
* [ ] Java `CodingErrorAction`
* [ ] Java `StandardCharsets`

---

# 9. Unicode & Input Cleaning

## Invisible Characters

* [ ] Unicode whitespace
* [ ] Zero-width space
* [ ] Other zero-width characters
* [ ] Byte Order Mark (BOM)
* [ ] Control characters
* [ ] Code points

Understand why these can cause:

```text
192.168.1.45
```

and:

```text
192.168.1.45<invisible character>
```

to become different strings.

## Cleaning vs Normalisation

* [ ] `trim()`
* [ ] What `trim()` actually removes
* [ ] Unicode-aware whitespace handling
* [ ] Removing specific hidden characters
* [ ] Unicode normalization
* [ ] `java.text.Normalizer`
* [ ] Difference between cleaning and normalization
* [ ] Knowing when full Unicode normalization is unnecessary

## TraceFinder Rules

* [ ] Clean fields before matching
* [ ] Clean fields before grouping
* [ ] Clean fields before counting
* [ ] Clean rulebook levels
* [ ] Check duplicate rulebook levels after cleaning
* [ ] Preserve original values where the specification requires them

### Recommended reading

* [ ] Java `Character` documentation
* [ ] `Character.isWhitespace()`
* [ ] Unicode code-point documentation
* [ ] Java `Normalizer` documentation

---

# 10. Input Validation & Defensive Programming

## Trust Boundaries

* [ ] Understand trusted vs untrusted input
* [ ] Identify external files as a trust boundary
* [ ] Validate input before processing
* [ ] Fail fast when input cannot safely be handled
* [ ] Recover when the specification allows recovery

## Resource Limits

* [ ] Log-file size limit
* [ ] Rulebook size limit
* [ ] Log-line length limit
* [ ] Why limits protect against resource exhaustion
* [ ] Bytes vs characters
* [ ] Choosing an appropriate limit
* [ ] Documenting why the limit was chosen
* [ ] Exact limit accepted
* [ ] Above limit rejected/malformed

## File Validation

* [ ] File exists
* [ ] File is readable
* [ ] File is a regular file
* [ ] Directory vs regular file
* [ ] Other filesystem objects
* [ ] Output path is writable

### Recommended reading

* [ ] OWASP Input Validation Cheat Sheet
* [ ] OWASP security/input-validation material
* [ ] Java `Files` documentation

---

# 11. Parsing & Data Validation

## Log Parsing

* [ ] Five-field log format
* [ ] Delimiter-based parsing
* [ ] Extra delimiters
* [ ] Missing fields
* [ ] Empty lines
* [ ] Invalid timestamps
* [ ] Wrong delimiters
* [ ] Preserving original content for malformed lines
* [ ] Recording original line numbers

## Rulebook Parsing

* [ ] CSV header validation
* [ ] Required columns
* [ ] Missing columns
* [ ] Score validation
* [ ] Non-negative whole numbers
* [ ] `0` is valid
* [ ] Negative numbers rejected
* [ ] Decimal numbers rejected
* [ ] Non-numeric values rejected
* [ ] Duplicate cleaned levels
* [ ] Clean rulebook values before matching
* [ ] Reject invalid rulebooks before analysis

## Unknown Patterns

* [ ] Unknown level is not malformed
* [ ] Unknown level is not an exception
* [ ] Unknown level is recorded
* [ ] Unknown level contributes to IP totals
* [ ] Unknown level is excluded from Activity Summary

---

# 12. Report Generation

* [ ] Separate report formatting from analysis
* [ ] Fixed five-section structure
* [ ] Section order
* [ ] Activity Summary
* [ ] Flagged Entries
* [ ] Suspicious Activity by IP
* [ ] Unknown Patterns
* [ ] Malformed Lines
* [ ] Empty sections still appear
* [ ] `none were found` behaviour
* [ ] Severity sorting
* [ ] IP count sorting
* [ ] Preserve original content where required
* [ ] Print cleaned values where required
* [ ] Report generation timestamp
* [ ] Time Window line
* [ ] `full file` when no window is specified

## Security consideration

* [ ] Understand that report content comes from untrusted input
* [ ] Understand why the README warns users to open reports in a plain-text editor
* [ ] Consider how hostile input could affect report consumers

---

# 13. Hostile / Adversarial Input Testing

Think like someone trying to break the program.

## Attack Surfaces

* [ ] Whole log file
* [ ] Individual log line
* [ ] Individual field
* [ ] Rulebook

## Hostile Log File

* [ ] Oversized log file
* [ ] Invalid UTF-8
* [ ] Directory instead of file
* [ ] Unreadable file
* [ ] Exact size limit
* [ ] Just above size limit

## Hostile Log Line

* [ ] Oversized line
* [ ] Exact line limit
* [ ] One unit over line limit
* [ ] Extra delimiters
* [ ] Missing fields
* [ ] Invalid timestamp
* [ ] Empty line
* [ ] Invalid UTF-8 inside oversized line

## Hostile Field

* [ ] Leading whitespace
* [ ] Trailing whitespace
* [ ] Zero-width characters
* [ ] Hidden characters
* [ ] IP grouping affected by invisible characters
* [ ] Rulebook matching affected by invisible characters

## Hostile Rulebook

* [ ] Missing required column
* [ ] Negative score
* [ ] Decimal score
* [ ] Non-numeric score
* [ ] Duplicate cleaned level
* [ ] Invalid UTF-8
* [ ] Oversized rulebook
* [ ] Exact size limit
* [ ] Just above size limit

## Hostile File Documentation

* [ ] Each hostile file has a descriptive name
* [ ] README explains what each file attacks
* [ ] README explains expected result
* [ ] README explains how to run each hostile input

---

# 14. Recoverable vs Unrecoverable Failures

## Unrecoverable

* [ ] Wrong argument count
* [ ] Missing/unreadable log
* [ ] Missing/unreadable rulebook
* [ ] Invalid rulebook
* [ ] Unwritable output
* [ ] Invalid UTF-8
* [ ] Oversized log file
* [ ] Oversized rulebook
* [ ] Invalid time window

## Recoverable

* [ ] Malformed log line
* [ ] Oversized individual log line
* [ ] Unknown rulebook level

## Architecture

* [ ] Recoverability represented by exception type
* [ ] Handler can distinguish failures by type
* [ ] No parsing exception messages to determine failure category
* [ ] Recoverable exceptions carry the necessary information
* [ ] Malformed-line exception carries line number
* [ ] Malformed-line exception carries original content

---

# 15. CLI Error Handling

* [ ] Correct argument count
* [ ] Three-argument mode
* [ ] Five-argument mode
* [ ] Invalid argument count
* [ ] Invalid timestamps
* [ ] Reversed time window
* [ ] Readable error messages
* [ ] Non-zero exit codes
* [ ] No stack traces
* [ ] No raw internal exception messages
* [ ] `main` controls user-facing failure output

---

# 16. Code Coverage

* [ ] Understand what code coverage measures
* [ ] Line coverage
* [ ] Branch coverage
* [ ] Instruction coverage
* [ ] Understand what coverage does NOT prove
* [ ] Generate JaCoCo report
* [ ] Interpret the coverage report
* [ ] Identify covered code without meaningful behavioural verification
* [ ] Understand why 100% coverage doesn't mean 100% correctness

### Recommended reading

* [ ] JaCoCo documentation
* [ ] JaCoCo Maven plugin documentation
* [ ] Coverage vs behavioural testing

---

# 17. Git & Development History

* [ ] Understand atomic commits
* [ ] Small focused commits
* [ ] One logical change per commit
* [ ] Meaningful commit messages
* [ ] Avoid `wip`, `fix`, `stuff`, etc.
* [ ] `git log`
* [ ] `git show`
* [ ] `git diff`
* [ ] Reviewing historical changes
* [ ] Preserving project history when extending the project
* [ ] Commit changes in logical steps

### Optional

* [ ] Conventional Commits
* [ ] `git bisect`
* [ ] Interactive staging

### Recommended reading

* [ ] Pro Git
* [ ] Conventional Commits

---

# 18. Documentation & Reproducibility

* [ ] README installation/setup instructions
* [ ] README run instructions
* [ ] README test instructions
* [ ] README hostile-input instructions
* [ ] README explains chosen limits
* [ ] README explains reasoning behind limits
* [ ] README documents over-long-line representation
* [ ] README documents time-window behaviour
* [ ] README documents edge cases
* [ ] README documents coverage generation
* [ ] README documents expected hostile-input results
* [ ] README warns about opening reports in a plain-text editor
* [ ] README traces an exception from creation → propagation → handling
* [ ] README accurately reflects implementation

---

# 19. Code Review & Defending Your Decisions

Be able to explain:

* [ ] Why each component has its responsibility
* [ ] Why you chose each data structure
* [ ] Why you chose your exception types
* [ ] Why you chose checked or unchecked exceptions
* [ ] Why some failures are recoverable
* [ ] Why other failures stop the program
* [ ] Why `main` handles user-facing errors
* [ ] Why lower-level code doesn't print failures
* [ ] Why the log is processed as a stream
* [ ] What remains in memory while processing a large log
* [ ] Why you chose each input limit
* [ ] Why fields are cleaned
* [ ] Why original values are preserved
* [ ] Which hostile inputs your tool detects
* [ ] Which hostile inputs it cannot detect
* [ ] Which tests protect against quietly incorrect reports
* [ ] Which test would fail if the time-window end became inclusive
* [ ] Which edge case you discovered last
* [ ] What your coverage report actually tells you
* [ ] What your coverage report cannot tell you

---

# 20. AI-Assisted Development

The review explicitly requires you to account for how you used AI.

* [ ] Know what AI assistance you used
* [ ] Know why you asked AI for help
* [ ] Verify AI-generated suggestions yourself
* [ ] Compare suggestions against requirements
* [ ] Run tests against AI-generated code
* [ ] Read and understand generated code before using it
* [ ] Identify an example where AI gave an incorrect/unhelpful answer
* [ ] Explain how you detected the problem
* [ ] Explain what you changed or rejected
* [ ] Be able to defend code you kept even when AI suggested it

---

# Suggested Learning Order

Work through the material roughly in this order rather than trying to learn everything simultaneously.

### Phase 1 — Understand the original TraceFinder

* [ ] Separation of concerns
* [ ] Data structures
* [ ] Parsing
* [ ] Exceptions
* [ ] JUnit basics
* [ ] Arrange–Act–Assert

### Phase 2 — Understand the Time Window task

* [ ] Requirements → implementation
* [ ] Boundary-value analysis
* [ ] Edge-case testing
* [ ] Regression testing
* [ ] JaCoCo
* [ ] Coverage limitations

### Phase 3 — Understand Securing Input

* [ ] Defensive programming
* [ ] Input validation
* [ ] Resource exhaustion
* [ ] File limits
* [ ] Streaming I/O
* [ ] UTF-8
* [ ] `CharsetDecoder`
* [ ] Unicode/code points
* [ ] Input cleaning
* [ ] Hostile input

### Phase 4 — Understand the exception/refactoring requirements

* [ ] Custom exception hierarchy
* [ ] Recoverable vs unrecoverable failures
* [ ] Exception propagation
* [ ] Exception wrapping
* [ ] Exception causes
* [ ] Checked vs unchecked exceptions
* [ ] Centralised CLI error handling

### Phase 5 — Engineering practice

* [ ] Git atomic commits
* [ ] README/reproducibility
* [ ] Code review
* [ ] Defending design decisions
* [ ] Responsible AI-assisted development

---

# Core Documentation Bookmark List

## Java

* [ ] Java Collections Framework
* [ ] Java Exceptions
* [ ] `Throwable`
* [ ] `Exception`
* [ ] `RuntimeException`
* [ ] `Files`
* [ ] `Path`
* [ ] `BufferedReader`
* [ ] `InputStreamReader`
* [ ] `Charset`
* [ ] `CharsetDecoder`
* [ ] `CodingErrorAction`
* [ ] `StandardCharsets`
* [ ] `Character`
* [ ] `Normalizer`

## Testing

* [ ] JUnit 5 User Guide
* [ ] JUnit assertions
* [ ] `assertThrows`
* [ ] JaCoCo documentation

## Security

* [ ] OWASP Input Validation Cheat Sheet
* [ ] OWASP threat modelling material
* [ ] Resource exhaustion / denial-of-service concepts

## Git

* [ ] Pro Git
* [ ] Conventional Commits

---

# Final Goal

By the end of these tasks, you should be able to look at TraceFinder and explain the entire path:

```text
                 UNTRUSTED INPUT
                       │
                       ▼
                 File validation
                       │
                       ▼
                 Streaming reader
                       │
                       ▼
                    Parser
                       │
             ┌─────────┴─────────┐
             │                   │
        malformed            valid entry
             │                   │
             ▼                   ▼
       recoverable          Time filter
       exception                 │
                                 ▼
                            Rule matching
                                 │
                 ┌───────────────┼───────────────┐
                 │               │               │
             known level    unknown level     invalid?
                 │               │
                 ▼               ▼
              analysis       finding
                 │
                 ▼
             Findings
                 │
                 ▼
             Formatter
                 │
                 ▼
              Report
```

And you should be able to explain **why each arrow exists, what can fail at each stage, whether that failure is recoverable, and how the tests prove the behaviour.**
