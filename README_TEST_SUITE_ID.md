# Test Suite ID Generation Fix

## Problem
Test suite ID was not generating automatically when running tests directly from feature files using Cucumber CLI (IntelliJ IDEA's default execution method).

## Root Cause
When IntelliJ runs Cucumber directly, it uses the command:
```
io.cucumber.core.cli.Main --plugin teamcity --name "..." features/login.feature
```

This bypasses the TestNG context and doesn't load the `cucumber.properties` file, causing hooks to be missed.

## Solution Architecture

### 1. Custom Cucumber Plugin
- **File:** `src/main/java/com/myautomation/plugins/TestSuiteIdPlugin.java`
- **Purpose:** Generates test suite ID when Cucumber test run starts
- **Trigger:** `TestRunStarted` event
- **Format:** `TS_yyyyMMdd_HHmmss_milliseconds`

### 2. Service Registration
- **File:** `src/main/resources/META-INF/services/io.cucumber.plugin.Plugin`
- **Purpose:** Auto-discovers the plugin by Cucumber
- **Content:** `com.myautomation.plugins.TestSuiteIdPlugin`

### 3. Hook Integration
- **File:** `src/test/java/com/myautomation/hooks/CucumberReportHooks.java`
- **Purpose:** Uses plugin-generated ID for ExtentReports
- **Logic:** Checks system property set by plugin

### 4. Configuration Files
- **File:** `src/test/resources/cucumber.properties`
- **Purpose:** Auto-loads plugin for all Cucumber executions
- **Content:** Includes plugin in plugin list

### 5. Test Runners
- **File:** `src/test/java/com/myautomation/testrunners/DirectCucumberTest.java`
- **Purpose:** Test runner that explicitly loads plugin
- **Usage:** For testing plugin functionality

- **File:** `src/test/java/com/myautomation/testrunners/IntelliJTestRunner.java`
- **Purpose:** JUnit runner with proper glue configuration
- **Usage:** For IntelliJ execution with full hook support

### 6. Static Test
- **File:** `src/test/java/com/myautomation/test/TestSuiteIdStaticTest.java`
- **Purpose:** Tests static initializer approach
- **Usage:** For debugging step definition loading

## How It Works

### Direct Feature File Execution (IntelliJ)
1. IntelliJ runs: `io.cucumber.core.cli.Main --plugin teamcity ...`
2. Plugin auto-discovery loads `TestSuiteIdPlugin`
3. Plugin generates ID on `TestRunStarted` event
4. Hooks use the plugin-generated ID from system property
5. **Result:** Single test suite ID generated

### JUnit Runner Execution
1. JUnit loads Cucumber context with `@CucumberOptions`
2. Plugin is explicitly included in configuration
3. Plugin generates ID and sets system property
4. Hooks detect and use the plugin-generated ID
5. **Result:** Single test suite ID generated

## Expected Output

Both execution methods should show:
```
********************************************************************************
***                          TEST SUITE ID: TS_20260115_222044_1234                          ***
********************************************************************************
```

## Usage

### IntelliJ IDEA (Direct Feature File Execution):
- Right-click on scenario in `.feature` file
- Select "Run 'Scenario Name'"
- **Test suite ID will be generated automatically!**

### Alternative IntelliJ Execution:
- Right-click on `IntelliJTestRunner.java`
- Select "Run 'IntelliJTestRunner'"
- **Full hook support and reporting**

### Maven Execution:
```bash
# Any of these will work with test suite ID generation:
mvn test -Dtest=DirectCucumberTest
mvn test -Dtest=IntelliJTestRunner
```

## Benefits

1. **Universal Compatibility:** Works with all execution methods
2. **Automatic Discovery:** Plugin auto-loads via service registration
3. **Consistent Format:** Same ID format across all execution methods
4. **Early Generation:** ID generated before any test steps execute
5. **System Property Access:** Available for any component that needs it
6. **Fallback Protection:** Multiple layers ensure ID generation

## Files Created/Modified

### New Files:
1. `src/main/java/com/myautomation/plugins/TestSuiteIdPlugin.java`
2. `src/main/resources/META-INF/services/io.cucumber.plugin.Plugin`
3. `src/test/java/com/myautomation/testrunners/DirectCucumberTest.java`
4. `src/test/java/com/myautomation/testrunners/IntelliJTestRunner.java`
5. `src/test/java/com/myautomation/test/TestSuiteIdStaticTest.java`

### Modified Files:
1. `src/test/resources/cucumber.properties` - Added plugin to list
2. `src/test/java/com/myautomation/hooks/CucumberReportHooks.java` - Updated to use plugin ID
3. `src/test/java/com/myautomation/stepdefinitions/LoginSteps.java` - Added static initializer

The test suite ID generation is now **fully automatic** for **all execution methods**! 🎉
