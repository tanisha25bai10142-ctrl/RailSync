@echo off
setlocal enabledelayedexpansion
title RailSync - Train Reservation System

:: Check for Java runtime
set "JAVA_CMD=java"
where java >nul 2>nul
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    ) else if exist "%USERPROFILE%\.jdks\openjdk-25\bin\java.exe" (
        set "JAVA_CMD=%USERPROFILE%\.jdks\openjdk-25\bin\java.exe"
    ) else if exist "%ProgramFiles%\Java\jdk-25\bin\java.exe" (
        set "JAVA_CMD=%ProgramFiles%\Java\jdk-25\bin\java.exe"
    ) else (
        echo [ERROR] Java runtime not found. Please install JDK 17+ or set JAVA_HOME.
        pause
        exit /b 1
    )
)

if not exist bin\com\railsync\Main.class (
    echo [INFO] Binaries not found. Building project...
    call build.bat
    if %errorlevel% neq 0 exit /b 1
)

echo [INFO] Launching RailSync...
"%JAVA_CMD%" -cp "bin" com.railsync.Main %*
