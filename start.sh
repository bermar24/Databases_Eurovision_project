#!/usr/bin/env bash
# =============================================================================
#  Eurovision 2025 — One-Command Setup Script
#  Usage: ./start.sh [--seed] [--reset] [--stop] [--logs]
#
#  --seed   : run the seed script after startup (loads scripts/seed-data.json)
#  --reset  : tear down containers + wipe DB volume before starting fresh
#  --stop   : stop and remove all containers (keeps DB volume)
#  --logs   : tail logs after startup instead of returning to shell
# =============================================================================

set -euo pipefail

# ── Colours ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ── Parse args ───────────────────────────────────────────────────────────────
DO_SEED=false
DO_RESET=false
DO_STOP=false
DO_LOGS=false

for arg in "$@"; do
  case $arg in
    --seed)  DO_SEED=true  ;;
    --reset) DO_RESET=true ;;
    --stop)  DO_STOP=true  ;;
    --logs)  DO_LOGS=true  ;;
    *) warn "Unknown argument: $arg" ;;
  esac
done

# ── Check we are in the project root ─────────────────────────────────────────
if [[ ! -f "docker-compose.yml" ]]; then
  error "Run this script from the project root (where docker-compose.yml lives)"
fi

# ── Check dependencies ───────────────────────────────────────────────────────
info "Checking dependencies..."
command -v docker        &>/dev/null || error "Docker not found. Install: https://docs.docker.com/get-docker/"
command -v docker        &>/dev/null && docker compose version &>/dev/null || \
  command -v docker-compose &>/dev/null || error "Docker Compose not found."
command -v python3       &>/dev/null || error "python3 not found (needed for seed script)"
success "All dependencies found"

# Detect docker compose command (v2 plugin vs standalone)
if docker compose version &>/dev/null 2>&1; then
  DC="docker compose"
else
  DC="docker-compose"
fi

# ── --stop ───────────────────────────────────────────────────────────────────
if $DO_STOP; then
  info "Stopping containers..."
  $DC down
  success "Containers stopped. DB volume preserved (use --reset to wipe it)."
  exit 0
fi

# ── --reset ───────────────────────────────────────────────────────────────────
if $DO_RESET; then
  warn "RESET: this will destroy all containers and the MySQL data volume!"
  read -rp "Are you sure? [y/N] " confirm
  [[ "$confirm" =~ ^[Yy]$ ]] || { info "Reset cancelled."; exit 0; }
  info "Tearing down containers and volumes..."
  $DC down -v --remove-orphans
  success "Clean slate ready"
fi

# ── Check mvnw wrapper exists ────────────────────────────────────────────────
if [[ ! -f "mvnw" ]]; then
  warn "mvnw not found — Dockerfile requires it to build the app."
  echo ""
  echo "  Fix: download from https://start.spring.io, generate any project,"
  echo "  then copy mvnw, mvnw.cmd and .mvn/ into this directory."
  echo ""
  echo "  OR run locally without Docker:"
  echo "    mvn spring-boot:run"
  echo ""
  read -rp "Continue anyway (Docker build will fail)? [y/N] " confirm
  [[ "$confirm" =~ ^[Yy]$ ]] || exit 1
fi

# ── Build & start ────────────────────────────────────────────────────────────
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   Eurovision 2025 — Starting up...       ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

info "Building Docker images and starting containers..."
$DC up --build -d

success "Containers started"
echo ""
info "Container status:"
$DC ps

# ── Wait for Spring Boot ─────────────────────────────────────────────────────
echo ""
info "Waiting for Spring Boot to be ready at http://localhost:8080 ..."
MAX_WAIT=120
ELAPSED=0
until curl -sf http://localhost:8080/api/countries > /dev/null 2>&1; do
  if [[ $ELAPSED -ge $MAX_WAIT ]]; then
    error "Spring Boot didn't start within ${MAX_WAIT}s. Check logs: $DC logs app"
  fi
  echo -n "."
  sleep 3
  ELAPSED=$((ELAPSED + 3))
done
echo ""
success "Spring Boot is up! (${ELAPSED}s)"

# ── Seed ─────────────────────────────────────────────────────────────────────
if $DO_SEED; then
  echo ""
  info "Running seed script from scripts/seed-data.json ..."
  python3 scripts/seed.py --host http://localhost:8080 --retries 3
fi

# ── Done ─────────────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✅  Eurovision Backend is LIVE                       ║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  API Base    : http://localhost:8080/api             ║${NC}"
echo -e "${GREEN}║  Countries   : http://localhost:8080/api/countries   ║${NC}"
echo -e "${GREEN}║  Songs       : http://localhost:8080/api/songs       ║${NC}"
echo -e "${GREEN}║  Votes       : http://localhost:8080/api/votes       ║${NC}"
echo -e "${GREEN}║  Scores      : http://localhost:8080/api/scores      ║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  MySQL port  : localhost:3306                        ║${NC}"
echo -e "${GREEN}║  DB name     : eurovision_db                         ║${NC}"
echo -e "${GREEN}║  User/pass   : eurovision / eurovisionpass           ║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  To stop     : ./start.sh --stop                     ║${NC}"
echo -e "${GREEN}║  To reset DB : ./start.sh --reset                    ║${NC}"
echo -e "${GREEN}║  To reseed   : python3 scripts/seed.py               ║${NC}"
echo -e "${GREEN}║  App logs    : docker compose logs -f app            ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"

if $DO_LOGS; then
  echo ""
  info "Tailing logs (Ctrl+C to exit)..."
  $DC logs -f
fi
