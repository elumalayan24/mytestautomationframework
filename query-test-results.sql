-- PostgreSQL Query Script for Test Results
-- Run this in psql or any PostgreSQL client

-- Connect to your database first:
-- \c test_automation

-- 1. View recent test suites
SELECT 
    test_suite_id,
    engine,
    start_time,
    end_time,
    status,
    total_scenarios,
    passed_scenarios,
    failed_scenarios,
    skipped_scenarios,
    CASE 
        WHEN status = 'COMPLETED' THEN '✅'
        WHEN status = 'RUNNING' THEN '🔄'
        ELSE '❌'
    END as status_icon
FROM test_suites 
ORDER BY start_time DESC 
LIMIT 10;

-- 2. View recent scenarios with their status
SELECT 
    scenario_name,
    test_suite_id,
    engine,
    status,
    start_time,
    end_time,
    CASE 
        WHEN status = 'PASSED' THEN '✅'
        WHEN status = 'FAILED' THEN '❌'
        WHEN status = 'RUNNING' THEN '🔄'
        ELSE '⏸️'
    END as status_icon,
    feature_file,
    tags
FROM test_scenarios 
ORDER BY start_time DESC 
LIMIT 15;

-- 3. View recent logs
SELECT 
    timestamp,
    log_level,
    message,
    test_suite_id,
    scenario_name,
    engine,
    CASE 
        WHEN log_level = 'INFO' THEN 'ℹ️'
        WHEN log_level = 'ERROR' THEN '❌'
        WHEN log_level = 'DEBUG' THEN '🐛'
        ELSE '📝'
    END as level_icon
FROM test_logs 
ORDER BY timestamp DESC 
LIMIT 20;

-- 4. Summary statistics by engine
SELECT 
    engine,
    COUNT(*) as total_suites,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_suites,
    SUM(total_scenarios) as total_scenarios,
    SUM(passed_scenarios) as passed_scenarios,
    SUM(failed_scenarios) as failed_scenarios,
    ROUND(
        CASE 
            WHEN SUM(total_scenarios) > 0 
            THEN (SUM(passed_scenarios)::decimal / SUM(total_scenarios)) * 100 
            ELSE 0 
        END, 2
    ) as pass_rate_percentage
FROM test_suites 
GROUP BY engine
ORDER BY total_suites DESC;

-- 5. Recent failed scenarios
SELECT 
    scenario_name,
    test_suite_id,
    engine,
    error_message,
    start_time,
    feature_file
FROM test_scenarios 
WHERE status = 'FAILED'
ORDER BY start_time DESC 
LIMIT 10;

-- 6. Test execution timeline (last 7 days)
SELECT 
    DATE(start_time) as execution_date,
    engine,
    COUNT(*) as test_suites_run,
    SUM(total_scenarios) as total_scenarios,
    SUM(passed_scenarios) as passed_scenarios,
    SUM(failed_scenarios) as failed_scenarios
FROM test_suites 
WHERE start_time >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(start_time), engine
ORDER BY execution_date DESC, engine;
