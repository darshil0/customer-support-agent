#!/bin/bash

# Customer Support Multi-Agent System - Quick Start Script
# Version 1.0.4

set -e  # Exit on error

echo "============================================================"
echo "Customer Support Multi-Agent System v1.0.4"
echo "Quick Start Script"
echo "============================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Java is installed
echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java is not installed${NC}"
    echo "Please install Java 17 or higher from https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}✗ Java version is $JAVA_VERSION, but 17 or higher is required${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java $JAVA_VERSION detected${NC}"

# Check if Maven is installed
echo "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ Maven is not installed${NC}"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi
echo -e "${GREEN}✓ Maven detected${NC}"

# Check for Google API Key
echo "Checking environment variables..."
if [ -z "$GOOGLE_API_KEY" ]; then
    echo -e "${YELLOW}⚠ GOOGLE_API_KEY environment variable is not set${NC}"
    echo ""
    read -p "Enter your Google API Key: " api_key
    export GOOGLE_API_KEY="$api_key"
    echo -e "${GREEN}✓ API Key set for this session${NC}"
else
    echo -e "${GREEN}✓ GOOGLE_API_KEY is set${NC}"
fi

echo ""
echo "============================================================"
echo "Building the application..."
echo "============================================================"
mvn clean install

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✓ Build successful!${NC}"
echo ""

# Ask user what they want to do
echo "What would you like to do?"
echo "1) Run tests only"
echo "2) Start the application"
echo "3) Both (run tests then start application)"
echo "4) Build JAR for deployment"
read -p "Enter choice (1-4): " choice

case $choice in
    1)
        echo ""
        echo "============================================================"
        echo "Running tests..."
        echo "============================================================"
        mvn test
        echo ""
        echo -e "${GREEN}✓ Tests completed${NC}"
        ;;
    2)
        echo ""
        echo "============================================================"
        echo "Starting application..."
        echo "============================================================"
        echo ""
        mvn spring-boot:run
        ;;
    3)
        echo ""
        echo "============================================================"
        echo "Running tests..."
        echo "============================================================"
        mvn test
        
        if [ $? -eq 0 ]; then
            echo ""
            echo -e "${GREEN}✓ All tests passed${NC}"
            echo ""
            echo "============================================================"
            echo "Starting application..."
            echo "============================================================"
            echo ""
            mvn spring-boot:run
        else
            echo -e "${RED}✗ Tests failed. Please fix errors before running the application.${NC}"
            exit 1
        fi
        ;;
    4)
        echo ""
        echo "============================================================"
        echo "Building deployment JAR..."
        echo "============================================================"
        mvn clean package -DskipTests
        
        if [ $? -eq 0 ]; then
            echo ""
            echo -e "${GREEN}✓ JAR built successfully!${NC}"
            echo "Location: target/customer-support-agent-1.0.4.jar"
            echo ""
            echo "To run the JAR:"
            echo "  java -jar target/customer-support-agent-1.0.4.jar"
        else
            echo -e "${RED}✗ JAR build failed${NC}"
            exit 1
        fi
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo "============================================================"
echo "Quick Start Guide:"
echo "============================================================"
echo "• Web UI: http://localhost:8000"
echo "• API: http://localhost:8000/api"
echo "• Health: http://localhost:8000/api/health"
echo ""
echo "Sample API Calls:"
echo "  curl http://localhost:8000/api/customer/CUST001"
echo "  curl http://localhost:8000/api/health"
echo ""
echo "For more information, see README.md"
echo "============================================================"
