@echo off
setlocal EnableDelayedExpansion

:: =============================================================================
::  Eurovision 2025 — One-Command Setup Script (Windows)
::  Usage: start.bat [--seed] [--reset] [--stop] [--logs]
::
::  --seed   : run the seed script after startup (loads scripts/seed-data.json)
::  --reset  : tear down containers + wipe DB volume before starting fresh
::  --stop   : stop and remove all containers (keeps DB volume)
::  --logs   : tail logs after startup instead of returning to shell
::  --frontend : also start the frontend dev server on http://localhost:3000
::
::  Requirements: Docker Desktop running, Python 3 installed
:: =============================================================================

:: ── Parse arguments ──────────────────────────────────────────────────────────
set DO_SEED=false
set DO_RESET=false
set DO_STOP=false
set DO_LOGS=false
set DO_FRONTEND=false

for %%A in (%*) do (
    if "%%A"=="--seed"  set DO_SEED=true
    if "%%A"=="--reset" set DO_RESET=true
    if "%%A"=="--stop"  set DO_STOP=true
    if "%%A"=="--logs"  set DO_LOGS=true
    if "%%A"=="--frontend" set DO_FRONTEND=true
)

:: ── Check we are in the project root ─────────────────────────────────────────
if not exist "docker-compose.yml" (
    echo [ERROR] Run this script from the project root ^(where docker-compose.yml lives^)
    exit /b 1
)

:: ── Check dependencies ───────────────────────────────────────────────────────
echo [INFO]  Checking dependencies...

docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker not found. Install Docker Desktop: https://docs.docker.com/get-docker/
    exit /b 1
)

docker compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose not found. Make sure Docker Desktop is up to date.
    exit /b 1
)

python --version >nul 2>&1
if errorlevel 1 (
    python3 --version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Python 3 not found. Install from https://www.python.org/downloads/
        exit /b 1
    )
    set PYTHON=python3
) else (
    set PYTHON=python
)

:: Check Docker Desktop is actually running (not just installed)
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is installed but not running.
    echo         Please start Docker Desktop and wait for it to fully load, then try again.
    exit /b 1
)

echo [OK]    All dependencies found

:: ── --stop ───────────────────────────────────────────────────────────────────
if "%DO_STOP%"=="true" (
    echo [INFO]  Stopping containers...
    docker compose down
   echo [INFO]  Stopping frontend server if running...
       taskkill /FI "WINDOWTITLE eq Eurovision Frontend*" /F >nul 2>&1
       echo [OK]    All services stopped. DB volume preserved.
       echo         Use --reset to wipe the database volume.
       exit /b 0
)

:: ── --reset ──────────────────────────────────────────────────────────────────
if "%DO_RESET%"=="true" (
    echo [WARN]  RESET: this will destroy all containers and the MySQL data volume!
    set /p CONFIRM="Are you sure? [y/N] "
    if /i "!CONFIRM!" neq "y" (
        echo [INFO]  Reset cancelled.
        exit /b 0
    )
    echo [INFO]  Tearing down containers and volumes...
    docker compose down -v --remove-orphans
    echo [OK]    Clean slate ready
)

:: ── Check mvnw wrapper exists ────────────────────────────────────────────────
if not exist "mvnw" (
    echo.
    echo [WARN]  mvnw not found — Dockerfile requires it to build the app.
    echo.
    echo         Fix: run this in PowerShell or Git Bash:
    echo           curl https://start.spring.io/starter.zip -d type=maven-project -d dependencies=web -d javaVersion=17 -o tmp.zip
    echo           tar -xf tmp.zip mvnw mvnw.cmd .mvn/
    echo           del tmp.zip
    echo.
    set /p CONFIRM="Continue anyway? Docker build will fail. [y/N] "
    if /i "!CONFIRM!" neq "y" exit /b 1
)

:: ── Build and start ──────────────────────────────────────────────────────────
echo.
echo ╔══════════════════════════════════════════╗
echo ║   Eurovision 2025 — Starting up...       ║
echo ╚══════════════════════════════════════════╝
echo.

echo [INFO]  Building Docker images and starting containers...
docker compose up --build -d
if errorlevel 1 (
    echo [ERROR] Docker Compose failed. Check the output above.
    exit /b 1
)

echo [OK]    Containers started
echo.
echo [INFO]  Container status:
docker compose ps

:: ── Wait for Spring Boot ─────────────────────────────────────────────────────
echo.
echo [INFO]  Waiting for Spring Boot to be ready at http://localhost:8080 ...

set MAX_WAIT=120
set ELAPSED=0
set READY=false

:WAIT_LOOP
    curl -sf http://localhost:8080/api/countries >nul 2>&1
    if not errorlevel 1 (
        set READY=true
        goto WAIT_DONE
    )
    if !ELAPSED! geq %MAX_WAIT% goto WAIT_DONE
    <nul set /p =.
    timeout /t 3 /nobreak >nul
    set /a ELAPSED+=3
    goto WAIT_LOOP

:WAIT_DONE
if "%READY%"=="false" (
    echo.
    echo [ERROR] Spring Boot did not start within %MAX_WAIT%s.
    echo         Check logs with:  docker compose logs app
    exit /b 1
)
echo.
echo [OK]    Spring Boot is up! ^(%ELAPSED%s^)

:: ── Seed ─────────────────────────────────────────────────────────────────────
if "%DO_SEED%"=="true" (
    echo.
    echo [INFO]  Running seed script from scripts/seed-data.json ...
    %PYTHON% scripts/seed.py --host http://localhost:8080 --retries 3
    if errorlevel 1 (
        echo [WARN]  Seed script exited with errors. Check output above.
    )
)


:: ── Frontend server ───────────────────────────────────────────────────────────
if "%DO_FRONTEND%"=="true" (
    if not exist "frontend" (
        echo [WARN]  frontend\ directory not found — skipping frontend server.
    ) else (
        echo.
        echo [INFO]  Starting frontend server on http://localhost:3000 ...
        :: Kill existing instance if any
        for /f "tokens=5" %%a in ('netstat -aon ^| find ":3000 " ^| find "LISTENING"') do (
            taskkill /PID %%a /F >nul 2>&1
        )
        :: Open a new titled window running the server
        start "Eurovision Frontend" /min cmd /c "cd frontend && %PYTHON% -m http.server 3000"
        timeout /t 2 /nobreak >nul
        echo [OK]    Frontend server started in background window
    )
)

:: ── Done ─────────────────────────────────────────────────────────────────────
echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║  OK  Eurovision 2025 is LIVE                         ║
echo ╠══════════════════════════════════════════════════════╣
echo ║  Frontend  : http://localhost:3000                   ║
echo ║  API Base  : http://localhost:8080/api               ║
echo ╠══════════════════════════════════════════════════════╣
echo ║  API endpoints:                                      ║
echo ║    Countries : http://localhost:8080/api/countries   ║
echo ║    Songs     : http://localhost:8080/api/songs       ║
echo ║    Shows     : http://localhost:8080/api/shows       ║
echo ║    Votes     : http://localhost:8080/api/votes       ║
echo ║    Scores    : http://localhost:8080/api/scores      ║
echo ╠══════════════════════════════════════════════════════╣
echo ║  MySQL  : localhost:3306  db=eurovision_db           ║
echo ║  User/pass : eurovision / eurovisionpass             ║
echo ╠══════════════════════════════════════════════════════╣
echo ║  To stop all   : start.bat --stop                    ║
echo ║  To reset DB   : start.bat --reset                   ║
echo ║  To reseed     : python scripts/seed.py              ║
echo ║  App logs      : docker compose logs -f app          ║
echo ╚══════════════════════════════════════════════════════╝


:: ── --logs ───────────────────────────────────────────────────────────────────
if "%DO_LOGS%"=="true" (
    echo.
    echo [INFO]  Tailing logs ^(Ctrl+C to exit^)...
    docker compose logs -f
)

endlocal
