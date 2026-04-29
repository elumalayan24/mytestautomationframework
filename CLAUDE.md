# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Overview (Big Picture)

This project is a comprehensive, multi-engine test automation framework designed to validate web and mobile applications. The core architecture is built around the concept of decoupled test execution engines and centralized reporting.

### 1. Test Engines and Integration
The framework supports three primary testing domains, which are managed by different execution setups:
*   **Web (Selenium/Playwright):** Handles standard web browser interactions.
*   **Mobile (Appium):** Manages interactions with native mobile applications (Android/iOS) via Appium.
*   **Database:** Test results are persisted and queried against a dedicated PostgreSQL database.

### 2. Code Structure and Dependencies
*   **Source Code (`src/`):** Contains the core automation logic, step definitions, and utility classes for interacting with the target applications and the testing environment.
*   **Configuration (`src/test/resources/`):** This directory houses all test definitions, including Cucumber feature files (`.feature`) and environment-specific property files (e.g., `mobile-config.properties`).
*   **Database Layer:** Test run data is stored in the `test_automation` PostgreSQL database. This is the source of truth for all execution results.
*   **Build Tooling:** The project uses Maven (`Pom.xml`) for dependency management and build lifecycle execution.

### 3. Database Schema & Reporting
The `test_automation` PostgreSQL database maintains detailed records for every test run, structured across three key tables:
*   **`test_suites`:** Tracks overall test executions (Pass Rate, Total Scenarios, Status, Engine, Timeframe).
*   **`test_scenarios`:** Tracks the status of individual Cucumber scenarios within a suite.
*   **`test_logs`:** Stores detailed, timestamped logs (INFO/ERROR/DEBUG) for deep troubleshooting.

## Common Development Commands

All commands should typically be run from the root directory.

### 🛠 Build and Execution
*   **Clean Build:** `mvn clean install`
*   **Run All Tests (General):** `mvn test`
*   **Run Mobile Tests:** `mvn test -Dcucumber.filter.tags=@mobile` (Requires Appium server to be running).
*   **Run Single Test:** Use Cucumber feature filtering: `mvn test -Dcucumber.features="path/to/your/feature.feature"`

### 🖥️ Database Interaction
*   **Querying Results:** Use the dedicated Java Query Tool or standard PostgreSQL clients (psql, pgAdmin) with the scripts found in `query-test-results.sql`.
*   **Database Details:**
    *   Host: `localhost`
    *   Port: `5432`
    *   DB: `test_automation`

### 📱 Mobile Automation Setup
*   **Prerequisites:** Ensure Appium Server is running (`appium --port 4723`) and platform-specific tools (Android SDK/Xcode) are configured and added to the PATH.
*   **Running Mobile Tests:** Use the dedicated Maven command: `mvn test -Dcucumber.filter.tags=@mobile`

## Development Guidelines

*   **Element Locators:** Prefer using descriptive `accessibility-id` locators over volatile XPaths.
*   **Synchronization:** Always use explicit waits (`WebDriverWait`) to synchronize elements, rather than static `Thread.sleep()`.
*   **Debugging:** When a test fails, the first step should be to query the `test_logs` table in the PostgreSQL database using the `test_suite_id` and `scenario_name` for detailed context.
*   **Environment:** The project uses environment-specific configuration files (e.g., `mobile-config.properties`) which should be updated when moving between different target environments.
