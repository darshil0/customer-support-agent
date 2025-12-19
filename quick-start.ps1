# Customer Support Multi-Agent System - Quick Start Script (PowerShell)
# Version 1.0.4

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Customer Support Multi-Agent System v1.0.4" -ForegroundColor Cyan
Write-Host "Quick Start Script" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Java is installed
Write-Host "Checking Java installation..."
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($null -eq $javaVersion) { throw "Java not found" }
    Write-Host "✓ Java detected" -ForegroundColor Green
} catch {
    Write-Host "✗ Java is not installed" -ForegroundColor Red
    Write-Host "Please install Java 17 or higher from https://adoptium.net/"
    exit 1
}

# Check if Maven is installed
Write-Host "Checking Maven installation..."
try {
    $mvnVersion = mvn -version 2>&1 | Select-Object -First 1
    if ($null -eq $mvnVersion) { throw "Maven not found" }
    Write-Host "✓ Maven detected" -ForegroundColor Green
} catch {
    Write-Host "✗ Maven is not installed" -ForegroundColor Red
    Write-Host "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
}

# Check for Google API Key
Write-Host "Checking environment variables..."
if ([string]::IsNullOrWhiteSpace($env:GOOGLE_API_KEY)) {
    Write-Host "⚠ GOOGLE_API_KEY environment variable is not set" -ForegroundColor Yellow
    $apiKey = Read-Host "Enter your Google API Key"
    $env:GOOGLE_API_KEY = $apiKey
    Write-Host "✓ API Key set for this session" -ForegroundColor Green
} else {
    Write-Host "✓ GOOGLE_API_KEY is set" -ForegroundColor Green
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Building the application..."
Write-Host "============================================================" -ForegroundColor Cyan
mvn clean install

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Build failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✓ Build successful!" -ForegroundColor Green
Write-Host ""

# Ask user what they want to do
Write-Host "What would you like to do?"
Write-Host "1) Run tests only"
Write-Host "2) Start the application"
Write-Host "3) Both (run tests then start application)"
Write-Host "4) Build JAR for deployment"
$choice = Read-Host "Enter choice (1-4)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "Running tests..."
        Write-Host "============================================================" -ForegroundColor Cyan
        mvn test
        Write-Host ""
        Write-Host "✓ Tests completed" -ForegroundColor Green
    }
    "2" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "Starting application..."
        Write-Host "============================================================" -ForegroundColor Cyan
        mvn spring-boot:run
    }
    "3" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "Running tests..."
        Write-Host "============================================================" -ForegroundColor Cyan
        mvn test
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✓ All tests passed" -ForegroundColor Green
            Write-Host ""
            Write-Host "============================================================" -ForegroundColor Cyan
            Write-Host "Starting application..."
            Write-Host "============================================================" -ForegroundColor Cyan
            mvn spring-boot:run
        } else {
            Write-Host "✗ Tests failed. Please fix errors before running the application." -ForegroundColor Red
            exit 1
        }
    }
    "4" {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "Building deployment JAR..."
        Write-Host "============================================================" -ForegroundColor Cyan
        mvn clean package -DskipTests
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✓ JAR built successfully!" -ForegroundColor Green
            Write-Host "Location: target/customer-support-agent-1.0.4.jar"
            Write-Host ""
            Write-Host "To run the JAR:"
            Write-Host "  java -jar target/customer-support-agent-1.0.4.jar"
        } else {
            Write-Host "✗ JAR build failed" -ForegroundColor Red
            exit 1
        }
    }
    default {
        Write-Host "Invalid choice" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Quick Start Guide:" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "• Web UI: http://localhost:8000"
Write-Host "• API: http://localhost:8000/api"
Write-Host "• Health: http://localhost:8000/api/health"
Write-Host ""
Write-Host "Sample API Calls:"
Write-Host "  Invoke-RestMethod -Uri http://localhost:8000/api/customer/CUST001"
Write-Host "  Invoke-RestMethod -Uri http://localhost:8000/api/health"
Write-Host ""
Write-Host "For more information, see README.md"
Write-Host "============================================================" -ForegroundColor Cyan
