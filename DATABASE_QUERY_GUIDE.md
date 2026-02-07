# How to Query Test Results from PostgreSQL Database

## Quick Start

### 1. Using psql (PostgreSQL Command Line)

```bash
# Connect to PostgreSQL
psql -U postgres -d test_automation

# Run the query script
\i query-test-results.sql
```

### 2. Using pgAdmin or DBeaver

1. Connect to your PostgreSQL server
2. Select the `test_automation` database
3. Open a new query window
4. Copy and paste queries from `query-test-results.sql`

### 3. Using the Java Query Tool

```bash
# Compile and run the Java query tool
javac -cp "target/dependency/*" src/main/java/com/myautomation/database/DatabaseQueryTool.java -d target/classes
java -cp "target/classes;target/dependency/*;C:/Users/elumalayan/.m2/repository/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar" com.myautomation.database.DatabaseQueryTool
```

## Database Schema

### Tables:

1. **test_suites** - Overall test execution records
   - `test_suite_id` - Unique identifier (e.g., TS_20260207_211552_2381)
   - `engine` - selenium/playwright/mobile
   - `start_time`, `end_time` - Execution timestamps
   - `status` - RUNNING/COMPLETED
   - `total_scenarios`, `passed_scenarios`, `failed_scenarios`, `skipped_scenarios`

2. **test_scenarios** - Individual scenario results
   - `scenario_name` - Name of the Cucumber scenario
   - `test_suite_id` - Links to test_suites table
   - `feature_file` - Source feature file
   - `tags` - Cucumber tags
   - `status` - PASSED/FAILED/RUNNING
   - `error_message` - Error details if failed

3. **test_logs** - Detailed execution logs
   - `log_level` - INFO/ERROR/DEBUG
   - `message` - Log message
   - `timestamp` - When the log was created
   - `test_suite_id`, `scenario_name` - Context information

## Common Queries

### Get Latest Test Suite
```sql
SELECT * FROM test_suites 
ORDER BY start_time DESC 
LIMIT 1;
```

### Get Failed Scenarios
```sql
SELECT scenario_name, error_message, start_time 
FROM test_scenarios 
WHERE status = 'FAILED' 
ORDER BY start_time DESC;
```

### Get Test Suite Summary
```sql
SELECT 
    test_suite_id,
    engine,
    status,
    total_scenarios,
    passed_scenarios,
    failed_scenarios,
    ROUND((passed_scenarios::decimal / total_scenarios) * 100, 2) as pass_rate
FROM test_suites 
ORDER BY start_time DESC;
```

### Filter by Engine
```sql
-- Selenium tests only
SELECT * FROM test_suites WHERE engine = 'selenium';

-- Playwright tests only  
SELECT * FROM test_suites WHERE engine = 'playwright';

-- Mobile tests only
SELECT * FROM test_suites WHERE engine = 'mobile';
```

### Filter by Date Range
```sql
SELECT * FROM test_suites 
WHERE start_time >= '2026-02-07' 
  AND start_time < '2026-02-08'
ORDER BY start_time DESC;
```

## Database Connection Details

- **Host**: localhost
- **Port**: 5432
- **Database**: test_automation
- **Username**: postgres
- **Password**: admin (as configured in database.properties)

## Tips

1. **Real-time monitoring**: Use the `test_logs` table to see what's happening during test execution
2. **Performance analysis**: Compare pass rates between different engines (selenium vs playwright)
3. **Error tracking**: Use the `error_message` field in `test_scenarios` to debug failures
4. **Trend analysis**: Use date-based queries to track test performance over time

## Example Output

When you run the query tool, you'll see output like:

```
=== PostgreSQL Database Query Tool ===

✅ PostgreSQL driver loaded
✅ Connected to PostgreSQL database

📊 DATABASE TABLES:
============================================================
📋 test_suites: 3 records
📋 test_scenarios: 15 records
📋 test_logs: 45 records

🏁 TEST SUITES:
============================================================
🔸 Suite: TS_20260207_211552_2381
   Engine: playwright
   Status: COMPLETED
   Start: 2026-02-07 21:15:52.387987
   End: 2026-02-07 21:15:57.375117
   Results: 1 passed, 1 failed, 0 skipped
```
