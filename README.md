# TestIT Cucumber API Tests

A BDD-driven API test automation framework built with **Cucumber** (Java) for testing a TestIT-compatible REST API. Tests are written in Gherkin, organized by feature, and cover authentication, user management, test lifecycle, company workflows, and analytics.

---

## 📊 Test Run Summary

> Last run: **2026-03-12** &nbsp;|&nbsp; ✅ **195 / 195 scenarios passed** &nbsp;|&nbsp; ❌ 0 failed

| Feature | Endpoint(s) | Scenarios |
|---|---|:---:|
| User Registration | `POST /auth/register/` | 6 |
| User Login | `POST /auth/login/` | 10 |
| Refresh Access Token | `POST /auth/refresh/` | 5 |
| User Profile | `GET, PATCH, DELETE /auth/me/` | 12 |
| Tests | `GET, POST /api/tests/` | 17 |
| Test Detail | `GET, PATCH, DELETE /api/tests/{slug}/` | 8 |
| Take Test | `GET /api/tests/{slug}/take/`, `POST /api/tests/{slug}/verify-password/` | 5 |
| Questions | `POST, GET, PATCH, DELETE /tests/{slug}/questions/` | 25 |
| Attempts | `POST /tests/{slug}/attempts/`, `PATCH /tests/{slug}/attempts/{id}/` | 7 |
| Submit Attempt | `POST /tests/{slug}/attempts/{id}/submit/` | 5 |
| Results | `GET /tests/{slug}/results/` | 4 |
| Companies | `GET, POST, PATCH, DELETE /companies/` | 18 |
| Company Members | `GET, PUT, DELETE /companies/{id}/members/` | 13 |
| Company Invites | `GET, POST /companies/{id}/invites/`, `POST /invites/{token}/accept/` | 19 |
| Company Folders | `GET, POST, PATCH, DELETE /companies/{id}/folders/` | 17 |
| Company Tests | `GET, POST, PATCH, DELETE /tests/company/{id}/` | 17 |
| Analytics | `GET /analytics/tests/{slug}/` | 7 |
| **Total** | | **195** |

---

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [Tags](#tags)
- [Writing Tests](#writing-tests)
- [Step Definitions](#step-definitions)
- [Contributing](#contributing)

---

## Tech Stack

| Tool | Purpose |
|---|---|
| Java | Primary language |
| Cucumber (JVM) | BDD framework / Gherkin scenarios |
| JUnit / TestNG | Test runner & hooks |
| RestAssured | HTTP client for API calls |
| Maven | Build & dependency management |
| IntelliJ IDEA | Recommended IDE |

---

## Project Structure

```
TestIT_Cucumber_API_Tests/
├── src/
│   └── test/
│       ├── java/
│       │   ├── hooks/
│       │   │   └── Hooks.java            # setUp / tearDown per scenario
│       │   ├── steps/
│       │   │   ├── CommonSteps.java      # Shared steps (requests, status, field assertions)
│       │   │   ├── LoginSteps.java       # Auth / user registration steps
│       │   │   ├── RefreshSteps.java     # Token refresh steps
│       │   │   ├── TestSteps.java        # Test creation & slug management
│       │   │   ├── AnalyticsSteps.java   # Analytics-specific steps
│       │   │   ├── CompanySteps.java     # Company & multi-user steps
│       │   │   └── ...                   # Other domain step files
│       │   └── runner/
│       │       └── TestRunner.java       # Cucumber runner configuration
│       └── resources/
│           └── features/
│               └── auth/
│                   ├── 01_register.feature
│                   ├── 02_login.feature
│                   ├── 03_refresh.feature
│                   ├── 04_profile.feature
│                   ├── 05_tests.feature
│                   ├── 06_test_detail.feature
│                   ├── 07_test_take.feature
│                   ├── 08_questions.feature
│                   ├── 09_attempts.feature
│                   ├── 10_submit.feature
│                   ├── 11_results.feature
│                   ├── 12_companies.feature
│                   ├── 13_members.feature
│                   ├── 14_invites.feature
│                   ├── 15_folders.feature
│                   ├── 16_company_tests.feature
│                   └── 17_analytics.feature
├── pom.xml
└── README.md
```

---

## Prerequisites

- **Java** 11 or higher
- **Maven** 3.6+
- Access to the API under test (local or remote)

---

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/ghristov98/TestIT-Cucumber-IntelliJ.git
   cd TestIT_Cucumber_IntelliJ
   ```

2. **Install dependencies**

   ```bash
   mvn clean install -DskipTests
   ```

3. **Configure the base URL**

   Set the target API base URL in your config file or as an environment variable:

   ```properties
   base.url=https://exampractices.com/api/
   ```

---

## Running Tests

**Run all tests**

```bash
mvn test
```

**Run only smoke tests**

```bash
mvn test -Dcucumber.filter.tags="@smoke"
```

**Run full regression suite**

```bash
mvn test -Dcucumber.filter.tags="@regression"
```

**Run a specific feature**

```bash
mvn test -Dcucumber.features="src/test/resources/features/auth/01_register.feature"
```

**Generate the Cucumber HTML report**

The report is generated automatically after each run at:

```
target/cucumber-report.html
```

---

## Tags

| Tag | Description |
|---|---|
| `@smoke` | Critical happy-path scenarios (fast, minimal set) |
| `@regression` | Full regression coverage across all features |

---

## Writing Tests

Feature files live in `src/test/resources/features/auth/`. Follow the existing numbering convention when adding new features.

**Example scenario:**

```gherkin
Feature: User Registration - POST /auth/register/

  Background:
    Given the base URL is configured

  @smoke @regression
  Scenario: Successful registration with valid data
    When I send a POST request to "/auth/register/" with body:
      """
      {
        "email": "john.doe+{time}@example.com",
        "first_name": "John",
        "last_name": "Doe",
        "password": "SecurePass123!",
        "password_confirm": "SecurePass123!"
      }
      """
    Then the response status should be 201
    And the response body should have field "id"
    And the response body field "email" should equal "john.doe+{time}@example.com"
    And the response body should NOT contain "password"
```

> **Tip:** Use the `{time}` placeholder in email addresses to generate unique values per run and avoid conflicts between test executions.

---

## Step Definitions

| Class | Responsibility |
|---|---|
| `CommonSteps` | Send GET/POST/PATCH/DELETE requests; verify status codes, field existence, field values, and field errors |
| `LoginSteps` | Register users, perform login, verify auth errors |
| `RefreshSteps` | Login and persist access/refresh tokens for subsequent steps |
| `TestSteps` | Create tests, save slugs for use in later steps |
| `AnalyticsSteps` | Request and verify test analytics data |
| `CompanySteps` | Register additional users, manage company-related flows |
| `Hooks` | `@Before` / `@After` hooks for scenario setup and teardown |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Follow the existing naming convention for feature files (e.g. `18_new_feature.feature`)
4. Commit using conventional commits: `git commit -m "feat: add scenarios for X endpoint"`
5. Push and open a Pull Request

---

## License

This project is licensed under the [MIT License](LICENSE).
