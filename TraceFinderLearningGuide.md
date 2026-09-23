# TraceFinder — Fundamentals Learning Guide

> **Purpose:** Learn the Java and software-engineering fundamentals behind TraceFinder well enough to rebuild the project from the requirements with little to no assistance.
>
> **Principle:** Do not study TraceFinder as a collection of solutions. Study the concepts that make the solution possible.

---

## How to Use This Guide

For each topic:

1. Read the recommended documentation.
2. Write a small example yourself.
3. Explain the concept in your own words.
4. Apply it to TraceFinder.
5. Only then look at the existing implementation.
6. If you use AI, use it to explain or challenge your understanding rather than generate the solution.

### Completion rule

A topic is **not complete** because you read the documentation.

Consider it complete when you can:

* [ ] Explain the concept without looking it up.
* [ ] Write a small example from memory.
* [ ] Explain why TraceFinder needs it.
* [ ] Identify at least one alternative approach.
* [ ] Explain why you chose your approach.
* [ ] Debug a basic problem involving the concept without assistance.

---

# 1. Java Fundamentals

These are the foundations everything else depends on.

## 1.1 Classes and Objects

### Learn

* [ ] Classes
* [ ] Objects
* [ ] Fields
* [ ] Methods
* [ ] Constructors
* [ ] Instance vs static members
* [ ] Access modifiers
* [ ] Encapsulation

### Read

**Oracle — Classes and Objects**

https://docs.oracle.com/javase/tutorial/java/javaOO/

### Be able to explain

* What is an object?
* What is the difference between a class and an object?
* Why should fields generally be encapsulated?
* When should a method belong to an object?
* What does `static` actually mean?

### Practice

Build a small Java program containing:

* a `LogEntry`
* a `Rule`
* a collection of both
* methods that operate on them

Do this independently of TraceFinder.

---

# 2. Interfaces and Abstraction

TraceFinder contains multiple responsibilities that can be separated behind interfaces.

## Learn

* [ ] Interfaces
* [ ] Implementations
* [ ] Abstraction
* [ ] Polymorphism
* [ ] Dependency inversion
* [ ] Programming against interfaces

### Read

**Oracle — Interfaces and Inheritance**

https://docs.oracle.com/javase/tutorial/java/IandI/

### Be able to explain

* Why create an interface?
* When is an interface unnecessary?
* What is polymorphism?
* What does "program to an interface" mean?
* How does an interface reduce coupling?

### Practice

Create:

```text
LogReader
    |
    +-- FileLogReader
    +-- InMemoryLogReader
```

Then write code that depends on `LogReader`, not specifically on `FileLogReader`.

---

# 3. Separation of Responsibilities

This is one of the most important concepts for rebuilding TraceFinder yourself.

## Learn

* [ ] Single Responsibility Principle
* [ ] Separation of concerns
* [ ] Cohesion
* [ ] Coupling
* [ ] Dependency direction
* [ ] Orchestration vs business logic
* [ ] I/O vs processing

### Read

**Martin Fowler — Single Responsibility Principle**

https://martinfowler.com/bliki/SingleResponsibilityPrinciple.html

**Martin Fowler — Dependency Injection**

https://martinfowler.com/articles/injection.html

### Be able to explain

Why shouldn't one method:

```text
read file
→ parse
→ validate
→ calculate
→ sort
→ generate report
→ print errors
```

do everything?

You should be able to identify separate responsibilities such as:

```text
Input
Parsing
Validation
Analysis
Reporting
CLI orchestration
```

### Practice

Take a simple CSV-processing program and deliberately write it as one large class.

Then refactor it into separate responsibilities.

---

# 4. Java Collections

TraceFinder relies heavily on collections.

## Learn

* [ ] `List`
* [ ] `Set`
* [ ] `Map`
* [ ] `HashMap`
* [ ] `LinkedHashMap`
* [ ] `TreeMap`
* [ ] `ArrayList`
* [ ] Iteration
* [ ] Generics
* [ ] Choosing the correct collection

### Read

**Oracle — The Collections Framework**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/doc-files/coll-overview.html

**Oracle — Collection Interfaces**

https://docs.oracle.com/javase/tutorial/collections/interfaces/

### Be able to explain

Why would you choose:

```java
List<LogEntry>
```

instead of:

```java
Set<LogEntry>
```

Why would a rulebook naturally be represented as:

```java
Map<String, Integer>
```

?

What is the difference between:

```text
HashMap
LinkedHashMap
TreeMap
```

?

### Practice

Implement:

* word counter
* IP counter
* lookup table
* duplicate detector
* sorted results

without looking at previous solutions.

---

# 5. Generics

## Learn

* [ ] Generic classes
* [ ] Generic methods
* [ ] Type parameters
* [ ] Why collections use generics
* [ ] Type safety

### Read

**Oracle — Generics**

https://docs.oracle.com/javase/tutorial/java/generics/

### Be able to explain

Why is this useful?

```java
Map<String, Integer>
```

What problem would exist if everything were:

```java
Map<Object, Object>
```

?

---

# 6. Exceptions

This is a major part of the TraceFinder curriculum.

## Learn

* [ ] `Throwable`
* [ ] `Exception`
* [ ] `RuntimeException`
* [ ] Checked exceptions
* [ ] Unchecked exceptions
* [ ] `throw`
* [ ] `throws`
* [ ] `try`
* [ ] `catch`
* [ ] `finally`
* [ ] Custom exceptions
* [ ] Exception propagation
* [ ] Exception chaining
* [ ] `getCause()`

### Read

**Oracle — Exceptions**

https://docs.oracle.com/javase/tutorial/essential/exceptions/

**Oracle — Throwable API**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Throwable.html

### Be able to explain

* What is an exception?
* Why do exceptions exist?
* What is the difference between throwing and catching?
* What is exception propagation?
* What is the difference between checked and unchecked exceptions?
* What is exception chaining?
* Why preserve the original cause?

---

# 7. Custom Exceptions

## Learn

* [ ] Designing exception types
* [ ] Naming exceptions
* [ ] Exception hierarchy
* [ ] Recoverable vs unrecoverable failures
* [ ] Exceptions as part of an API

### Read

**Effective Java — Exceptions**

Read the sections covering:

* checked exceptions
* exceptions for exceptional conditions
* exception translation
* exception chaining

### Be able to explain

Why might these be different types?

```text
InvalidArgumentsException
RulebookNotFoundException
InvalidRulebookException
MalformedLogLineException
OutputNotWritableException
```

And why might some of them belong to different recoverability categories?

---

# 8. Exception Propagation

This is especially important for the CLI.

Understand the flow:

```text
low-level operation
        ↓
throws exception
        ↓
higher-level component
        ↓
propagates or translates
        ↓
main()
        ↓
user-friendly message
```

## Learn

* [ ] Exceptions should not necessarily be handled where they occur
* [ ] Propagation
* [ ] Translation
* [ ] Exception boundaries
* [ ] Keeping user-interface concerns out of lower layers

### Be able to explain

Why is this problematic?

```java
catch (Exception e) {
    System.out.println("Something went wrong");
}
```

inside every component?

What happens to debugging information?

What happens to consistent CLI behavior?

---

# 9. Try-With-Resources

TraceFinder reads files and therefore must manage resources correctly.

## Learn

* [ ] `AutoCloseable`
* [ ] `Closeable`
* [ ] try-with-resources
* [ ] resource cleanup
* [ ] suppressed exceptions

### Read

**Oracle — The try-with-resources Statement**

https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html

### Be able to write

```java
try (BufferedReader reader = ...) {
    ...
}
```

without assistance.

---

# 10. Java File I/O

## Learn

* [ ] `Path`
* [ ] `Files`
* [ ] `File`
* [ ] `BufferedReader`
* [ ] `InputStream`
* [ ] `Reader`
* [ ] `InputStreamReader`
* [ ] Checking file existence
* [ ] Checking regular files
* [ ] Reading file metadata
* [ ] File size
* [ ] File permissions

### Read

**Oracle — File I/O**

https://docs.oracle.com/javase/tutorial/essential/io/

**Java `Files` API**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html

**Java `Path` API**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Path.html

### Be able to explain

* Why use `Path` instead of manually manipulating strings?
* How do you check whether something is a regular file?
* How do you determine its size?
* How do you stream its contents?

---

# 11. Streaming and Memory Management

This is a core requirement of the securing-input task.

## Learn

* [ ] Streaming
* [ ] Buffered reading
* [ ] Memory usage
* [ ] `Files.readAllLines`
* [ ] Why loading an entire file can be dangerous
* [ ] Processing one record at a time
* [ ] Space complexity

### Be able to explain

Why is:

```java
Files.readAllLines(path)
```

potentially dangerous for an untrusted log file?

Understand the difference between:

```text
Read entire file
        ↓
Store entire file
        ↓
Process
```

and:

```text
Read line
 ↓
Process
 ↓
Discard
 ↓
Read next line
```

### Practice

Write a program that counts lines in a very large file without storing all lines.

---

# 12. File Size Limits

## Learn

* [ ] Bytes vs characters
* [ ] File size
* [ ] Resource limits
* [ ] Fail-before-processing
* [ ] Resource exhaustion

### Important distinction

A file size is measured in:

```text
bytes
```

A string length is measured in terms of Java's character representation.

These are not necessarily the same thing.

### Be able to explain

Why could:

```text
100 characters
```

occupy a different number of bytes depending on encoding?

---

# 13. Character Encoding

This is essential for the UTF-8 requirements.

## Learn

* [ ] Characters
* [ ] Bytes
* [ ] Encoding
* [ ] Decoding
* [ ] UTF-8
* [ ] Unicode
* [ ] Charset
* [ ] Malformed byte sequences

### Read

**Oracle — StandardCharsets**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/charset/StandardCharsets.html

**Oracle — Charset**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/charset/Charset.html

### Be able to explain

What happens here?

```text
bytes
   ↓
UTF-8 decoder
   ↓
characters
   ↓
String
```

And why can invalid UTF-8 cause a problem?

---

# 14. Strict UTF-8 Decoding

TraceFinder must reject invalid UTF-8 rather than silently replacing invalid data.

## Learn

* [ ] `CharsetDecoder`
* [ ] `CodingErrorAction`
* [ ] `REPORT`
* [ ] `REPLACE`
* [ ] `IGNORE`

### Read

**CharsetDecoder**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/charset/CharsetDecoder.html

**CodingErrorAction**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/charset/CodingErrorAction.html

### Be able to explain

The difference between:

```text
REPORT
REPLACE
IGNORE
```

and why a security-sensitive application might deliberately choose strict decoding.

---

# 15. Unicode and Text Cleaning

TraceFinder must deal with hidden and zero-width characters.

## Learn

* [ ] Unicode code points
* [ ] UTF-16
* [ ] Java `char`
* [ ] Unicode whitespace
* [ ] Zero-width characters
* [ ] BOM
* [ ] Control characters
* [ ] `Character`
* [ ] `String`
* [ ] `trim()`
* [ ] `strip()`

### Read

**Java `Character` API**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Character.html

**Java `String` API**

https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/String.html

### Be able to explain

Why is:

```java
value.trim()
```

not necessarily equivalent to:

```text
remove every unwanted Unicode character
```

?

---

# 16. Parsing

Parsing is one of the central TraceFinder concepts.

## Learn

* [ ] Tokenization
* [ ] Delimiters
* [ ] Field counts
* [ ] Parsing vs validation
* [ ] Syntax vs semantics
* [ ] Fail-fast parsing
* [ ] Defensive parsing

For the log:

```text
timestamp | level | source IP | target | action
```

you should understand exactly what constitutes:

```text
valid
malformed
unknown
```

### Be able to explain

Why is an unknown level:

```text
valid structure + unknown value
```

rather than necessarily:

```text
malformed input
```

?

---

# 17. Timestamps and `java.time`

## Learn

* [ ] `Instant`
* [ ] Parsing timestamps
* [ ] `DateTimeParseException`
* [ ] Time comparison
* [ ] Inclusive boundaries
* [ ] Exclusive boundaries

### Read

**Oracle — Date and Time API**

https://docs.oracle.com/javase/tutorial/datetime/

### Be able to implement

```text
start <= timestamp < end
```

without assistance.

---

# 18. Boundary Value Analysis

This is an important testing fundamental.

## Learn

* [ ] Boundary conditions
* [ ] Equivalence classes
* [ ] Edge cases
* [ ] Off-by-one errors
* [ ] Inclusive boundaries
* [ ] Exclusive boundaries

### Read

Search/read about:

**Boundary Value Analysis**

https://en.wikipedia.org/wiki/Boundary-value_analysis

### Apply it to TraceFinder

For:

```text
start <= timestamp < end
```

test:

* [ ] before start
* [ ] exactly start
* [ ] inside window
* [ ] exactly end
* [ ] after end
* [ ] start == end
* [ ] start > end

### Fundamental question

If you changed `< end` to `<= end`, which test should fail?

You should be able to answer this immediately.

---

# 19. JUnit 5

Testing is not just something required by TraceFinder. It is a fundamental software-engineering skill.

## Learn

* [ ] Unit tests
* [ ] Test classes
* [ ] `@Test`
* [ ] Assertions
* [ ] `assertEquals`
* [ ] `assertTrue`
* [ ] `assertFalse`
* [ ] `assertThrows`
* [ ] Test lifecycle
* [ ] Test independence

### Read

**JUnit 5 User Guide**

https://junit.org/junit5/docs/current/user-guide/

### Be able to write

A test from a requirement without first writing the implementation.

---

# 20. Arrange–Act–Assert

## Learn

```text
Arrange
   ↓
Act
   ↓
Assert
```

Example structure:

```java
// Arrange

// Act

// Assert
```

### Be able to explain

Why should tests generally have one clear action?

Why should test data be created inside the test rather than depend on external files?

---

# 21. Testing Behavior Instead of Implementation

This is critical.

## Learn

* [ ] Behavioral testing
* [ ] Implementation details
* [ ] Public contracts
* [ ] Regression testing
* [ ] Refactoring-safe tests

### Example

Prefer testing:

```text
"An unknown level becomes an Unknown Pattern"
```

over testing:

```text
"HashMap contains this exact internal key"
```

### Be able to explain

Why can a test pass while the application behavior is still wrong?

---

# 22. Negative Testing

## Learn

* [ ] Invalid input
* [ ] Expected failures
* [ ] Exception assertions
* [ ] Failure contracts
* [ ] Error-path testing

TraceFinder should test things such as:

* [ ] Wrong argument count
* [ ] Missing log
* [ ] Missing rulebook
* [ ] Invalid rulebook
* [ ] Invalid timestamp
* [ ] Reversed window
* [ ] Unwritable output
* [ ] Malformed log line
* [ ] Invalid UTF-8

---

# 23. Testing Exceptions Properly

## Learn

* [ ] `assertThrows`
* [ ] Testing exception type
* [ ] Testing exception data
* [ ] Testing exception cause
* [ ] Avoiding message-based testing where possible

### Be able to write tests such as

```text
given malformed line
when parser processes it
then MalformedLogLineException is thrown
and it contains:
    line number
    original content
```

You should understand why testing the **type and data** is generally stronger than checking:

```text
exception.getMessage()
```

for a particular sentence.

---

# 24. Checked vs Unchecked Exceptions

This is one of the topics you should understand deeply rather than simply copy from TraceFinder.

## Learn

* [ ] Checked exceptions
* [ ] Unchecked exceptions
* [ ] API design
* [ ] Recoverable conditions
* [ ] Programming errors
* [ ] Trade-offs

### Read

**Effective Java — Exceptions**

Study the discussion around when to use checked vs unchecked exceptions.

### Be able to defend

For every custom exception in TraceFinder:

```text
Why does this exception exist?
Why is it checked/unchecked?
Should the caller recover?
Where should it be handled?
```

---

# 25. Exception Chaining

## Learn

* [ ] Original cause
* [ ] Wrapping exceptions
* [ ] `Throwable#getCause`
* [ ] Preserving diagnostic information

Example concept:

```text
IOException
   ↓
FileReadingException
   ↓
CLI handling
```

The higher-level exception adds application meaning while preserving the lower-level cause.

### Be able to explain

Why is this better than:

```java
catch (IOException e) {
    throw new FileReadingException();
}
```

when the original exception is lost?

---

# 26. Recoverable vs Unrecoverable Errors

Build this distinction mentally:

### Recoverable

The application can continue processing.

Examples:

```text
malformed log line
unknown level
overlong line
```

### Unrecoverable

Continuing would make the result invalid or impossible.

Examples:

```text
invalid rulebook
invalid UTF-8
missing input file
invalid CLI arguments
unwritable output
invalid time window
```

### Be able to explain

Why can TraceFinder continue after:

```text
line 57 is malformed
```

but cannot sensibly continue after:

```text
rulebook cannot be parsed
```

?

---

# 27. Input Validation

## Learn

* [ ] Trust boundaries
* [ ] Untrusted input
* [ ] Allow-list validation
* [ ] Structural validation
* [ ] Semantic validation
* [ ] Resource limits
* [ ] Fail-fast validation

### Read

**OWASP — Input Validation Cheat Sheet**

https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html

### Be able to identify

Every external input to TraceFinder:

```text
CLI arguments
file paths
file contents
timestamps
rulebook levels
severity scores
log fields
```

Then ask:

> What assumptions am I making about this value?

---

# 28. Threat Modeling

You do not need to become a security specialist for TraceFinder.

You do need to understand the basic idea:

```text
Input
  ↓
Assumption
  ↓
Failure
  ↓
Impact
```

### Practice

For every external input ask:

```text
What if it is:

empty?
huge?
malformed?
unexpected?
duplicated?
encoded incorrectly?
a directory instead of a file?
full of hidden characters?
```

---

# 29. Regular Files and Filesystem Validation

## Learn

* [ ] Regular file
* [ ] Directory
* [ ] Symbolic links
* [ ] Special filesystem objects
* [ ] Readability
* [ ] Writable output
* [ ] `Files.isRegularFile`

### Be able to explain

Why is:

```text
"the path exists"
```

not enough to establish:

```text
"this is a valid input file"
```

?

---

# 30. Report Generation

## Learn

* [ ] Deterministic formatting
* [ ] Fixed section ordering
* [ ] Empty results
* [ ] Sorting
* [ ] Presentation vs analysis
* [ ] Formatting as a separate responsibility

TraceFinder requires:

```text
Activity Summary
Flagged Entries
Suspicious Activity by IP
Unknown Patterns
Malformed Lines
```

### Be able to explain

Why should report formatting not contain the actual analysis algorithms?

---

# 31. Sorting

## Learn

* [ ] `Comparator`
* [ ] `Comparator.comparing`
* [ ] `thenComparing`
* [ ] Reverse ordering
* [ ] Sorting collections
* [ ] Deterministic output

### Practice

Sort objects by:

```text
severity descending
count descending
name ascending
```

without assistance.

---

# 32. Cleaned Data vs Original Data

This is an important data-modeling concept in TraceFinder.

Understand the difference between:

```text
original input
```

and:

```text
normalized/cleaned value
```

For example:

```text
" INFO\u200B "
```

may become:

```text
"INFO"
```

for matching.

But the original line may still need to appear in the report.

### Be able to explain

Why shouldn't the application simply replace the original value everywhere?

---

# 33. Data Lineage

This is a useful concept to learn from TraceFinder.

Understand:

```text
Input
  ↓
Parsed value
  ↓
Cleaned value
  ↓
Analysis
  ↓
Report
```

while preserving enough information to answer:

> Where did this result come from?

This is why line numbers and original log entries matter.

---

# 34. Memory Complexity

Learn basic Big-O notation.

## Learn

* [ ] O(1)
* [ ] O(n)
* [ ] O(log n)
* [ ] O(n log n)
* [ ] Time complexity
* [ ] Space complexity

### Read

**Big-O notation**

https://en.wikipedia.org/wiki/Big_O_notation

### Apply it

Ask:

```text
How much memory does TraceFinder use as the log grows?
```

You should understand why streaming changes the answer.

---

# 35. Code Coverage

## Learn

* [ ] Line coverage
* [ ] Branch coverage
* [ ] Instruction coverage
* [ ] Coverage reports
* [ ] Coverage limitations

### Read

**JaCoCo Documentation**

https://www.jacoco.org/jacoco/trunk/doc/

### Important principle

High coverage does **not** prove correct behavior.

You should be able to give an example of:

```text
100% line coverage
```

with a bug still present.

---

# 36. Property and Invariant Thinking

Go beyond individual examples.

Ask:

> What must always be true?

Examples from TraceFinder:

```text
Unknown levels never appear in Activity Summary.
```

```text
Unknown entries can still contribute to suspicious IP totals.
```

```text
Malformed lines never affect analysis counts.
```

```text
The end timestamp is excluded.
```

```text
Malformed line numbers refer to original file positions.
```

### Practice

Turn requirements into statements that should always hold.

---

# 37. Regression Testing

## Learn

* [ ] Regression
* [ ] Regression test
* [ ] Refactoring safety
* [ ] Existing behavior vs new behavior

When adding the time-window feature, the original:

```text
3-argument invocation
```

must continue to work.

### Be able to explain

Why should the old behavior have tests even after adding new functionality?

---

# 38. CLI Design

## Learn

* [ ] `main`
* [ ] `String[] args`
* [ ] Argument validation
* [ ] Exit codes
* [ ] Standard output
* [ ] Standard error
* [ ] User-facing errors

### Be able to design

```text
3 arguments → full file

5 arguments → time window

anything else → failure
```

without assistance.

---

# 39. Unix Process Exit Codes

## Learn

* [ ] Exit code `0`
* [ ] Non-zero exit codes
* [ ] `System.exit`
* [ ] Shell behavior

### Practice

Run a Java program and inspect:

```bash
echo $?
```

Understand what the value means.

---

# 40. Git as a Software Engineering Tool

Git is part of the engineering process, not just a backup system.

## Learn

* [ ] Commit
* [ ] Branch
* [ ] Merge
* [ ] Rebase
* [ ] Diff
* [ ] Log
* [ ] Revert
* [ ] Cherry-pick
* [ ] Atomic commits

### Read

**Pro Git**

https://git-scm.com/book/en/v2

### Practice

Make small commits such as:

```text
feat: add time window parsing
test: cover time window boundaries
docs: document time window behavior
```

rather than:

```text
update everything
```

---

# 41. Code Review

Learn to review code against:

```text
Requirement
    ↓
Implementation
    ↓
Test
    ↓
Documentation
```

For every feature ask:

* [ ] Is the requirement implemented?
* [ ] Is the behavior tested?
* [ ] Are failure paths tested?
* [ ] Is the implementation understandable?
* [ ] Does documentation match reality?
* [ ] Did the change break existing behavior?

---

# 42. Reading Existing Code

A major goal of this project is becoming comfortable entering an unfamiliar codebase.

Practice reading in this order:

```text
main()
 ↓
application/orchestration
 ↓
domain logic
 ↓
parsing
 ↓
I/O
 ↓
exceptions
 ↓
tests
```

Do not immediately start reading every class.

First identify:

```text
Where does execution begin?
Where does data enter?
Where is it transformed?
Where is it analyzed?
Where is it written?
Where are failures handled?
```

---

# 43. Requirements → Design

This is perhaps the most important skill.

Take a requirement such as:

> Unknown levels should be recorded as unknown patterns and processing should continue.

Translate it yourself:

```text
Requirement
    ↓
What data is required?
    ↓
What component owns the behavior?
    ↓
What happens on failure?
    ↓
What exception/state represents it?
    ↓
How do I test it?
    ↓
How does the report expose it?
```

You should eventually be able to perform this process without assistance.

---

# 44. Requirements → Tests

Before implementing a feature, practice extracting tests.

Example:

> The time window is start-inclusive and end-exclusive.

Derive:

```text
timestamp < start       → excluded
timestamp == start      → included
start < timestamp < end → included
timestamp == end        → excluded
timestamp > end         → excluded
```

This is the kind of reasoning you should become comfortable doing automatically.

---

# 45. AI-Assisted Development

The goal is not to avoid AI.

The goal is to **not depend on AI to understand your own code**.

## Learn

* [ ] Asking AI for explanations
* [ ] Asking for alternative designs
* [ ] Checking AI claims against documentation
* [ ] Writing tests before accepting generated code
* [ ] Identifying incorrect AI suggestions
* [ ] Reviewing generated code yourself

### Practice

When AI suggests code, ask yourself:

```text
What does this code do?
Why does it work?
What assumption does it make?
What could break it?
What requirement does it satisfy?
What requirement does it not satisfy?
```

Keep at least one example of an AI suggestion you rejected and document why.

---

# 46. Recommended Learning Order

Do **not** study everything simultaneously.

Follow this sequence.

## Phase 1 — Java Foundations

* [ ] Classes and objects
* [ ] Interfaces
* [ ] Encapsulation
* [ ] Generics
* [ ] Collections
* [ ] `List`
* [ ] `Set`
* [ ] `Map`
* [ ] `Comparator`

---

## Phase 2 — Exceptions

* [ ] Exception hierarchy
* [ ] Checked exceptions
* [ ] Unchecked exceptions
* [ ] Custom exceptions
* [ ] Propagation
* [ ] Exception chaining
* [ ] Try-with-resources

---

## Phase 3 — File I/O

* [ ] `Path`
* [ ] `Files`
* [ ] `BufferedReader`
* [ ] Streams
* [ ] Resource management
* [ ] File metadata
* [ ] Regular files

---

## Phase 4 — Parsing and Text

* [ ] Strings
* [ ] Delimiters
* [ ] Parsing
* [ ] Validation
* [ ] `java.time`
* [ ] UTF-8
* [ ] Unicode
* [ ] `CharsetDecoder`
* [ ] Hidden characters

---

## Phase 5 — Software Design

* [ ] Separation of concerns
* [ ] Single Responsibility Principle
* [ ] Cohesion
* [ ] Coupling
* [ ] Interfaces
* [ ] Dependency direction
* [ ] Orchestration

---

## Phase 6 — Testing

* [ ] JUnit
* [ ] AAA
* [ ] Unit tests
* [ ] Negative tests
* [ ] Boundary tests
* [ ] Regression tests
* [ ] Exception testing
* [ ] Behavioral testing

---

## Phase 7 — Security Fundamentals

* [ ] Untrusted input
* [ ] Input validation
* [ ] Resource limits
* [ ] File size limits
* [ ] Line limits
* [ ] Invalid encoding
* [ ] Hostile input
* [ ] Resource exhaustion

---

## Phase 8 — Quality

* [ ] Code coverage
* [ ] Git history
* [ ] Code review
* [ ] Documentation
* [ ] Requirements tracing
* [ ] AI-assisted development

---

# 47. Final "Can I Rebuild TraceFinder?" Test

Do not look at the existing TraceFinder implementation.

Take only the requirements.

Then attempt to design the project yourself.

You should be able to answer:

### Architecture

* [ ] Where does the application start?
* [ ] What component handles CLI arguments?
* [ ] What component reads files?
* [ ] What component parses logs?
* [ ] What component validates the rulebook?
* [ ] What component performs analysis?
* [ ] What component generates the report?
* [ ] Where are exceptions handled?

### Data

* [ ] What represents a log entry?
* [ ] What represents a rule?
* [ ] Which collections are needed?
* [ ] How are IP counts represented?
* [ ] How are unknown patterns represented?
* [ ] How are malformed lines represented?

### Exceptions

* [ ] Which failures are recoverable?
* [ ] Which are unrecoverable?
* [ ] Which custom exceptions are needed?
* [ ] Where are they thrown?
* [ ] Where are they handled?
* [ ] How is the original cause preserved?

### Input Security

* [ ] What inputs are untrusted?
* [ ] What limits exist?
* [ ] When are file sizes checked?
* [ ] How are overlong lines handled?
* [ ] How is UTF-8 validated?
* [ ] How are hidden characters handled?
* [ ] How are filesystem objects validated?

### Analysis

* [ ] How are known levels counted?
* [ ] How are unknown levels handled?
* [ ] How are flagged entries determined?
* [ ] How are suspicious IPs determined?
* [ ] How does the time window affect analysis?

### Testing

* [ ] What is the smallest unit I can test?
* [ ] What are the normal cases?
* [ ] What are the malformed cases?
* [ ] What are the boundary cases?
* [ ] What are the security cases?
* [ ] What regression tests are required?

### Reporting

* [ ] What sections must always exist?
* [ ] What data is cleaned?
* [ ] What data remains original?
* [ ] What gets sorted?
* [ ] What happens when a section is empty?

---

# 48. The Real Completion Criteria

The project is **not** complete from a learning perspective when:

```text
./mvnw test
```

passes.

It is complete when you can take a new requirement and independently move through:

```text
Requirement
     ↓
Understand the domain
     ↓
Identify inputs/outputs
     ↓
Choose data structures
     ↓
Design responsibilities
     ↓
Choose failure model
     ↓
Implement
     ↓
Write tests
     ↓
Test boundaries
     ↓
Review
     ↓
Document
```

without needing someone to tell you what class or method to write.

---

# 49. Minimal Reference Library

You do **not** need 50 books.

Start with these.

### Java

* [ ] Oracle Java Tutorials — Java language fundamentals
  https://docs.oracle.com/javase/tutorial/

* [ ] Oracle Java API Documentation
  https://docs.oracle.com/en/java/javase/25/docs/api/

### Testing

* [ ] JUnit 5 User Guide
  https://junit.org/junit5/docs/current/user-guide/

* [ ] JaCoCo Documentation
  https://www.jacoco.org/jacoco/trunk/doc/

### Software Design

* [ ] Martin Fowler
  https://martinfowler.com/

* [ ] Effective Java — Joshua Bloch

### Security

* [ ] OWASP Input Validation Cheat Sheet
  https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html

### Git

* [ ] Pro Git
  https://git-scm.com/book/en/v2

### Commit Practices

* [ ] Conventional Commits
  https://www.conventionalcommits.org/

---

# 50. Final Rule

> **Don't ask "How do I implement TraceFinder?"**
>
> Ask:
>
> **"What Java/software-engineering concept allows me to implement this requirement?"**

For example:

```text
"Read a huge log safely"
        ↓
Streaming + resource management

"Reject invalid UTF-8"
        ↓
Character encoding + CharsetDecoder

"Continue after malformed lines"
        ↓
Error classification + exception design

"Catch the exact start/end boundary"
        ↓
Boundary value analysis

"Count entries by IP"
        ↓
Maps + grouping

"Keep code maintainable"
        ↓
Separation of responsibilities + cohesion/coupling

"Know whether my implementation works"
        ↓
Behavioral testing + boundary testing

"Handle hostile files"
        ↓
Input validation + resource limits + threat modeling
```

The objective is to reach the point where **the requirement itself tells you which fundamental concepts to reach for**.

That is the skill you want to carry into your next Java project.
