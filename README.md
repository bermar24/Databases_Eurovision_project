# Databases_Eurovision_project

## 🎤 Eurovision Song Contest 2026 – Database Project

Welcome to our Eurovision-inspired database project!  
This project is part of our databases course and focuses on designing and implementing a full voting system for the **Eurovision Song Contest 2026**.

We aim to model, store, and process voting data from both **jury** and **public** votes across all shows — just like the real contest.

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
  - All Eurovision shows
- Reading and displaying results for all shows

---

## 🛠️ Features

- 🗳️ Add votes (Jury & Public)
- 🔢 Automatic vote counting
- 🧮 Points calculation based on Eurovision rules
- 📊 Retrieve full results for each show
- 🧪 Final live test with class participation

---

## 🧱 Tech Stack

- **Backend:** Spring Boot
- **Database:** MySQL
- **Containerization:** Docker
- **Version Control:** GitHub

---

## 📦 Project Deliverables

- ✅ Spring Boot project (Dockerized)
- ✅ GitHub repository
- ✅ EERM diagram (PDF format)
- ❌ Frontend *(not required)*

---

## 🧪 Testing Scenario

In the final lecture:

- Each student will participate as either:
  - 🎤 **Jury member**, or
  - 🌍 **Public voter** representing a country
- Votes will be submitted into the system
- The system must:
  - Count all votes correctly
  - Calculate final points
  - Output accurate rankings

---

## 🧮 Eurovision Scoring System

The project follows the traditional Eurovision voting format:

Each country awards two sets of points:
- **Jury votes**
- **Public votes**

For each voting group, points are assigned as follows:

| Rank | Points |
|------|--------|
| 1st  | 12     |
| 2nd  | 10     |
| 3rd  | 8      |
| 4th  | 7      |
| 5th  | 6      |
| 6th  | 5      |
| 7th  | 4      |
| 8th  | 3      |
| 9th  | 2      |
| 10th | 1      |

- Countries **cannot vote for themselves**
- Points from all countries are aggregated to determine final rankings

---

## 🚀 Getting Started

### Prerequisites

- Java (version 17+)
- Docker
- MySQL 

### Setup


# Clone the repository
``` git clone <REPO_LINK> ```

# Navigate into the project
```cd Databases_Eurovision_project```

# Run on Linux/MacOS
```chmod +x start.sh```
 
```./start.sh --seed```

## using Parrot OS
Start the Podman socket (needed once per session) before running the script:
```systemctl --user start podman.socket```

# Run on Windows
Prerequisites (Windows)
- Docker Desktop installed and running (the script checks this and gives a clear error if it's not)
- Python 3 installed from python.org — tick "Add Python to PATH" during install

How to run (Command Prompt or PowerShell)
bat:: Start + seed
```start.bat --seed```

:: Stop containers
```start.bat --stop```

:: Wipe DB and start fresh
```start.bat --reset --seed```

:: Tail logs after startup
```start.bat --seed --logs```
Then run script: ```./start.sh --seed```

# 🔧 Other useful commands
```./start.sh``` Start without seeding

```./start.sh --seed``` Start + load seed-data.json

```./start.sh --reset --seed``` Wipe DB + fresh start + seed

```./start.sh --stop``` Stop all containers

```./start.sh --logs``` Start + tail live logs

```python3 scripts/seed.py``` Re-seed a running instance

```docker compose logs -f app``` Watch Spring Boot logs

# ✏️ To customise the seed data
Just edit scripts/seed-data.json — it has all 37 Eurovision 2025 countries, all competing artists, jury members, citizens, and admins pre-filled. Run python3 scripts/seed.py any time to re-push it to a running instance.

