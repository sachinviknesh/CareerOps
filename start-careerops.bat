@echo off
setlocal
echo ========================================================
echo       Starting CareerOps (Backend + Frontend)
echo ========================================================

REM Check if backend is already listening on 8080
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo [INFO] Backend is already running on http://localhost:8080
) else (
    echo [INFO] Starting Backend (Spring Boot)...
    start "CareerOps Backend (Port 8080)" cmd /c "cd /d %~dp0careerops && java -jar target\careerops-1.0.0.jar"
)

REM Wait a moment
timeout /t 3 /nobreak >nul

REM Check if frontend is already listening on 5173
netstat -ano | findstr ":5173" | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo [INFO] Frontend is already running on http://localhost:5173
) else (
    echo [INFO] Starting Frontend (Vite)...
    start "CareerOps Frontend (Port 5173)" cmd /c "cd /d %~dp0careerops-ui && npm run dev"
)

echo ========================================================
echo   CareerOps is running:
echo   - Web Console: http://localhost:5173
echo   - Backend API: http://localhost:8080
echo   - Swagger UI:  http://localhost:8080/swagger-ui.html
echo ========================================================

start http://localhost:5173
