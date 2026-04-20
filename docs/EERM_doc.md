# Eurovision Project - EERM Documentation (Current Implementation)

> **Project:** `Databases_Eurovision_project`  
> **Backend:** Java 17+, Spring Boot 3.2, Hibernate/JPA, MySQL/H2  
> **Package:** `com.dhbw.eurovision`  
> **Inheritance strategy:** Joined Table Inheritance (`InheritanceType.JOINED`)

---

## 1. Overview

The current implemented model has these core concepts:

- `Country` owns many `User` and many `Song`
- `User` is abstract, specialized into `Jury`, `Citizen`, and `Admin`
- `Show` (table name `shows`) contains many songs and can be managed by many admins
- `VoteLog` stores one awarded point entry and is grouped into a ballot with `vote_session_id`
- `Score` stores aggregated results per `(song, show)` pair

```
Country --has--> User (abstract)
                  |-- Jury --votes--> VoteLog --for--> Song
                  |-- Citizen --votes-> VoteLog
                  |-- Admin --manage--> Show

Country --submits--> Song --appears in--> Show
VoteLog --calculate--> Score (per song per show)
```

---

## 2. Entity Catalogue

### 2.1 `Country`

- **Table:** `country`
- **Class:** `com.dhbw.eurovision.entity.Country`

| Attribute | Column | Constraints |
|---|---|---|
| `countryCode` | `country_code` | PK, NOT NULL, length 3 |
| `countryName` | `country_name` | NOT NULL, UNIQUE |

Relationships:
- 1:M to `User` (`Country.users`)
- 1:M to `Song` (`Country.songs`)

### 2.2 `User` (abstract)

- **Table:** `user`
- **Class:** `com.dhbw.eurovision.entity.User`
- **Inheritance:** `@Inheritance(strategy = JOINED)`

| Attribute | Column | Constraints |
|---|---|---|
| `userId` | `user_id` | PK, AUTO_INCREMENT |
| `country` | `country_code` | FK -> `country.country_code`, NOT NULL |

Current TODO in code:
- optional future shared fields: `username`, `email`

### 2.3 `Jury`

- **Table:** `jury`
- **Class:** `com.dhbw.eurovision.entity.Jury`

| Attribute | Column | Constraints |
|---|---|---|
| inherited `userId` | `user_id` | PK/FK -> `user.user_id` |
| `professionalBg` | `professional_bg` | nullable |

Relationships:
- 1:M to `VoteLog` (`vote_log.jury_id`)

### 2.4 `Citizen`

- **Table:** `citizen`
- **Class:** `com.dhbw.eurovision.entity.Citizen`

| Attribute | Column | Constraints |
|---|---|---|
| inherited `userId` | `user_id` | PK/FK -> `user.user_id` |
| `phoneNumber` | `phone_number` | NOT NULL, UNIQUE, length 20 |

Behavior in service layer:
- phone is treated as a business key
- registration is idempotent (`findOrCreateByPhone` returns existing citizen for existing phone)

### 2.5 `Admin`

- **Table:** `admin`
- **Class:** `com.dhbw.eurovision.entity.Admin`

| Attribute | Column | Constraints |
|---|---|---|
| inherited `userId` | `user_id` | PK/FK -> `user.user_id` |
| `adminLevel` | `admin_level` | nullable |

Relationships:
- M:N with `Show` via join table `admin_show` (`admin_user_id`, `show_id`)

### 2.6 `Song`

- **Table:** `song`
- **Class:** `com.dhbw.eurovision.entity.Song`

| Attribute | Column | Constraints |
|---|---|---|
| `songId` | `song_id` | PK, AUTO_INCREMENT |
| `singerName` | `singer_name` | NOT NULL |
| `country` | `country_code` | FK -> `country.country_code`, NOT NULL |

Relationships:
- M:1 to `Country`
- 1:M to `VoteLog`
- 1:M to `Score` (because score is per show)
- M:N with `Show` via `show_song` (inverse side on `Song`)

### 2.7 `Show`

- **Table:** `shows`
- **Class:** `com.dhbw.eurovision.entity.Show`

| Attribute | Column | Constraints |
|---|---|---|
| `showId` | `show_id` | PK, AUTO_INCREMENT |
| `showName` | `show_name` | NOT NULL |

Relationships:
- M:N with `Song` via `show_song`
- M:N with `Admin` via `admin_show` (inverse side on `Show`)

Current TODO in code:
- optional future fields `showDate`, `showType`

### 2.8 `VoteLog`

- **Table:** `vote_log`
- **Class:** `com.dhbw.eurovision.entity.VoteLog`

| Attribute | Column | Constraints |
|---|---|---|
| `voteLogId` | `vote_log_id` | PK, AUTO_INCREMENT |
| `points` | `points` | NOT NULL |
| `voteSessionId` | `vote_session_id` | NOT NULL, length 36 |
| `song` | `song_id` | FK -> `song.song_id`, NOT NULL |
| `show` | `show_id` | FK -> `shows.show_id`, NOT NULL |
| `jury` | `jury_id` | FK -> `jury.user_id`, nullable |
| `citizen` | `citizen_id` | FK -> `citizen.user_id`, nullable |

Rules enforced in `VoteLogService.submitBallot()`:
- exactly one voter type (`juryId` xor `citizenId`)
- exactly 10 entries using points `{12,10,8,7,6,5,4,3,2,1}` each once
- voter can submit only one ballot per show
- no own-country voting
- all voted songs must belong to the selected show
- semifinal eligibility check: voter country must compete in that semifinal

### 2.9 `Score`

- **Table:** `score`
- **Class:** `com.dhbw.eurovision.entity.Score`

| Attribute | Column | Constraints |
|---|---|---|
| `scoreId` | `score_id` | PK, AUTO_INCREMENT |
| `songScore` | `song_score` | NOT NULL, default 0 in entity |
| `song` | `song_id` | FK -> `song.song_id`, NOT NULL |
| `show` | `show_id` | FK -> `shows.show_id`, NOT NULL |

Unique constraint:
- `UNIQUE(song_id, show_id)` (`uq_score_song_show`)

Meaning:
- one score row per song per show
- same song can have separate scores in semifinal and grand final

---

## 3. Relationship Catalogue

### 3.1 Has - `Country` -> `User` (1:M)

- FK: `user.country_code`
- Implemented with `@ManyToOne` in `User` and `@OneToMany` in `Country`

### 3.2 Is a - `User` -> `Jury` / `Citizen` / `Admin`

- Joined Table Inheritance
- tables: `user`, `jury`, `citizen`, `admin`
- subtype tables use shared `user_id` PK/FK

### 3.3 Country -> Song (1:M)

- FK: `song.country_code`
- cascade configured from `Country.songs`

### 3.4 Votes - voter -> `VoteLog` -> `Song`

- `VoteLog` resolves voting relationships
- voter is either jury or citizen per row
- rows are grouped into one ballot using `vote_session_id`

### 3.5 Calculate - `VoteLog` -> `Score`

- implemented in `ScoreService`
- `calculateShowScores(showId)`:
  - semifinals: citizen aggregation only
  - grand final: jury aggregation + citizen aggregation
- both jury and citizen use country-based ranking aggregation with Eurovision scale

### 3.6 Song <-> Show (M:N)

- join table: `show_song` (`show_id`, `song_id`)
- owning side: `Show.songs`

### 3.7 Manage - Admin <-> Show (M:N)

- join table: `admin_show` (`admin_user_id`, `show_id`)
- owning side: `Admin.managedShows`
- exposed by endpoint: `POST /api/admins/{adminId}/shows/{showId}`

---

## 4. Database Tables

| Table | PK | Notes |
|---|---|---|
| `country` | `country_code` | countries |
| `user` | `user_id` | base user table |
| `jury` | `user_id` | user subtype |
| `citizen` | `user_id` | user subtype, includes `phone_number` |
| `admin` | `user_id` | user subtype |
| `song` | `song_id` | songs |
| `shows` | `show_id` | shows |
| `vote_log` | `vote_log_id` | vote rows |
| `score` | `score_id` | aggregated scores |

Join tables:

| Join Table | Columns |
|---|---|
| `show_song` | `show_id`, `song_id` |
| `admin_show` | `admin_user_id`, `show_id` |

---

## 5. Notes and TODOs (from source code)

- `User`: potential future shared fields `username`, `email`
- `Song`: potential future fields `songTitle`, `language`
- `Show`: potential future fields `showDate`, `showType`
- there is no global `@ControllerAdvice` yet; many service errors still bubble as default error responses
