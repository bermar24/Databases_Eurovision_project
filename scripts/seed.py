#!/usr/bin/env python3
"""
Eurovision 2025 — Database Seed Script
=======================================
Reads scripts/seed-data.json and POSTs each record to the Spring Boot REST API.

Usage:
    python3 scripts/seed.py [--host http://localhost:8080] [--retries 20]

Requirements: Python 3.7+ (no extra packages needed — uses stdlib only)
"""

import json
import urllib.request
import urllib.error
import sys
import time
import argparse
from pathlib import Path


# ── Config ──────────────────────────────────────────────────────────────────

SEED_FILE = Path(__file__).parent / "seed-data.json"

ENDPOINTS = {
    "countries": "/api/countries",
    "shows":     "/api/shows",
    "songs":     "/api/songs",
    "admins":    "/api/admins",
    "jury":      "/api/jury",
    "citizens":  "/api/citizens",
}

# Order matters — countries must exist before songs, users before votes
SEED_ORDER = ["countries", "shows", "songs", "admins", "jury", "citizens"]


# ── Helpers ──────────────────────────────────────────────────────────────────

def post(base_url: str, path: str, payload: dict) -> dict:
    url = base_url + path
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode("utf-8")
            return json.loads(body) if body.strip() else {}
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        print(f"    ✗ HTTP {e.code} — {body[:200]}")
        return {}


def wait_for_api(base_url: str, retries: int) -> bool:
    """Poll /api/countries until Spring Boot is ready."""
    print(f"⏳  Waiting for API at {base_url} ...")
    for attempt in range(1, retries + 1):
        try:
            with urllib.request.urlopen(base_url + "/api/countries", timeout=3) as r:
                if r.status < 500:
                    print(f"✅  API is up (attempt {attempt})\n")
                    return True
        except Exception:
            pass
        print(f"    ... not ready yet ({attempt}/{retries}), retrying in 3s")
        time.sleep(3)
    return False


# ── Main ─────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Seed the Eurovision DB via REST API")
    parser.add_argument("--host",    default="http://localhost:8080", help="Base URL of the Spring Boot app")
    parser.add_argument("--retries", type=int, default=20,            help="How many times to retry waiting for the API")
    args = parser.parse_args()

    base_url = args.host.rstrip("/")

    # ── Load seed file ───────────────────────────────────────────────────────
    if not SEED_FILE.exists():
        print(f"❌  Seed file not found: {SEED_FILE}")
        sys.exit(1)

    with open(SEED_FILE) as f:
        data = json.load(f)

    # ── Wait for Spring Boot ─────────────────────────────────────────────────
    if not wait_for_api(base_url, args.retries):
        print("❌  API never became ready. Is Docker Compose running?")
        sys.exit(1)

    # ── Seed each collection in order ────────────────────────────────────────
    created_ids = {}   # store returned IDs for cross-references

    for key in SEED_ORDER:
        records = data.get(key, [])
        if not records:
            print(f"⚠️   No records for '{key}' — skipping")
            continue

        endpoint = ENDPOINTS[key]
        print(f"📥  Seeding {len(records)} {key} → {endpoint}")
        ids = []
        ok = 0

        for i, record in enumerate(records):
            # Skip comment keys (start with _)
            if not isinstance(record, dict):
                continue

            result = post(base_url, endpoint, record)
            if result:
                ok += 1
                # Grab the ID field (varies by entity)
                for id_field in ("userId", "songId", "showId", "scoreId", "voteLogId", "countryCode"):
                    if id_field in result:
                        ids.append(result[id_field])
                        break
            else:
                print(f"    ⚠️  Record {i+1} failed: {record}")

        created_ids[key] = ids
        print(f"    ✔  {ok}/{len(records)} created\n")

    # ── Summary ──────────────────────────────────────────────────────────────
    print("=" * 50)
    print("🎉  Seed complete! Summary:")
    for key in SEED_ORDER:
        ids = created_ids.get(key, [])
        print(f"   {key:12s}: {len(ids)} records")
    print("=" * 50)
    print(f"\n🌐  API is live at {base_url}")
    print(f"    Countries : {base_url}/api/countries")
    print(f"    Songs     : {base_url}/api/songs")
    print(f"    Scores    : {base_url}/api/scores")
    print(f"    Votes     : {base_url}/api/votes")


if __name__ == "__main__":
    main()
