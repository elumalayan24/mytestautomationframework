@echo off
echo Setting up JDK environment...

set JAVA_HOME=C:\JDK\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java Home: %JAVA_HOME%
java -version

echo Environment setup complete!
pause
