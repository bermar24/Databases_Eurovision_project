# Eurovision 2025 — EERM Documentation

> **Project:** Databases_Eurovision_project  
> **Backend:** Java 17 · Spring Boot 3.2 · Hibernate/JPA · MySQL 8  
> **Package:** `com.dhbw.eurovision`  
> **Inheritance strategy:** Joined Table Inheritance (JTI)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Entity Catalogue](#2-entity-catalogue)
   - 2.1 [Country](#21-country)
   - 2.2 [User](#22-user-abstract-base)
   - 2.3 [Jury](#23-jury)
   - 2.4 [Citizen](#24-citizen)
   - 2.5 [Admin](#25-admin)
   - 2.6 [Song](#26-song)
   - 2.7 [VoteLog](#27-votelog)
   - 2.8 [Score](#28-score)
   - 2.9 [Show](#29-show)
3. [Relationship Catalogue](#3-relationship-catalogue)
   - 3.1 [Has — Country → User (1:M)](#31-has--country--user-1m)
   - 3.2 [Is a — User → Jury | Citizen | Admin](#32-is-a--user--jury--citizen--admin)
   - 3.3 [Country → Song (1:M)](#33-country--song-1m)
   - 3.4 [Votes — Jury/Citizen → VoteLog → Song (M:N)](#34-votes--jurycitizen--votelog--song-mn)
   - 3.5 [Calculate — VoteLog → Score](#35-calculate--votelog--score)
   - 3.6 [Song ↔ Show (M:N)](#36-song--show-mn)
   - 3.7 [Manage — Admin ↔ Show (M:N)](#37-manage--admin--show-mn)
4. [Database Tables](#4-database-tables)
5. [Join Tables](#5-join-tables)
6. [Inheritance Tables (JTI)](#6-inheritance-tables-jti)
7. [Design Decisions & TODOs](#7-design-decisions--todos)

---

## 1. Overview

The Eurovision 2025 data model captures the full lifecycle of the contest:
participating **Countries** submit **Songs**, three **Shows** are held (Semi-Final 1, Semi-Final 2, Grand Final), registered **Users** (split into **Jury**, **Citizen**, and **Admin** subtypes) interact with the system, **VoteLogs** record every individual vote cast, and a **Score** aggregates those votes per song.

```
Country ──has──► User (abstract)
                   ├── Jury      ──votes──► VoteLog ──► Song ──calculate──► Score
                   ├── Citizen   ──votes──► VoteLog
                   └── Admin     ──manages──► Show ◄──contains──► Song
Country ──────────────────────────────────────────────► Song
```

---

## 2. Entity Catalogue

---

### 2.1 Country

**EERM notation:** Rectangle  
**DB table:** `country`  
**Java class:** `com.dhbw.eurovision.entity.Country`

Represents a nation participating in Eurovision. Used as the root anchor — every Song and every User belongs to a Country.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `countryCode` | `country_code` | `VARCHAR(3)` | **PK**, NOT NULL | ISO 3166-1 alpha-2, e.g. `"DE"`, `"SE"` |
| `countryName` | `country_name` | `VARCHAR(255)` | NOT NULL, UNIQUE | Full English name |

**Relationships owned by Country:**

| Relationship | Type | Target | Mapped by |
|---|---|---|---|
| Has Users | 1:M | `User` | `user.country_code` FK |
| Has Songs | 1:M | `Song` | `song.country_code` FK |

---

### 2.2 User *(abstract base)*

**EERM notation:** Rectangle + "Is a" triangle  
**DB table:** `user`  
**Java class:** `com.dhbw.eurovision.entity.User`  
**JPA Inheritance:** `InheritanceType.JOINED`

Base class for all user types. Never instantiated directly — always a Jury, Citizen, or Admin. The `user` table holds all shared columns; subtype tables join back via `user_id`.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `userId` | `user_id` | `BIGINT` | **PK**, AUTO_INCREMENT | Shared across all subtypes |
| `country` (FK) | `country_code` | `VARCHAR(3)` | NOT NULL, FK → `country` | The "Has" relationship |

> **TODO:** Add `username VARCHAR(255) UNIQUE NOT NULL` and `email VARCHAR(255) UNIQUE NOT NULL` once agreed with your team.

---

### 2.3 Jury

**EERM notation:** Rectangle (subtype of User)  
**DB table:** `jury`  
**Java class:** `com.dhbw.eurovision.entity.Jury`

A professional music expert who casts weighted jury votes. Each country's jury votes for songs from *other* countries.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `userId` (inherited) | `user_id` | `BIGINT` | **PK/FK** → `user.user_id` | Joined table — same ID as User |
| `professionalBg` | `professional_bg` | `VARCHAR(255)` | nullable | e.g. `"Music Producer"`, `"Composer"` |

**Jury aggregation rule:**
Each country has 1–5 Jury members in the database. When scores are calculated, all jury ballots from the same country are pooled — their raw points per song are summed to produce a national ranking, and then the scale 12,10,8,7,6,5,4,3,2,1 is awarded once per country to the top 10 songs. This mirrors the real Eurovision national jury model: regardless of how many jurors a country has, each country contributes exactly one set of points.

**Relationships:**

| Relationship | Type | Target | Notes |
|---|---|---|---|
| Votes | 1:M | `VoteLog` | `vote_log.jury_id` FK |

---

### 2.4 Citizen

**EERM notation:** Rectangle (subtype of User)  
**DB table:** `citizen`  
**Java class:** `com.dhbw.eurovision.entity.Citizen`

A member of the public who casts a televote. Citizens vote from their home country.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `userId` (inherited) | `user_id` | `BIGINT` | **PK/FK** → `user.user_id` | Joined table |
| `phoneNumber` | `phone_number` | `VARCHAR(20)` | NOT NULL, UNIQUE | Voter identity — same phone = same citizen |

> Registering with a phone number that already exists returns the existing citizen record (idempotent).

**Relationships:**

| Relationship | Type | Target | Notes |
|---|---|---|---|
| Votes | 1:M | `VoteLog` | `vote_log.citizen_id` FK |

---

### 2.5 Admin

**EERM notation:** Rectangle (subtype of User)  
**DB table:** `admin`  
**Java class:** `com.dhbw.eurovision.entity.Admin`

A show manager with elevated permissions. Can be assigned to manage one or more Shows.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `userId` (inherited) | `user_id` | `BIGINT` | **PK/FK** → `user.user_id` | Joined table |
| `adminLevel` | `admin_level` | `INT` | nullable | Permission tier, e.g. 1 = super, 2 = standard |

**Relationships:**

| Relationship | Type | Target | Join Table | Notes |
|---|---|---|---|---|
| Manages | M:N | `Show` | `admin_show` | owning side |

---

### 2.6 Song

**EERM notation:** Rectangle  
**DB table:** `song`  
**Java class:** `com.dhbw.eurovision.entity.Song`

A musical entry submitted by a Country. Each country submits exactly one song per contest year. A song can appear in multiple Shows and receives votes from Jury and Citizens.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `songId` | `song_id` | `BIGINT` | **PK**, AUTO_INCREMENT | |
| `singerName` | `singer_name` | `VARCHAR(255)` | NOT NULL | Performing artist name |
| `country` (FK) | `country_code` | `VARCHAR(3)` | NOT NULL, FK → `country` | Representing country |

> **TODO:** Add `songTitle`, `language`, `runningOrder` once agreed.

**Relationships:**

| Relationship | Type | Target | Notes |
|---|---|---|---|
| Belongs to Country | M:1 | `Country` | FK `country_code` |
| Has Score | 1:1 | `Score` | inverse side — Score owns the FK |
| Receives Votes | 1:M | `VoteLog` | `vote_log.song_id` FK |
| Appears in Shows | M:N | `Show` | join table `show_song` — inverse side |

---

### 2.7 VoteLog

**EERM notation:** Rectangle (associative entity for the "Votes" diamond)  
**DB table:** `vote_log`  
**Java class:** `com.dhbw.eurovision.entity.VoteLog`

Records a single vote cast event. Acts as the resolved M:N between (Jury or Citizen) and Song. Exactly one of `jury_id` or `citizen_id` must be non-null per row — this represents either a jury vote or a public vote.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `voteLogId` | `vote_log_id` | `BIGINT` | **PK**, AUTO_INCREMENT | |
| `points` | `points` | `INT` | NOT NULL | Eurovision scale: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1 |
| `song` (FK) | `song_id` | `BIGINT` | NOT NULL, FK → `song` | Song being voted for |
| `jury` (FK) | `jury_id` | `BIGINT` | nullable, FK → `jury` | Set when jury vote |
| `citizen` (FK) | `citizen_id` | `BIGINT` | nullable, FK → `citizen` | Set when public vote |

> **Business rules:** (1) Exactly one of `jury_id` / `citizen_id` must be set. (2) `points` must be one of `{12, 10, 8, 7, 6, 5, 4, 3, 2, 1}`. (3) Voter's country must differ from the song's country — enforced in `VoteLogService`.

---

### 2.8 Score

**EERM notation:** Rectangle (result of "Calculate" diamond)  
**DB table:** `score`  
**Java class:** `com.dhbw.eurovision.entity.Score`

Holds the aggregated total score for a Song. Calculated by summing all VoteLog entries for that song. One Score per Song (1:1). Score is never created directly — always via `ScoreService.calculateScoreForSong()`.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `scoreId` | `score_id` | `BIGINT` | **PK**, AUTO_INCREMENT | |
| `songScore` | `song_score` | `INT` | NOT NULL, default 0 | Aggregated total points |
| `song` (FK) | `song_id` | `BIGINT` | NOT NULL, UNIQUE, FK → `song` | Owning side of 1:1 |

---

### 2.9 Show

**EERM notation:** Rectangle  
**DB table:** `show`  
**Java class:** `com.dhbw.eurovision.entity.Show`

An Eurovision broadcast event. The contest has three shows: Semi-Final 1, Semi-Final 2, and the Grand Final. Admins manage shows; Songs compete in shows.

| Attribute | Column | Type | Constraints | Notes |
|---|---|---|---|---|
| `showId` | `show_id` | `BIGINT` | **PK**, AUTO_INCREMENT | |
| `showName` | `show_name` | `VARCHAR(255)` | NOT NULL | e.g. `"Semi-Final 1"`, `"Grand Final"` |

**Relationships:**

| Relationship | Type | Target | Join Table | Notes |
|---|---|---|---|---|
| Contains Songs | M:N | `Song` | `show_song` | owning side |
| Managed by Admins | M:N | `Admin` | `admin_show` | inverse side |

---

## 3. Relationship Catalogue

---

### 3.1 Has — Country → User (1:M)

| Property | Value |
|---|---|
| EERM symbol | Diamond labelled **"Has"** |
| Cardinality | 1 Country : M Users |
| Direction | Country → User |
| Implementation | `user.country_code` FK → `country.country_code` |
| Cascade | ALL (delete country → delete its users) |
| Fetch | LAZY |

One country has many registered users. Every User (Jury, Citizen, Admin) must belong to exactly one country.

---

### 3.2 Is a — User → Jury | Citizen | Admin

| Property | Value |
|---|---|
| EERM symbol | Triangle labelled **"Is a"** |
| Type | Total specialisation (every User is exactly one subtype) |
| Strategy | **Joined Table Inheritance** |
| Tables | `user` (base) + `jury`, `citizen`, `admin` (subtypes) |
| Join key | `user_id` shared as PK/FK across all four tables |

Each User is specialised into exactly one of three subtypes. The `user` table holds shared data; subtype tables extend it with type-specific attributes.

---

### 3.3 Country → Song (1:M)

| Property | Value |
|---|---|
| Cardinality | 1 Country : M Songs |
| Implementation | `song.country_code` FK → `country.country_code` |
| Cascade | ALL |
| Fetch | LAZY |

Each song represents exactly one country. A country may have multiple song entries (across different years or heats, depending on business rules).

---

### 3.4 Votes — Jury/Citizen → VoteLog → Song (M:N)

| Property | Value |
|---|---|
| EERM symbol | Diamond labelled **"Votes"** |
| Cardinality | M (Jury or Citizen) : M Songs |
| Resolution | Associative entity **VoteLog** |
| FKs in VoteLog | `jury_id` nullable, `citizen_id` nullable, `song_id` NOT NULL |

The "Votes" diamond is resolved into the `vote_log` table. Each row is one vote event. The voter is identified by either `jury_id` OR `citizen_id` — never both.

---

### 3.5 Calculate — VoteLog → Score

| Property | Value |
|---|---|
| EERM symbol | Diamond labelled **"Calculate"** |
| Type | Derived / aggregation relationship |
| Cardinality | M VoteLogs → 1 Score (per Song) |
| Implementation | `ScoreService.calculateScoreForSong()` aggregates VoteLogs → writes `score.song_score` |
| DB | `score.song_id` UNIQUE FK → `song.song_id` |

The Calculate diamond is not a stored FK relationship but a business logic operation. Calling the score calculation endpoint triggers aggregation of all VoteLogs for a song into a single Score row.

---

### 3.6 Song ↔ Show (M:N)

| Property | Value |
|---|---|
| Cardinality | M Songs : M Shows |
| Join table | `show_song` (`show_id`, `song_id`) |
| Owning side | `Show` entity |
| Inverse side | `Song.shows` |

A song can be assigned to multiple shows (e.g. a semi-final and the grand final). A show contains multiple songs.

---

### 3.7 Manage — Admin ↔ Show (M:N)

| Property | Value |
|---|---|
| EERM symbol | Diamond labelled **"Manage"** |
| Cardinality | M Admins : M Shows |
| Join table | `admin_show` (`admin_user_id`, `show_id`) |
| Owning side | `Admin.managedShows` |
| Inverse side | `Show.admins` |
| Endpoint | `POST /api/admins/{adminId}/shows/{showId}` |

One admin can manage multiple shows; one show can be managed by multiple admins.

---

## 4. Database Tables

| Table | PK | Description |
|---|---|---|
| `country` | `country_code` | Participating nations |
| `user` | `user_id` | Base user table (JTI parent) |
| `jury` | `user_id` (FK/PK) | Jury subtype |
| `citizen` | `user_id` (FK/PK) | Citizen subtype |
| `admin` | `user_id` (FK/PK) | Admin subtype |
| `song` | `song_id` | Contest entries |
| `vote_log` | `vote_log_id` | Individual vote events |
| `score` | `score_id` | Aggregated scores per song |
| `show` | `show_id` | Contest broadcast events |

---

## 5. Join Tables

| Join Table | Column 1 | Column 2 | Resolves |
|---|---|---|---|
| `show_song` | `show_id` FK → `show` | `song_id` FK → `song` | Song ↔ Show M:N |
| `admin_show` | `admin_user_id` FK → `admin` | `show_id` FK → `show` | Admin ↔ Show M:N |

---

## 6. Inheritance Tables (JTI)

Joined Table Inheritance means `user_id` is the shared key across four tables:

```
user          jury               citizen          admin
──────────    ───────────────    ─────────────    ──────────────────
user_id (PK)  user_id (PK/FK)   user_id (PK/FK)  user_id (PK/FK)
country_code  professional_bg   (no extra cols)   admin_level
```

A query for a `Jury` record does: `SELECT * FROM user JOIN jury USING (user_id) WHERE user_id = ?`

---

## 7. Design Decisions & TODOs

| # | Decision / TODO | Reason |
|---|---|---|
| 1 | Joined Table Inheritance for User subtypes | Cleaner DB schema; each subtype's attributes are in its own table; easier to query one type independently |
| 2 | `country_code` as String PK (not BIGINT) | ISO codes are natural, stable, and human-readable — no surrogate key needed |
| 3 | `VoteLog.jury_id` and `citizen_id` both nullable | One row = one vote; voter type determined by which FK is set |
| 4 | Score is calculated, not inserted directly | Keeps score in sync with VoteLog; prevents manual tampering |
| 5 | TODO: Add `User.username` + `User.email` | Required for authentication — deferred until auth layer is designed |
| 6 | TODO: Add `VoteLog.points` | Scoring scale (1–12 Eurovision style) not yet agreed by team |
| 7 | TODO: Add `Show.showDate`, `showType` | Show schedule/type not yet confirmed by team |
| 8 | TODO: Add `Song.songTitle`, `language` | Extended song metadata deferred to next sprint |
