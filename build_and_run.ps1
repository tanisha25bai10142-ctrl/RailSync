# RailSync PowerShell Build and Execution Script
$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  RailSync - Intelligent Train Reservation System" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

# Locate Java & Javac
$javaPath = "java"
$javacPath = "javac"

$defaultJdk = "$env:USERPROFILE\.jdks\openjdk-25\bin"
if (Test-Path "$defaultJdk\javac.exe") {
    $javacPath = "$defaultJdk\javac.exe"
    $javaPath = "$defaultJdk\java.exe"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javacPath = "$env:JAVA_HOME\bin\javac.exe"
    $javaPath = "$env:JAVA_HOME\bin\java.exe"
} elseif (Test-Path "$env:ProgramFiles\Java\jdk-25\bin\javac.exe") {
    $javacPath = "$env:ProgramFiles\Java\jdk-25\bin\javac.exe"
    $javaPath = "$env:ProgramFiles\Java\jdk-25\bin\java.exe"
}

Write-Host "[INFO] Using Compiler: $javacPath" -ForegroundColor Gray
Write-Host "[INFO] Using Runtime : $javaPath" -ForegroundColor Gray

# Create directories
New-Item -ItemType Directory -Force -Path "bin", "data", "data\tickets" | Out-Null

# Compile
Write-Host "[INFO] Compiling Core Java source code..." -ForegroundColor Yellow
$sources = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& $javacPath -encoding UTF-8 -d "bin" $sources

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation successful! Zero errors." -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}

# Run option
$arg = if ($args.Count -gt 0) { $args[0] } else { "" }
if ($arg -eq "--test") {
    Write-Host "[INFO] Launching Verification Suite..." -ForegroundColor Cyan
    & $javaPath -cp "bin" com.railsync.TestRunner
} elseif ($arg -eq "--cli") {
    Write-Host "[INFO] Launching CLI Mode..." -ForegroundColor Cyan
    & $javaPath -cp "bin" com.railsync.Main --cli
} else {
    Write-Host "[INFO] Launching RailSync Modern Swing GUI..." -ForegroundColor Green
    & $javaPath -cp "bin" com.railsync.Main
}
