# ASR-TEK Automation Framework

A Selenium + Cucumber BDD test automation framework for the ASR-TEK Portal, built with Java 11 and Maven.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Architecture](#architecture)
   - [Layer Diagram](#layer-diagram)
   - [Core Layers](#core-layers)
   - [Design Patterns](#design-patterns)
   - [Key Data Flows](#key-data-flows)
3. [Project Structure](#project-structure)
4. [Configuration](#configuration)
5. [Running Tests](#running-tests)
6. [Writing Tests](#writing-tests)
7. [Reports](#reports)

---

## Tech Stack

| Concern | Library | Version |
|---|---|---|
| Browser automation | Selenium Java | 4.15.0 |
| BDD / test DSL | Cucumber Java + JUnit | 7.14.0 / 4.13.2 |
| Driver management | WebDriverManager | 5.6.3 |
| HTML reporting | ExtentReports (Spark) | 5.1.1 |
| JSON parsing | Jackson Databind | 2.15.3 |
| Structured logging | SLF4J + Logback | 1.7.36 / 1.2.12 |
| Build | Maven | 3.x |
| Java | JDK | 11 |

---

## Architecture

### Layer Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Test Layer                               │
│  TestRunner (JUnit/Cucumber)  ·  Hooks  ·  CommonSteps          │
│  features/*.feature  ·  users/Users.json                        │
└──────────────────────────────┬──────────────────────────────────┘
                               │ uses
┌──────────────────────────────▼──────────────────────────────────┐
│                        Core Layer                               │
│  DriverManager  ·  PageContext  ·  ActionHelper                 │
│  LocatorLoader  ·  LocatorRegistry                              │
└──────────┬───────────────────────────────────┬──────────────────┘
           │ reads                             │ reads
┌──────────▼──────────┐             ┌──────────▼──────────────────┐
│   Config Layer      │             │   Locator Store             │
│   ConfigReader      │             │   locators/*.json           │
│   config.properties │             │   (loaded into JVM memory   │
└─────────────────────┘             │    at startup)              │
                                    └─────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                        Utils Layer                              │
│  ExtentReportManager  ·  WaitUtils  ·  JsonUtils                │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                       Models & Enums                            │
│  LocatorModel  ·  UserModel  ·  Browser  ·  LocatorStrategy     │
└─────────────────────────────────────────────────────────────────┘
```

---

### Core Layers

#### Config Layer

**`ConfigReader`** — Singleton. Reads `src/test/resources/config.properties` once at startup. Every key can be overridden at runtime via a system property.

```
Priority: -Dbrowser=firefox (system property) > config.properties value
```

Key accessors: `getBrowser()`, `getBaseUrl()`, `isHeadless()`, `isSessionReuse()`, `getParallelThreadCount()`.

---

#### Core Layer

**`DriverManager`** — Manages WebDriver instances using two patterns layered together:

- **Singleton per user**: `activeSessions` map guarantees exactly one live driver per `userName`.
- **Factory**: `createDriver(Browser, headless)` switches on the `Browser` enum to build Chrome, Firefox, or Edge.
- **ThreadLocal**: `threadDriver` holds the current thread's driver reference so parallel scenarios never share state.
- **Session reuse** (`session.reuse=true`): an existing driver is reused if it is alive and not past `session.timeout.minutes`. On reuse the login step is skipped.

```
getOrCreateSession(userName)
  → session.reuse=true AND alive AND not expired → return existing
  → otherwise → createDriver() → store in activeSessions + threadDriver
```

**`PageContext`** — ThreadLocal tracker for which page the browser is currently on. Every `click` action checks `navigateTo` in the locator JSON and calls `PageContext.setCurrentPage()`, which also appends to a running `navigationPath` (useful for step logs: `LoginPage → DashboardPage → StudentsPage`).

**`ActionHelper`** — Facade over Selenium. Implements `IAction`. Every method follows the same four-step contract:

```
1. Resolve locator  →  LocatorRegistry.find(currentPage, labelKey)
2. Build By         →  LocatorStrategy.fromString(locatorType).toBy(locator)
3. Wait + interact  →  WaitUtils.waitForClickable / waitForVisible
4. Update page      →  if navigateTo present → PageContext.setCurrentPage()
```

**`LocatorLoader`** — Called once from `Hooks.@Before(order=0)`. Scans the classpath `locators/` directory, parses every `*.json` file with `JsonUtils.parseLocators()`, and registers each page in `LocatorRegistry`. Idempotent — safe to call multiple times.

**`LocatorRegistry`** — Singleton in-memory store. Holds `Map<PageName, List<LocatorModel>>` for all pages loaded at startup. Implements `ILocatorProvider` so callers depend on the interface.

```java
LocatorRegistry.getInstance().find("LoginPage", "Sign In")
// → LocatorModel { locatorType="xpath", locator="//button[.='Sign In']", navigateTo="DashboardPage" }
```

---

#### Locator Store

Each page in `src/test/resources/locators/` has a corresponding JSON file. A locator entry:

```json
{
  "labelkey": "Sign In",
  "locatorType": "xpath",
  "locator": "//button[contains(text(),'Sign In')]",
  "navigateTo": "DashboardPage",
  "description": "Login submit button"
}
```

| Field | Required | Description |
|---|---|---|
| `labelkey` | yes | Human-readable key used in step definitions |
| `locator` | yes | The actual selector string |
| `locatorType` | no | `xpath` (default) \| `css` \| `id` \| `name` \| `class` \| `linkText` |
| `navigateTo` | no | Page name written to `PageContext` after interacting with this element |
| `description` | no | Free-text documentation |

---

#### Test Layer

**`TestRunner`** — JUnit 4 entry point. Wires features directory, glue packages, and output plugins. Tag filter: `not @wip` by default; override with `-Dcucumber.filter.tags="@smoke"`.

**`Hooks`** — Cucumber lifecycle in order:

| Hook | Order | Action |
|---|---|---|
| `@Before` | 0 | Print config, load all JSON locators into JVM memory (idempotent) |
| `@Before` | 1 | Create an `ExtentTest` entry for the scenario |
| `@After` | — | Screenshot on failure (if `screenshot.on.failure=true`), log pass/fail, reset `PageContext` |
| `@AfterAll` | — | Quit all browser sessions, flush ExtentReports HTML file |

**`CommonSteps`** — All Cucumber step definitions. No instance state. Uses `ThreadLocal<String> currentUserRole` to handle role-ambiguous page names (e.g. "My Profile" navigates to `AdminProfilePage` or `ProfilePage` depending on the logged-in user type).

---

#### Utils Layer

**`ExtentReportManager`** — Singleton that owns the `ExtentReports` instance. `ThreadLocal<ExtentTest>` gives each parallel thread its own test node. Produces a timestamped HTML report at `target/extent-reports/Report_<timestamp>.html`.

**`WaitUtils`** — All Selenium explicit waits (`waitForClickable`, `waitForVisible`, `waitForPageLoad`, `waitForAngular`).

**`JsonUtils`** — Parses `locators/*.json` into `List<LocatorModel>` and `users/Users.json` into `UserModel`. Also provides `jsonReaderWithNavigate(labelKey)` / `getBy(encoded)` used in `CommonSteps` for the legacy lookup path.

---

### Design Patterns

| Pattern | Where |
|---|---|
| Singleton | `ConfigReader`, `DriverManager` (per-user), `LocatorRegistry`, `ExtentReportManager` |
| Factory | `DriverManager.createDriver()` — selects Chrome / Firefox / Edge |
| Strategy | `LocatorStrategy` enum — each constant builds its own `By` object |
| Facade | `ActionHelper` — one method per interaction type, hides Selenium complexity |
| ThreadLocal | `DriverManager`, `PageContext`, `ExtentReportManager` — parallel test isolation |

---

### Key Data Flows

#### Startup

```
mvn test
  → Surefire forks JVMs (parallel.thread.count)
  → TestRunner boots Cucumber
  → Hooks.@Before(order=0)
      → ConfigReader.getInstance()   (loads config.properties)
      → LocatorLoader.loadAll()
          → scan classpath locators/*.json
          → JsonUtils.parseLocators() per file
          → LocatorRegistry.register(pageName, locators)
  → Hooks.@Before(order=1)
      → ExtentReportManager.createTest(scenarioName)
```

#### Login Step

```
Given I am logged in as an "Admin" User
  → JsonUtils.findUserByType("Admin")       (Users.json → UserModel)
  → DriverManager.getOrCreateSession(userName)
      → session.reuse=true + alive + not expired → reuse (skip login)
      → else → WebDriverManager.setup() → new ChromeDriver(options)
  → driver.get(baseUrl + "/login")
  → WaitUtils.waitForPageLoad + waitForAngular
  → PageContext.setCurrentPage("LoginPage")
  → typeIntoField("Username", ...) → JsonUtils.jsonReaderWithNavigate("Username")
      → LocatorRegistry.find("LoginPage", "Username")
      → WaitUtils.waitForVisible → el.sendKeys(value)
  → clickLabel("Sign In")
      → LocatorRegistry.find("LoginPage", "Sign In")
      → WaitUtils.waitForClickable → el.click()
      → navigateTo="DashboardPage" → PageContext.setCurrentPage("DashboardPage")
```

#### Page Navigation via `navigateTo`

```
When I click on "Students" option in side menu
  → PageContext.setCurrentPage("SidebarPage")     (explicit context switch)
  → clickLabel("Students")
      → LocatorRegistry.find("SidebarPage", "Students")
      → WaitUtils.waitForClickable → el.click()
      → navigateTo="StudentsPage" → PageContext.setCurrentPage("StudentsPage")

Then I should be on the "StudentsPage" page
  → PageContext.getCurrentPageTitle() == "StudentsPage" ✔
```

---

## Project Structure

```
automation-framework/
├── pom.xml
└── src/
    ├── main/java/com/asrtek/automation/
    │   ├── config/
    │   │   └── ConfigReader.java          # Singleton config reader
    │   ├── core/
    │   │   ├── ActionHelper.java          # Facade for all UI actions
    │   │   ├── DriverManager.java         # WebDriver factory + session manager
    │   │   ├── DriverUtil.java            # Thin wrapper — getDriver() shorthand
    │   │   ├── LocatorLoader.java         # Startup JSON → LocatorRegistry loader
    │   │   ├── LocatorRegistry.java       # In-memory locator store (Singleton)
    │   │   └── PageContext.java           # ThreadLocal current-page tracker
    │   ├── enums/
    │   │   ├── Browser.java               # CHROME | FIREFOX | EDGE
    │   │   ├── Environment.java           # QA | STAGING | PROD
    │   │   └── LocatorStrategy.java       # xpath | css | id | name | class | linkText
    │   ├── interfaces/
    │   │   ├── IAction.java               # Contract for UI actions
    │   │   ├── ILocatorProvider.java      # Contract for locator registry
    │   │   └── IPage.java                 # Base page contract
    │   ├── models/
    │   │   ├── LocatorModel.java          # One locator entry from JSON
    │   │   └── UserModel.java             # Test user credentials
    │   └── utils/
    │       ├── ExtentReportManager.java   # Singleton HTML report manager
    │       ├── JsonUtils.java             # JSON parsing helpers
    │       └── WaitUtils.java             # Selenium explicit wait helpers
    └── test/
        ├── java/com/asrtek/automation/
        │   ├── hooks/
        │   │   └── Hooks.java             # Cucumber @Before / @After lifecycle
        │   ├── runner/
        │   │   └── TestRunner.java        # JUnit 4 Cucumber entry point
        │   └── steps/
        │       └── CommonSteps.java       # All Given / When / Then step definitions
        └── resources/
            ├── config.properties          # Runtime configuration
            ├── features/
            │   └── Portal.feature         # All BDD scenarios
            ├── locators/
            │   ├── LoginPage.json
            │   ├── DashboardPage.json
            │   ├── StudentsPage.json
            │   └── ...                    # One JSON file per page
            └── users/
                └── Users.json             # Test user credentials
```

---

## Configuration

`src/test/resources/config.properties`:

```properties
browser=chrome                  # chrome | firefox | edge
headless=false                  # true | false
env=qa                          # qa | staging | prod

url.qa=https://qa.asrtek.com
url.staging=https://staging.asrtek.com
url.prod=https://prod.asrtek.com

browser.implicit.wait=10        # seconds
browser.page.load.timeout=60    # seconds
screenshot.on.failure=true

session.reuse=true              # reuse open browser across scenarios for same user
session.timeout.minutes=30

parallel.thread.count=1
```

Any key can be overridden at the CLI without touching the file:

```bash
mvn test -Dbrowser=firefox -Denv=staging -Dheadless=true
```

---

## Running Tests

```bash
# Run all tests (default: qa env, chrome, headed)
mvn test

# Run only @smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Run against staging in headless Firefox with 3 parallel threads
mvn test -Pstaging -Dbrowser=firefox -Dheadless=true -Dparallel.thread.count=3

# Run a single tag set
mvn test -Dcucumber.filter.tags="@admin and @navigation"
```

Available Maven profiles: `qa` (default), `staging`, `prod`.

---

## Writing Tests

**1. Add a locator** — create or edit the page JSON in `src/test/resources/locators/`:

```json
[
  {
    "labelkey": "Save Changes",
    "locatorType": "xpath",
    "locator": "//button[@data-testid='save']",
    "navigateTo": "ProfilePage",
    "description": "Saves profile changes and navigates back to Profile page"
  }
]
```

**2. Write a scenario** — use the existing step vocabulary in `Portal.feature`:

```gherkin
@admin @profile
Scenario: Admin saves profile changes
  Given I am logged in as an "Admin" User
  When I click on "My Profile" option in side menu
  When I click on "Save Changes" button
  Then I should be on the "ProfilePage" page
```

**3. Run it** — no new Java code needed if your scenario uses only the existing step definitions.

**Available steps:**

| Step | Example |
|---|---|
| `Given I am logged in as an? "{role}" User` | `Given I am logged in as an "Admin" User` |
| `Given I navigate to the login page` | |
| `When I enter "{value}" into "{field}" field` | `When I enter "John" into "Search" field` |
| `When I click on "{label}" button` | `When I click on "Save" button` |
| `When I click on "{label}" link` | |
| `When I click on "{label}" tab` | |
| `When I click on "{option}" option in side menu` | `When I click on "Students" option in side menu` |
| `When I click on "{option}" from Dashboard` | `When I click on "Approvals" from Dashboard` |
| `When I select "{value}" from "{dropdown}" dropdown` | `When I select "Active" from "Status" dropdown` |
| `Then I should see "{label}"` | `Then I should see "Welcome"` |
| `Then "{label}" should display "{text}"` | `Then "Page Title" should display "Students"` |
| `Then I should be on the "{page}" page` | `Then I should be on the "StudentsPage" page` |

---

## Reports

After each run two reports are generated:

| Report | Location | Notes |
|---|---|---|
| Cucumber HTML | `target/cucumber-reports/cucumber-report.html` | Step-level pass/fail |
| Cucumber JSON | `target/cucumber-reports/cucumber-report.json` | Machine-readable, usable with CI dashboards |
| ExtentReports HTML | `target/extent-reports/Report_<timestamp>.html` | Rich dark-theme report with screenshots on failure |
