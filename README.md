# Databases_Eurovision_project

## 🎤 Eurovision Song Contest 2025 – Database Project

Welcome to our Eurovision-inspired database project!
This project is part of our databases course and focuses on designing and implementing a full voting system for the **Eurovision Song Contest 2025**.

We aim to model, store, and process voting data from both **jury** and **public** votes across all shows like the real contest.

---

## 📌 Project Goals

The main objective of this project is to design and implement a complete voting system, including:

- Creating an **Enhanced Entity-Relationship Model (EERM)** for Eurovision voting
- Developing a backend with an **ORM connected to a MySQL database**
- Handling:
  - Vote submission
  - Vote counting
  - Points calculation
- Supporting:
  - Jury voting
  - Public voting
- Reading and displaying results for all shows

---

## 🛠️ Features


- 🗳️ Add votes (Jury & Public)
- 🔢 Automatic vote counting
- 🧮 Points calculation based on Eurovision rules
- 📊 Retrieve full results for each show
- 🧪 Final live test with class participation

Spring Boot backend for a Eurovision-style voting system with:
- countries, songs, users (jury/citizen/admin), shows
- ballot submission (10 ranked point entries)
- score calculation per show
- Docker-based local setup and optional static frontend

---

## 🧱 Tech Stack
- Java 17+
- **Backend:** Spring Boot 3.2
- Spring Data JPA / Hibernate
- **Database:** MySQL 8 (runtime) and H2 (tests)
- **Containerization:** Docker
- **Version Control:** GitHub
---

## What is implemented
- CRUD APIs for `countries`, `songs`, `shows`, `jury`, `citizens`, `admins`
- M:N assignment APIs:
  - `POST /api/shows/{showId}/songs/{songId}`
  - `POST /api/admins/{adminId}/shows/{showId}`
- Ballot voting API:
  - `POST /api/votes/session` (one full ballot = 10 entries)
- Vote validation rules in service layer:
  - one ballot per voter per show
  - no own-country voting
  - songs must belong to selected show
  - semifinal eligibility checks
- Score APIs:
  - `GET /api/scores`
  - `GET /api/scores/show/{showId}`
  - `GET /api/scores/song/{songId}`
  - `POST /api/scores/calculate-show/{showId}`
  - `POST /api/scores/calculate/{showId}/{songId}`
  - `POST /api/scores/calculate-all`

---

## 📦 Project Deliverables

- ✅ Spring Boot project (Dockerized)
- ✅ GitHub repository
- ✅ EERM diagram (PDF format)
- ✅ Frontend *Provisional*

## Scoring model (as implemented)
- Point scale per ballot: `12,10,8,7,6,5,4,3,2,1`
- Votes are stored in `vote_log` rows and grouped by `vote_session_id`
- Scores are stored per `(song, show)` in `score` (`UNIQUE(song_id, show_id)`)
- `calculateShowScores(showId)` logic:
  - Grand Final: jury aggregation + citizen aggregation
  - Semi-finals: citizen aggregation only
- Both jury and citizen aggregation are country-based:
  raw points are summed per country and then converted to Eurovision top-10 scale


---

## Run locally Linux/macOS
### Prerequisites

- Java 17+
- Docker (or Podman socket compatibility)
- Python 3 (for seed script)


```bash
cd Databases_Eurovision_project
chmod +x start.sh
./start.sh --seed --frontend
```

#### Parrot OS (Podman)

```bash
systemctl --user start podman.socket
./start.sh --seed --frontend
```

### Useful commands

```./start.sh``` Start without seeding

```./start.sh --seed``` Start + load seed-data.json

```./start.sh --reset --seed``` Wipe DB + fresh start + seed

```./start.sh --stop``` Stop all containers

```./start.sh --logs``` Start + tail live logs

```python3 scripts/seed.py``` Re-seed a running instance

```docker compose logs -f app``` Watch Spring Boot logs


## Run locally Windows
### Prerequisites
- Docker Desktop installed and running (the script checks this and gives a clear error if it's not)
- Python 3 installed from python.org — tick "Add Python to PATH" during install
  
(Command Prompt or PowerShell)

Start + seed: ```start.bat --seed --frontend``` 

### Useful commands

Stop containers ```start.bat --stop```

Wipe DB and start fresh ```start.bat --reset --seed```

Tail logs after startup ```start.bat --seed --logs```

Then run script: ```./start.sh --seed```


## Docs
- EERM text: `docs/EERM_doc.md`
- EERM mermaid: `docs/EERM.mermaid`
- API reference: `docs/API_doc.md`

## Current gaps / next improvements
- add global exception mapping (`@ControllerAdvice`) for consistent HTTP errors
- add auth/security (Spring Security)
- add richer show/song metadata