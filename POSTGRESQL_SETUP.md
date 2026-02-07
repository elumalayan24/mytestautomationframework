# PostgreSQL Database Integration Setup Guide

## Overview
Your test automation framework now integrates with PostgreSQL to store test execution details, including test suite ID, scenario names, engine types (Selenium/Playwright/Appium), test results, and logs.

## 🚀 Quick Setup

### 1. PostgreSQL Database Setup
```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE test_automation;
CREATE USER automation_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE test_automation TO automation_user;
```

### 2. Update Configuration
Edit `src/test/resources/database.properties`:
```properties
# PostgreSQL Connection Settings
database.url=jdbc:postgresql://localhost:5432/test_automation
database.username=automation_user
database.password=your_password
```

### 3. Run Tests
```bash
# Run Selenium tests
mvn test -Dengine=selenium

# Run Playwright tests  
mvn test -Dengine=playwright

# Run Mobile tests
mvn test -Dengine=mobile
```

## 📊 Database Schema

### Tables Created Automatically

#### **test_suites**
- `test_suite_id` - Unique identifier for test run
- `engine` - selenium/playwright/mobile
- `status` - RUNNING/COMPLETED
- `start_time` / `end_time`
- `total_scenarios` / `passed_scenarios` / `failed_scenarios`

#### **test_scenarios**  
- `scenario_name` - Cucumber scenario name
- `feature_file` - Feature file path
- `tags` - Scenario tags
- `status` - RUNNING/PASSED/FAILED
- `error_message` - Failure details

#### **test_logs**
- `log_level` - INFO/WARNING/ERROR/DEBUG
- `message` - Log message
- `timestamp` - Log timestamp
- `engine` - Test engine used

## 🔍 Database Console Application

Run the console to view test data:
```bash
java -cp target/classes:target/test-classes com.myautomation.database.DatabaseConsole
```

**Console Options:**
1. View Recent Test Suites
2. View Test Suite Details  
3. View Scenario History
4. View Test Logs
5. Search by Test Suite ID
6. Exit

## 📈 What Gets Tracked

### **Test Suite Level**
- ✅ Unique Test Suite ID (TS_YYYYMMDD_HHMMSS_XXXX)
- ✅ Engine type (selenium/playwright/mobile)
- ✅ Start/end timestamps
- ✅ Total scenario counts
- ✅ Pass/fail/skip statistics

### **Scenario Level**
- ✅ Scenario name and feature file
- ✅ All tags (@selenium, @playwright, @mobile)
- ✅ Individual start/end times
- ✅ Pass/fail status
- ✅ Error messages for failures

### **Logging Level**
- ✅ Test suite initialization
- ✅ Scenario start/completion
- ✅ Screenshot captures
- ✅ Error conditions
- ✅ Engine-specific events

## 🎯 Sample SQL Queries

### **View Recent Test Runs**
```sql
SELECT test_suite_id, engine, status, 
       total_scenarios, passed_scenarios, failed_scenarios,
       start_time, end_time
FROM test_suites 
ORDER BY start_time DESC 
LIMIT 10;
```

### **Test Success Rate by Engine**
```sql
SELECT engine, 
       COUNT(*) as total_runs,
       AVG(passed_scenarios::float / total_scenarios * 100) as success_rate
FROM test_suites 
WHERE status = 'COMPLETED'
GROUP BY engine;
```

### **Failed Scenarios Analysis**
```sql
SELECT scenario_name, COUNT(*) as failure_count,
       MAX(end_time) as last_failure
FROM test_scenarios 
WHERE status = 'FAILED'
GROUP BY scenario_name
ORDER BY failure_count DESC;
```

### **Recent Test Logs**
```sql
SELECT test_suite_id, log_level, message, timestamp
FROM test_logs 
ORDER BY timestamp DESC 
LIMIT 50;
```

## 🔧 Configuration Options

### **Connection Pool Settings**
```properties
database.pool.maxSize=10
database.pool.minIdle=2
database.pool.idleTimeout=300000
database.pool.connectionTimeout=20000
database.pool.maxLifetime=1200000
```

### **Database URL Options**
```properties
# Local PostgreSQL
database.url=jdbc:postgresql://localhost:5432/test_automation

# Remote PostgreSQL
database.url=jdbc:postgresql://db-server:5432/test_automation

# With SSL
database.url=jdbc:postgresql://localhost:5432/test_automation?ssl=true
```

## 🚨 Error Handling

### **Common Issues & Solutions**

1. **Connection Failed**
   ```
   Check PostgreSQL is running
   Verify database exists: CREATE DATABASE test_automation;
   Check credentials in database.properties
   ```

2. **Table Creation Failed**
   ```
   Ensure user has CREATE TABLE permissions
   Check database connection
   Verify PostgreSQL version (9.6+)
   ```

3. **Permission Denied**
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE test_automation TO automation_user;
   GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO automation_user;
   ```

## 📱 Integration with Existing Features

### **Automatic Integration**
- ✅ Works with all engines (Selenium/Playwright/Mobile)
- ✅ Captures screenshots in database logs
- ✅ Tracks test suite IDs across reports
- ✅ No changes needed to existing tests

### **Reporting Integration**
- ✅ ExtentReports + Database logging
- ✅ Cucumber HTML reports + Database tracking
- ✅ Test suite ID appears in all reports
- ✅ Database console for detailed analysis

## 🔄 Backup and Maintenance

### **Backup Database**
```bash
pg_dump test_automation > backup_$(date +%Y%m%d).sql
```

### **Clean Old Data**
```sql
-- Delete test runs older than 30 days
DELETE FROM test_suites 
WHERE start_time < NOW() - INTERVAL '30 days';
```

### **Monitor Database Size**
```sql
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public';
```

## 🎉 Benefits

1. **Complete Test History** - Every test run tracked
2. **Trend Analysis** - Success rates over time
3. **Failure Patterns** - Identify flaky tests
4. **Performance Metrics** - Execution time tracking
5. **Audit Trail** - Full compliance logging
6. **Multi-Engine Support** - Unified reporting
7. **Real-time Monitoring** - Live test status

Your framework now provides comprehensive database integration with PostgreSQL! 🚀
