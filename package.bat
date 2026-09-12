@echo off
setlocal enabledelayedexpansion
title RailSync - Package Runnable JAR

echo ================================================================
echo   RailSync - Package Runnable Distribution JAR
echo ================================================================

:: Check for JDK path
set "JAR_CMD=jar"
set "JAVAC_CMD=javac"
where jar >nul 2>nul
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
    ) else if exist "%USERPROFILE%\.jdks\openjdk-25\bin\jar.exe" (
        set "JAR_CMD=%USERPROFILE%\.jdks\openjdk-25\bin\jar.exe"
        set "JAVAC_CMD=%USERPROFILE%\.jdks\openjdk-25\bin\javac.exe"
    ) else if exist "%ProgramFiles%\Java\jdk-25\bin\jar.exe" (
        set "JAR_CMD=%ProgramFiles%\Java\jdk-25\bin\jar.exe"
        set "JAVAC_CMD=%ProgramFiles%\Java\jdk-25\bin\javac.exe"
    ) else (
        echo [ERROR] Java Development Kit jar and javac not found. Please install JDK 17+ or set JAVA_HOME.
        pause
        exit /b 1
    )
)

:: Compile fresh binaries first
call build.bat
if %errorlevel% neq 0 (
    echo [ERROR] Build step failed. Cannot create JAR.
    exit /b 1
)

echo [INFO] Packaging RailSync.jar with Main-Class com.railsync.Main...
"%JAR_CMD%" cfe RailSync.jar com.railsync.Main -C bin .

if %errorlevel% equ 0 (
    echo ================================================================
    echo [SUCCESS] RailSync.jar packaged successfully!
    echo.
    echo To run the application:
    echo   java -jar RailSync.jar
    echo.
    echo Or double-click RailSync.jar on systems with Java installed.
    echo ================================================================
) else (
    echo [ERROR] Packaging failed.
    exit /b 1
)
