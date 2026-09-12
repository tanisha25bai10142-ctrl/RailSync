@echo off
setlocal enabledelayedexpansion
title RailSync - Build System

echo ================================================================
echo   RailSync - Compilation and Build Script
echo ================================================================

:: Check for JDK path
set "JAVAC_CMD=javac"
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
    ) else if exist "%USERPROFILE%\.jdks\openjdk-25\bin\javac.exe" (
        set "JAVAC_CMD=%USERPROFILE%\.jdks\openjdk-25\bin\javac.exe"
    ) else if exist "%ProgramFiles%\Java\jdk-25\bin\javac.exe" (
        set "JAVAC_CMD=%ProgramFiles%\Java\jdk-25\bin\javac.exe"
    ) else (
        echo [ERROR] Javac compiler not found. Please install JDK 17+ or set JAVA_HOME.
        pause
        exit /b 1
    )
)

echo [INFO] Using Java Compiler: %JAVAC_CMD%

if not exist bin mkdir bin
if not exist data mkdir data
if not exist data\tickets mkdir data\tickets

echo [INFO] Compiling Core Java source files...
dir /s /b src\*.java > sources.txt
"%JAVAC_CMD%" -encoding UTF-8 -d bin @sources.txt
set "COMPILE_STATUS=%errorlevel%"
if exist sources.txt del sources.txt

if %COMPILE_STATUS% equ 0 (
    echo ================================================================
    echo [SUCCESS] Compilation finished with 0 errors
    echo Classes generated in: bin\
    echo ================================================================
) else (
    echo ================================================================
    echo [ERROR] Compilation failed. Check error messages above.
    echo ================================================================
    pause
    exit /b 1
)
