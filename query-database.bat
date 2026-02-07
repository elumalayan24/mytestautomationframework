@echo off
echo === PostgreSQL Database Query Tool ===
echo.

REM Set classpath with all required JARs
set CLASSPATH=target/classes
set CLASSPATH=%CLASSPATH%;target\dependency\*
set CLASSPATH=%CLASSPATH%;C:\Users\elumalayan\.m2\repository\org\postgresql\postgresql\42.7.3\postgresql-42.7.3.jar
set CLASSPATH=%CLASSPATH%;C:\Users\elumalayan\.m2\repository\com\zaxxer\HikariCP\5.0.1\HikariCP-5.0.1.jar
set CLASSPATH=%CLASSPATH%;C:\Users\elumalayan\.m2\repository\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar

REM Run the query tool
java -cp "%CLASSPATH%" com.myautomation.database.DatabaseQueryTool

pause
