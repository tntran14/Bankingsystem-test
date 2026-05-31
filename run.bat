@echo off
chcp 65001 >nul
title Banking System

echo ============================================
echo   Banking System - Build ^& Run
echo ============================================
echo.

cd /d %~dp0

echo [1/3] Cleaning and compiling...
call mvn clean compile -q
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compile failed!
    pause
    exit /b 1
)
echo [OK] Compile successful

echo.
echo [2/3] Running tests...
call mvn test -q
if %ERRORLEVEL% neq 0 (
    echo [WARN] Some tests failed, continuing anyway...
) else (
    echo [OK] All tests passed
)

echo.
echo [3/3] Starting application...
echo ============================================
echo   App:       http://localhost:8080
echo   H2 Console: http://localhost:8080/h2-console
echo   Login:     admin / admin123
echo   Actuator:  http://localhost:8080/actuator/health
echo ============================================
echo.

call mvn spring-boot:run
pause
