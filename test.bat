@echo off
setlocal enabledelayedexpansion
title RailSync - Automated Test Suite

set "JAVA_CMD=java"
where java >nul 2>nul
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    ) else if exist "%USERPROFILE%\.jdks\openjdk-25\bin\java.exe" (
        set "JAVA_CMD=%USERPROFILE%\.jdks\openjdk-25\bin\java.exe"
    ) else if exist "%ProgramFiles%\Java\jdk-25\bin\java.exe" (
        set "JAVA_CMD=%ProgramFiles%\Java\jdk-25\bin\java.exe"
    )
)

if not exist bin\com\railsync\TestRunner.class (
    call build.bat
)

echo [INFO] Running RailSync Automated 15-Scenario Verification Suite...
"%JAVA_CMD%" -cp "bin" com.railsync.TestRunner
pause
