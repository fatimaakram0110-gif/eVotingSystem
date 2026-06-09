# 🗳 E-Voting System

A secure, desktop-based electronic voting application built with **Java Swing** for the SCD (Software Construction and Development) Lab semester project.

---

## 📋 Project Overview

The E-Voting System allows an administrator to run a single-session election for a configurable number of voters. It enforces one-vote-per-CNIC, validates all user input, and writes a full results report to disk at the end of the session.

### Key features
| Feature | Description |
|---|---|
| **Event Handling** | Button clicks, hover effects, Enter-key submission, focus-based CNIC auto-formatting |
| **Exception Handling** | Custom checked exceptions for duplicate CNICs and unknown candidates; I/O errors surfaced via dialogs |
| **Input Validation** | Regex-based name, CNIC, email validation; real-time status messages |
| **Logging** | Rotating file logger (`logs/evoting.log`) + console output via `java.util.logging` |
| **Unit Testing** | 20+ JUnit 5 test cases covering validation, vote casting, winner logic, and edge cases |
| **Clean Architecture** | Model / Service / Validation / GUI / Util layers; zero business logic in GUI classes |
| **Results Report** | `result.txt` generated with voter records, tallies, and winner announcement |

---

## 🏗 Project Structure

```
EVotingSystem/
├── pom.xml                                 # Maven build file
├── README.md
├── .gitignore
└── src/
    ├── main/java/evoting/
    │   ├── model/
    │   │   ├── Voter.java                  # Voter data object
    │   │   └── Candidate.java              # Candidate + vote count
    │   ├── validation/
    │   │   └── InputValidator.java         # All regex validation logic
    │   ├── service/
    │   │   └── VotingService.java          # Core business logic
    │   ├── util/
    │   │   ├── AppLogger.java              # Singleton logger
    │   │   └── FileResultWriter.java       # result.txt generation
    │   └── gui/
    │       ├── EVotingApp.java             # Entry point / bootstrap
    │       ├── SetupDialog.java            # Voter-count setup dialog
    │       └── VotingFormPanel.java        # Main voting form
    └── test/java/evoting/
        ├── InputValidatorTest.java         # 13 validation tests
        ├── VotingServiceTest.java          # 11 service/logic tests
        └── CandidateTest.java              # 6 model tests
```

---

## ⚙️ Prerequisites

| Tool | Minimum Version |
|---|---|
| JDK | 17 |
| Apache Maven | 3.8+ |

---

## 🚀 Setup & Run

### 1 — Clone the repository
```bash
git clone https://github.com/<your-username>/EVotingSystem.git
cd EVotingSystem
```

### 2 — Compile and run
```bash
mvn clean package -q
java -jar target/evoting-system-1.0.0.jar
```

### 3 — Run unit tests only
```bash
mvn test
```
Test results are printed to the console and saved under `target/surefire-reports/`.

---

## 🖥 How to Use

1. **Launch** the application (`java -jar …`).
2. **Enter** the number of voters in the setup dialog and click **Start Voting**.
3. For each voter, fill in:
   - Full Name (letters and spaces only)
   - CNIC (`XXXXX-XXXXXXX-X` — auto-formatted on tab-out)
   - Address
   - Email
   - Select a Candidate from the dropdown
4. Click **Submit Vote** (or press **Enter**).
5. After the last voter submits, a results dialog shows the final tally and the winner.
6. The full report is saved to **`result.txt`** in the working directory.

---

## 🧪 Test Coverage

| Test Class | Tests | What is verified |
|---|---|---|
| `InputValidatorTest` | 13 | Name/CNIC/email/address/candidate regex rules, null/blank edge cases |
| `VotingServiceTest` | 11 | Vote casting, duplicate CNIC prevention, unknown candidate, winner logic, constructor guards |
| `CandidateTest` | 6 | Initial state, vote increment, toString format, null/blank name rejection |

---

## 🏛 Design Decisions

- **Checked exceptions** (`DuplicateCnicException`, `UnknownCandidateException`) force the GUI to explicitly handle every error path — no silent failures.
- **`ValidationResult` value object** keeps validation logic pure (no Swing imports) and fully unit-testable.
- **`VotingService`** owns all mutable state; the GUI only calls service methods and reacts to results — a clean separation that makes testing trivial.
- **`AppLogger` singleton** ensures a single `FileHandler` is opened once, preventing duplicate log entries and file-handle leaks.

---

## 📄 License

Academic project — SCD Lab, 2025.
