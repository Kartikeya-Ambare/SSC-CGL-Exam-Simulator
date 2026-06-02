# SSC CGL Mock Test Platform 🎯

A full-stack Spring Boot web application for SSC CGL 2026 exam preparation. Students can register, take timed mock tests, and view detailed results. Admins can manage questions, users, and reports.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 · Spring Boot 3.2.5 |
| Security | Spring Security 6 |
| ORM | Spring Data JPA · Hibernate |
| Database | MySQL 8 |
| Templates | Thymeleaf 3 + Spring Security extras |
| Frontend | Bootstrap 5.3 · Bootstrap Icons · MathJax 3 |
| Build | Maven 3.9+ |
| Utilities | Lombok · Apache POI · Bucket4j |

---

## Features

- **Student Portal** — Register, login, take 100-question timed mock tests (60 min), view scored results with section-wise breakdown
- **Exam Engine** — 4 sections (General Intelligence, English, Quantitative Aptitude, General Awareness), 25 questions each, +2 / −0.5 marking
- **Admin Panel** — Add/edit/delete questions individually or via Excel bulk upload, manage users, view reports
- **Security** — CSRF protection, session management, role-based access (ROLE_USER / ROLE_ADMIN), password reset via email
- **Math Rendering** — MathJax 3 for LaTeX-formatted questions

---

## Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8.0+

---

## Setup & Run

### 1. Clone / Extract the project

```bash
unzip ssccgl.zip
cd ssccgl
```

### 2. Create the MySQL database

```sql
CREATE DATABASE CGL CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure database credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/CGL
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 4. (Optional) Configure email for password reset

```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password   # Gmail App Password, not your login password
```

If you skip this, the app runs fine — password reset emails just won't send.

### 5. Build and run

```bash
mvn spring-boot:run
```

Or build a JAR and run it:

```bash
mvn clean package -DskipTests
java -jar target/SscCgl-1.0.0.jar
```

### 6. Open in browser

```
http://localhost:8080/auth/login
```

---

## How to Start a Mock Test (Student)

1. Go to `http://localhost:8080/auth/register` and create an account
2. Log in at `/auth/login`
3. From the Dashboard, click **"Start Mock Test"** in the navbar or on the dashboard card
4. The system generates a fresh 100-question paper (25 per section)
5. Answer questions — use the section tabs and question palette to navigate
6. Submit before the 60-minute timer expires (auto-submits on timeout)
7. View your result immediately — section scores, accuracy, rank

---

## Admin Setup

On first run, `DataSeeder.java` automatically seeds:
- An admin account: `admin@ssccgl.com` / `Admin@123`
- Sample questions across all 4 sections

Log in with admin credentials, then go to:
- `/admin/questions` — manage the question bank
- `/admin/users` — view registered users
- `/admin/reports` — exam attempt reports
- `/admin/bulk-upload` — upload questions via `.xlsx`

---

## Project Structure

```
ssccgl/
├── src/main/java/com/ssccgl/
│   ├── config/          # DataSeeder (seeds DB on startup)
│   ├── controller/      # AuthController, DashboardController, ExamController, AdminController
│   ├── dto/             # RegistrationDto, ExamDtos
│   ├── entity/          # User, Exam, Question, ExamAttempt, Result, UserResponse
│   ├── enums/           # Section, Difficulty, ExamStatus, QuestionStatus
│   ├── exception/       # GlobalExceptionHandler, CustomErrorController + custom exceptions
│   ├── repository/      # Spring Data JPA repos
│   ├── security/        # SecurityConfig, CustomUserDetailsService
│   └── service/         # UserService, ExamGenerationService, ResultProcessingService, ...
│
├── src/main/resources/
│   ├── templates/
│   │   ├── auth/        # login, register, forgot-password, reset-password
│   │   ├── dashboard/   # home
│   │   ├── exam/        # exam-page, result
│   │   ├── admin/       # dashboard, questions, users, reports, bulk-upload, question-form
│   │   ├── error/       # error (404 / 403 / 500)
│   │   └── fragments/   # layout (head, navbar, scripts)
│   ├── static/
│   │   ├── css/         # main.css, exam.css
│   │   └── js/          # main.js, exam-engine.js
│   └── application.properties
│
└── pom.xml
```

---

## Bug Fixes Applied (v1.0.1)

| # | File | Issue | Fix |
|---|---|---|---|
| 1 | `fragments/layout.html` | MathJax `$` signs were parsed by Thymeleaf as expressions, crashing all pages | Added `th:inline="none"` to the MathJax config `<script>` block |
| 2 | `controller/CustomErrorController.java` | Spring Boot's `/error` route had no handler, causing a 500 loop when any error occurred | Added `CustomErrorController implements ErrorController` |
| 3 | `static/js/main.js` | File referenced in layout but missing from project, causing 404 on every page load | Created `main.js` with auto-dismiss alert utility |

---

## Configuration Reference

Key settings in `application.properties`:

| Property | Default | Description |
|---|---|---|
| `app.exam.duration-minutes` | 60 | Exam timer duration |
| `app.exam.total-questions` | 100 | Questions per exam |
| `app.exam.marks-per-correct` | 2 | Marks for correct answer |
| `app.exam.negative-marks` | 0.33 | Marks deducted per wrong answer |
| `app.exam.questions-per-section` | 25 | Questions per section |
| `server.port` | 8080 | HTTP port |

---

## License

MIT — free to use for educational purposes.
