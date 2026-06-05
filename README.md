# SSC CGL Online Examination Portal

A full-stack, production-grade online examination platform built with **Spring Boot 3.2**, **MySQL**, **Thymeleaf**, and **Spring Security**. It simulates the real SSC CGL (Staff Selection Commission Combined Graduate Level) exam with timed sessions, negative marking, section-wise navigation, and detailed result analytics.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Database Schema](#database-schema)
- [Question Bank](#question-bank)
- [Application Walkthrough](#application-walkthrough)
- [API Reference](#api-reference)
- [Scoring Logic](#scoring-logic)
- [Security](#security)
- [Troubleshooting](#troubleshooting)

---

## Features

- **User Registration & Login** — BCrypt-hashed passwords, session management, login audit trail
- **Real SSC CGL Exam Format** — 100 questions across 4 sections, 60-minute timer, negative marking
- **Live Exam Engine** — Question navigation, mark for review, save & next, auto-submit on timeout
- **Section-wise Progress** — Answered / Not Answered / Marked for Review / Not Visited counters per section
- **Detailed Result Page** — Section-wise scores, accuracy %, attempt %, percentile, time taken
- **Exam History** — Full attempt history with best score and averages
- **Profile Dashboard** — Stats summary, login history, personal details
- **Question Bank Auto-loader** — JSON question bank auto-imported to MySQL on startup
- **Global Error Handling** — Custom 403, 404, 500 pages

---

## Tech Stack

| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Backend      | Java 21, Spring Boot 3.2.5                      |
| Web          | Spring MVC, Thymeleaf, Thymeleaf Security Extras |
| Security     | Spring Security 6, BCrypt (strength 12)         |
| Persistence  | Spring Data JPA, Hibernate 6, MySQL 8           |
| Build        | Maven, Spring Boot Maven Plugin                 |
| Frontend     | HTML5, CSS3, Vanilla JS (exam.js, main.js, result.js) |
| Validation   | Jakarta Bean Validation                         |

---

## Project Structure

```
ssc-cgl-exam/
├── pom.xml
└── src/
    └── main/
        ├── java/com/ssccgl/exam/
        │   ├── SscCglExamApplication.java       # Entry point (@SpringBootApplication)
        │   ├── config/
        │   │   └── SecurityConfig.java          # Spring Security configuration
        │   ├── controller/
        │   │   ├── AuthController.java          # Login, register
        │   │   ├── DashboardController.java     # Dashboard, profile, history
        │   │   ├── ExamController.java          # Exam flow (instructions → attempt → result)
        │   │   └── ExamApiController.java       # REST APIs (save answer, timer, progress)
        │   ├── dto/
        │   │   ├── AnswerDto.java
        │   │   ├── ExamStateDto.java            # Full exam state sent to frontend
        │   │   ├── QuestionDto.java
        │   │   ├── QuestionReviewDto.java
        │   │   ├── RegistrationDto.java
        │   │   ├── ResultDto.java
        │   │   ├── SectionProgressDto.java
        │   │   └── SectionScoreDto.java
        │   ├── entity/
        │   │   ├── User.java
        │   │   ├── ExamSession.java             # Enum: IN_PROGRESS, SUBMITTED, TIMED_OUT, ABANDONED
        │   │   ├── ExamQuestion.java            # Maps a question to a session + position
        │   │   ├── CandidateAnswer.java         # Enum: NOT_VISITED, NOT_ANSWERED, ANSWERED,
        │   │   │                                #        MARKED_FOR_REVIEW, ANSWERED_MARKED
        │   │   ├── Question.java                # Enum: Section, Difficulty
        │   │   ├── Result.java
        │   │   └── LoginAudit.java
        │   ├── exception/
        │   │   └── GlobalExceptionHandler.java  # @ControllerAdvice — 403, 404, 500
        │   ├── repository/                      # Spring Data JPA interfaces
        │   │   ├── UserRepository.java
        │   │   ├── ExamSessionRepository.java
        │   │   ├── ExamQuestionRepository.java
        │   │   ├── CandidateAnswerRepository.java
        │   │   ├── QuestionRepository.java
        │   │   ├── ResultRepository.java
        │   │   └── LoginAuditRepository.java
        │   ├── security/
        │   │   └── CustomUserDetailsService.java
        │   └── service/
        │       ├── ExamService.java             # Core exam logic (~800 lines)
        │       ├── UserService.java
        │       └── QuestionBankLoader.java      # Runs on startup, seeds DB from JSON
        └── resources/
            ├── application.properties
            ├── question-bank/
            │   ├── general-awareness.json       # ~49 KB
            │   ├── logical-reasoning.json       # ~81 KB
            │   ├── quantitative-aptitude.json   # ~92 KB
            │   └── verbal-ability.json          # ~33 KB
            ├── static/
            │   ├── css/
            │   │   ├── main.css                 # ~53 KB — site-wide styles
            │   │   └── exam.css                 # ~7 KB  — exam UI styles
            │   └── js/
            │       ├── main.js                  # Dashboard/auth scripts
            │       ├── exam.js                  # ~25 KB — exam engine (timer, navigation, AJAX)
            │       └── result.js                # Result page charts/interactions
            └── templates/
                ├── auth/
                │   ├── login.html
                │   └── register.html
                ├── dashboard/
                │   ├── home.html
                │   ├── profile.html
                │   └── history.html
                ├── exam/
                │   ├── instructions.html
                │   ├── attempt.html
                │   ├── review.html
                │   └── result.html
                ├── error/
                │   └── error.html
                └── fragments/
                    └── layout.html
```

---

## Prerequisites

| Requirement | Minimum Version |
|-------------|----------------|
| Java JDK    | 21             |
| Maven       | 3.8+           |
| MySQL       | 8.0+           |

---

## Getting Started

### 1. Clone / Extract the project

```bash
unzip ssc-cgl-exam.zip
cd ssc-cgl-exam
```

### 2. Create the MySQL database

The application is configured to auto-create the database if it does not exist (`createDatabaseIfNotExist=true`). Just ensure the MySQL server is running and the credentials match.

```sql
-- Optional: create manually
CREATE DATABASE IF NOT EXISTS cgl;
```

### 3. Configure credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cgl?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 4. Build and run

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# OR run the JAR directly
java -jar target/exam-1.0.0.jar
```

### 5. Open in browser

```
http://localhost:8080
```

On first startup, `QuestionBankLoader` automatically imports all questions from the 4 JSON files into the `questions` table. This runs once; subsequent startups skip already-imported questions (idempotent by `json_id`).

---

## Configuration

All configuration lives in `src/main/resources/application.properties`.

### Server

```properties
server.port=8080
server.servlet.context-path=/
```

### Exam Rules

```properties
exam.duration.minutes=60
exam.questions.per.section=25     # 25 per section × 4 sections = 100 total
exam.marks.correct=2
exam.marks.wrong=-0.33
exam.total.questions=100
exam.max.marks=200
```

### Session

```properties
server.servlet.session.timeout=3600s
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

> **Note:** For production, set `server.servlet.session.cookie.secure=true` and enable HTTPS.

---

## Database Schema

Hibernate auto-generates all tables via `spring.jpa.hibernate.ddl-auto=update`. The tables are:

| Table               | Purpose                                                |
|---------------------|--------------------------------------------------------|
| `users`             | Registered candidates (username, email, BCrypt hash)  |
| `questions`         | Master question bank (imported from JSON)             |
| `exam_sessions`     | One row per exam attempt per user                     |
| `exam_questions`    | Question-to-session mapping with position             |
| `candidate_answers` | Per-question answer status and selected option        |
| `results`           | Computed scores after submission                      |
| `login_audit`       | Login history (IP, timestamp, success/failure)        |

### Key indexes

```sql
-- users
idx_users_email, idx_users_username

-- questions
idx_questions_section, idx_questions_difficulty, idx_questions_json_id

-- exam_sessions
idx_exam_sessions_user, idx_exam_sessions_status, idx_exam_sessions_start_time

-- results
idx_results_user, idx_results_session, idx_results_total_score
```

---

## Question Bank

Questions live in `src/main/resources/question-bank/` as JSON files. Each file holds an array of objects:

```json
[
  {
    "id": 1,
    "section": "GENERAL_AWARENESS",
    "topic": "History",
    "difficulty": "MEDIUM",
    "questionText": "Who was the first Governor-General of India?",
    "options": [
      "Lord Mountbatten",
      "Lord William Bentinck",
      "Warren Hastings",
      "Lord Canning"
    ],
    "correctAnswer": 0,
    "explanation": "Lord Mountbatten was the first Governor-General after independence."
  }
]
```

| Field           | Type    | Notes                                         |
|-----------------|---------|-----------------------------------------------|
| `id`            | long    | Unique within the file; used as `json_id`     |
| `section`       | string  | `GENERAL_AWARENESS`, `VERBAL_ABILITY`, `LOGICAL_REASONING`, `QUANTITATIVE_APTITUDE` |
| `topic`         | string  | Sub-topic label                               |
| `difficulty`    | string  | `EASY`, `MEDIUM`, `HARD`                      |
| `questionText`  | string  | The question                                  |
| `options`       | array   | Exactly 4 strings — A, B, C, D                |
| `correctAnswer` | int     | 0-indexed (0=A, 1=B, 2=C, 3=D)               |
| `explanation`   | string  | Optional rationale shown on result page       |

To add more questions, append to any JSON file and restart. `QuestionBankLoader` skips existing `json_id` values, so old questions won't be duplicated.

---

## Application Walkthrough

### Registration (`/register`)

- Fields: Username (3–50 chars, alphanumeric + underscore), Email, Password (min 8 chars), Confirm Password, Full Name, Mobile (10 digits), Date of Birth, Gender, State
- Validation: Server-side via Jakarta Bean Validation + custom duplicate checks
- Password is BCrypt-encoded (cost 12) before storage

### Login (`/login`)

- Spring Security form login
- On success → redirect to `/dashboard`
- On failure → `/login?error=true` with error message
- Session expiry → `/login?expired=true`
- Logout → `/logout` → `/login?logout=true`
- Every login attempt (success or failure) is recorded in `login_audit`

### Dashboard (`/dashboard`)

Shows:
- User's full name and stats (total attempts, best score)
- Question bank counts per section
- Active exam session (if any) with a "Resume" button
- Recent attempt history

### Exam Instructions (`/exam/instructions`)

- Lists exam rules, duration, marking scheme, section layout
- "Start Exam" button POSTs to `/exam/start`

### Starting the Exam (`POST /exam/start`)

`ExamService.startNewExam()` does the following in a transaction:

1. If an `IN_PROGRESS` session already exists for the user, return that (resume support)
2. Otherwise create a new `ExamSession` with `status = IN_PROGRESS`
3. **Generate question paper:** randomly select 25 questions per section using `ORDER BY RAND() LIMIT 25` in MySQL, assign global positions 0–99
4. **Initialize candidate answers:** create one `CandidateAnswer` row per `ExamQuestion`, all starting as `NOT_VISITED`
5. Redirect to `/exam/{sessionId}/attempt`

### Exam Attempt Page (`/exam/{sessionId}/attempt`)

The full `ExamStateDto` (all questions + current answers + section progress + time remaining) is serialized to JSON and embedded in the page for the JavaScript engine (`exam.js`).

The frontend handles:
- **Timer** — counts down every second, polls `/api/exam/{sessionId}/time` every 30 s as a safety net
- **Question navigation** — clicking the palette jumps to any question; section tabs switch sections
- **Save & Next** — AJAX POST to `/api/exam/{sessionId}/save-answer` with `action: "SAVE"`
- **Mark for Review** — same endpoint with `action: "MARK"` or `action: "ANSWERED_MARKED"`
- **Clear Response** — sets `selectedOption: null` with `action: "CLEAR"`
- **Auto-submit** — timer expiry triggers POST to `/api/exam/{sessionId}/auto-submit`

Answer statuses tracked per question:

| Status            | Meaning                                  | Palette Colour |
|-------------------|------------------------------------------|----------------|
| `NOT_VISITED`     | Never opened                             | Grey           |
| `NOT_ANSWERED`    | Opened but no option selected            | Red            |
| `ANSWERED`        | Option saved                             | Green          |
| `MARKED_FOR_REVIEW` | Marked, no answer                      | Purple         |
| `ANSWERED_MARKED` | Answered and marked for review           | Purple + tick  |

### Review Page (`/exam/{sessionId}/review`)

Before submitting, candidates see a summary of answered/unanswered/marked counts per section, with links to jump back to individual questions.

### Submission (`POST /exam/{sessionId}/submit`)

`ExamService.submitExam()`:

1. Sets session `status = SUBMITTED`, records `submittedAt`
2. Iterates every `CandidateAnswer`:
   - `ANSWERED` or `ANSWERED_MARKED` with correct option → `+2.0`
   - `ANSWERED` or `ANSWERED_MARKED` with wrong option → `−0.33`
   - `NOT_ANSWERED` / `NOT_VISITED` / `MARKED_FOR_REVIEW` → `0`
3. Builds section-wise scores and stores as JSON in `result.section_scores_json`
4. Computes percentile: `(count of results with lower score / total results) × 100`
5. Updates `user.best_score` (only if new score is higher) and increments `user.total_attempts`
6. Saves `Result` entity and redirects to `/exam/{sessionId}/result`

### Result Page (`/exam/{sessionId}/result`)

Displays:
- Total score / 200, score percentage
- Correct / Incorrect / Unattempted counts
- Accuracy % and Attempt %
- Percentile rank
- Time taken (formatted as MM:SS or H:MM:SS)
- Section-wise breakdown table
- Full question-by-question review with correct answers and explanations

### History (`/history`)

Lists all past attempts in descending order with:
- Attempt number, date, score, accuracy, attempt %, time taken
- Aggregate stats: best score, average score, average accuracy

### Profile (`/profile`)

- Personal details
- Aggregated stats across all attempts
- Last 10 login records (IP, timestamp, status)

---

## API Reference

All API endpoints are under `/api/exam/` and require authentication. CSRF is disabled for `/api/**`.

### `POST /api/exam/{sessionId}/save-answer`

Save or update a candidate's answer.

**Request body:**
```json
{
  "examQuestionId": 42,
  "selectedOption": 2,
  "action": "SAVE",
  "timeTaken": 35,
  "currentPosition": 5,
  "currentSection": "VERBAL_ABILITY"
}
```

`action` values: `SAVE`, `MARK`, `ANSWERED_MARKED`, `CLEAR`
`selectedOption` is 0-indexed (0=A … 3=D); omit or null to clear.

**Response:**
```json
{
  "success": true,
  "timeRemaining": 3245
}
```

On time expiry:
```json
{
  "success": false,
  "timeUp": true,
  "redirect": "/exam/42/result"
}
```

---

### `GET /api/exam/{sessionId}/time`

Get the current time remaining.

**Response:**
```json
{
  "timeRemaining": 3000,
  "submitted": false
}
```

---

### `POST /api/exam/{sessionId}/auto-submit`

Trigger auto-submission (called by frontend when timer hits zero).

**Response:**
```json
{
  "success": true,
  "redirect": "/exam/42/result"
}
```

---

### `GET /api/exam/{sessionId}/progress`

Get current answer counts.

**Response:**
```json
{
  "totalAnswered": 18,
  "totalNotAnswered": 4,
  "totalNotVisited": 72,
  "totalMarkedForReview": 3,
  "totalAnsweredMarked": 3,
  "timeRemaining": 2900
}
```

---

## Scoring Logic

```
Score = (correct × 2) + (wrong × −0.33)
```

- Max marks: **200** (100 questions × 2)
- Per section: **50** max (25 questions × 2)
- Penalty: **−0.33** per wrong answer (standard SSC CGL negative marking)
- Unattempted / Marked-only: **0** (no penalty)
- Score can go negative if many wrong answers

**Percentile calculation:**

```
Percentile = (number of results with score < your score / total results) × 100
```

---

## Security

- **Authentication:** Spring Security form login with `CustomUserDetailsService`
- **Password hashing:** BCrypt with cost factor 12
- **Session:** Single active session per user (`maximumSessions(1)`); existing session expires on new login
- **CSRF:** Enabled for all routes except `/api/**`
- **Headers:** XSS protection (`X-XSS-Protection: 1; mode=block`), `X-Content-Type-Options`, `X-Frame-Options: SAMEORIGIN`
- **Public routes:** `/`, `/home`, `/register`, `/login`, `/css/**`, `/js/**`, `/images/**`, `/error`, `/favicon.ico`
- **Login audit:** Every login attempt is persisted with IP, user agent, and outcome

---

## Troubleshooting

**App fails to start — DB connection error**
Verify MySQL is running and credentials in `application.properties` are correct. The `createDatabaseIfNotExist=true` parameter handles database creation automatically.

**`allowPublicKeyRetrieval` warning**
This is required for MySQL 8 when not using SSL locally. For production, use a proper TLS connection and remove this parameter.

**Questions not loading**
Check the logs for `QuestionBankLoader`. Ensure the JSON files exist in `src/main/resources/question-bank/`. The loader prints a summary of imported / skipped / error counts per section.

**`ORDER BY RAND()` is slow on large tables**
For production with hundreds of thousands of questions, replace the native query in `QuestionRepository.findRandomBySection` with a more efficient random-offset approach or a pre-shuffled pool.

**Port conflict**
Change `server.port` in `application.properties`.

**Session expires mid-exam**
`server.servlet.session.timeout` is set to 3600 s (1 hour) to match the exam duration. Increase it if needed, or ensure the polling calls to `/api/exam/{sessionId}/time` keep the session alive.
