# Eurovision 2025 — REST API Documentation

> **Base URL:** `http://localhost:8080/api`  
> **Content-Type:** `application/json` for all requests and responses  
> **Auth:** None implemented yet — add Spring Security in a later sprint

---

## Table of Contents

1. [Countries](#1-countries)
2. [Songs](#2-songs)
3. [Shows](#3-shows)
4. [Jury](#4-jury)
5. [Citizens](#5-citizens)
6. [Admins](#6-admins)
7. [Votes](#7-votes)
8. [Scores](#8-scores)
9. [Suggested Extra Queries](#9-suggested-extra-queries)
10. [Error Responses](#10-error-responses)
11. [cURL Quick-Reference](#11-curl-quick-reference)

---

## 1. Countries

### GET `/api/countries`
List all participating countries.

**Response `200 OK`:**
```json
[
  { "countryCode": "SE", "countryName": "Sweden" },
  { "countryCode": "DE", "countryName": "Germany" }
]
```

---

### GET `/api/countries/{code}`
Get one country by its ISO code.

**Path param:** `code` — e.g. `SE`

**Response `200 OK`:**
```json
{ "countryCode": "SE", "countryName": "Sweden" }
```

**Response `500`** if not found (implement `@ControllerAdvice` to return `404`).

---

### POST `/api/countries`
Create a new country.

**Request body:**
```json
{ "countryCode": "SE", "countryName": "Sweden" }
```

**Response `201 Created`:**
```json
{ "countryCode": "SE", "countryName": "Sweden" }
```

---

### DELETE `/api/countries/{code}`
Remove a country (cascades to its users and songs).

**Response `204 No Content`**

---

## 2. Songs

### GET `/api/songs`
List all songs.

**Response `200 OK`:**
```json
[
  { "songId": 1, "singerName": "KAJ", "countryCode": "SE" },
  { "songId": 2, "singerName": "Zoë Më", "countryCode": "CH" }
]
```

---

### GET `/api/songs/{id}`
Get one song by ID.

**Response `200 OK`:**
```json
{ "songId": 1, "singerName": "KAJ", "countryCode": "SE" }
```

---

### POST `/api/songs`
Create a new song entry.

**Request body:**
```json
{ "singerName": "KAJ", "countryCode": "SE" }
```

**Response `201 Created`:**
```json
{ "songId": 1, "singerName": "KAJ", "countryCode": "SE" }
```

> **Note:** `countryCode` must already exist in the `country` table.

---

### DELETE `/api/songs/{id}`
Remove a song (cascades to its VoteLogs and Score).

**Response `204 No Content`**

---

## 3. Shows

### GET `/api/shows`
List all shows.

**Response `200 OK`:**
```json
[
  { "showId": 1, "songIds": [1, 3, 5], "adminIds": [1] },
  { "showId": 2, "songIds": [2, 4, 6], "adminIds": [1, 2] },
  { "showId": 3, "songIds": [1, 2, 3, 4, 5, 6], "adminIds": [1, 2] }
]
```

---

### GET `/api/shows/{id}`
Get one show by ID.

---

### POST `/api/shows`
Create a new show.

**Request body:** `{}` *(empty for now — add showName/showDate when entity fields are added)*

**Response `201 Created`:**
```json
{ "showId": 3, "songIds": [], "adminIds": [] }
```

---

### DELETE `/api/shows/{id}`
Remove a show.

**Response `204 No Content`**

---

## 4. Jury

### GET `/api/jury`
List all jury members.

**Response `200 OK`:**
```json
[
  { "userId": 1, "countryCode": "DE", "professionalBg": "Music Producer" },
  { "userId": 2, "countryCode": "FR", "professionalBg": "Vocal Coach" }
]
```

---

### GET `/api/jury/{id}`
Get one jury member by user ID.

---

### POST `/api/jury`
Register a new jury member.

**Request body:**
```json
{ "countryCode": "DE", "professionalBg": "Music Producer" }
```

**Response `201 Created`:**
```json
{ "userId": 1, "countryCode": "DE", "professionalBg": "Music Producer" }
```

---

### DELETE `/api/jury/{id}`
Remove a jury member (cascades to their VoteLogs).

**Response `204 No Content`**

---

## 5. Citizens

### GET `/api/citizens`
List all registered citizens.

**Response `200 OK`:**
```json
[
  { "userId": 10, "countryCode": "SE" },
  { "userId": 11, "countryCode": "NO" }
]
```

---

### GET `/api/citizens/{id}`
Get one citizen by user ID.

---

### POST `/api/citizens`
Register a new citizen voter.

**Request body:**
```json
{ "countryCode": "SE" }
```

**Response `201 Created`:**
```json
{ "userId": 10, "countryCode": "SE" }
```

---

### DELETE `/api/citizens/{id}`
Remove a citizen (cascades to their VoteLogs).

**Response `204 No Content`**

---

## 6. Admins

### GET `/api/admins`
List all admins.

**Response `200 OK`:**
```json
[
  { "userId": 20, "countryCode": "CH", "adminLevel": 1, "managedShowIds": [1, 2, 3] }
]
```

---

### GET `/api/admins/{id}`
Get one admin by user ID.

---

### POST `/api/admins`
Create a new admin.

**Request body:**
```json
{ "countryCode": "CH", "adminLevel": 1 }
```

**Response `201 Created`:**
```json
{ "userId": 20, "countryCode": "CH", "adminLevel": 1, "managedShowIds": [] }
```

---

### POST `/api/admins/{adminId}/shows/{showId}`
Assign an admin to manage a show. Implements the EERM **"Manage"** M:N relationship.

**Example:** `POST /api/admins/20/shows/3`

**Response `200 OK`:**
```json
{ "userId": 20, "countryCode": "CH", "adminLevel": 1, "managedShowIds": [3] }
```

---

### DELETE `/api/admins/{id}`
Remove an admin.

**Response `204 No Content`**

---

## 7. Votes

### GET `/api/votes`
List all vote log entries.

**Response `200 OK`:**
```json
[
  { "voteLogId": 1, "songId": 1, "juryId": 1, "citizenId": null },
  { "voteLogId": 2, "songId": 1, "juryId": null, "citizenId": 10 }
]
```

---

### POST `/api/votes`
Cast a vote. Implements the EERM **"Votes"** relationship.

**Rules:**
- `songId` is required
- Exactly **one** of `juryId` or `citizenId` must be set — not both, not neither
- Both IDs must reference existing records

**Jury vote:**
```json
{ "songId": 1, "juryId": 1, "citizenId": null }
```

**Public (citizen) vote:**
```json
{ "songId": 1, "juryId": null, "citizenId": 10 }
```

**Response `201 Created`:**
```json
{ "voteLogId": 5, "songId": 1, "juryId": 1, "citizenId": null }
```

**Response `400 Bad Request`** if both or neither voter IDs are set.

---

## 8. Scores

### GET `/api/scores`
List all scores — effectively the leaderboard.

**Response `200 OK`:**
```json
[
  { "scoreId": 1, "songScore": 243, "songId": 1 },
  { "scoreId": 2, "songScore": 365, "songId": 2 }
]
```

---

### GET `/api/scores/song/{songId}`
Get the current score for a specific song.

**Example:** `GET /api/scores/song/1`

**Response `200 OK`:**
```json
{ "scoreId": 1, "songScore": 243, "songId": 1 }
```

---

### POST `/api/scores/calculate/{songId}`
Trigger score calculation for a song. Implements the EERM **"Calculate"** relationship.
Aggregates all VoteLog entries for the song and writes/updates the Score.

> **Note:** Requires `VoteLog.points` field to be implemented first (see TODO in `ScoreService`).

**Example:** `POST /api/scores/calculate/1`

**Response `200 OK`:**
```json
{ "scoreId": 1, "songScore": 243, "songId": 1 }
```

---

## 9. Suggested Extra Queries

These are not yet implemented but are the most useful next endpoints to add.
The `// TODO` comments in the service files mark exactly where to add them.

### Songs by country
```
GET /api/songs?countryCode=SE
```
Add to `SongRepository`:
```java
List<Song> findByCountry_CountryCode(String countryCode);
```

### Songs by show
```
GET /api/shows/{showId}/songs
```
Add to `ShowController` → `ShowService` → `ShowRepository`.

### Votes by song (all votes for one entry)
```
GET /api/votes?songId=1
```
Add to `VoteLogRepository`:
```java
List<VoteLog> findBySong_SongId(Long songId);
```

### Votes by jury member
```
GET /api/votes?juryId=1
```
Add to `VoteLogRepository`:
```java
List<VoteLog> findByJury_UserId(Long juryId);
```

### Leaderboard (scores sorted descending)
```
GET /api/scores?sort=songScore,desc
```
Spring Data JPA supports this automatically via `JpaRepository` with `Sort` parameter.
Add to `ScoreController`:
```java
@GetMapping("/leaderboard")
public ResponseEntity<List<ScoreResponseDTO>> getLeaderboard() {
    return ResponseEntity.ok(scoreService.getLeaderboard());
}
```
Add to `ScoreService`:
```java
public List<ScoreResponseDTO> getLeaderboard() {
    return scoreRepository.findAll(Sort.by(Sort.Direction.DESC, "songScore"))
            .stream().map(scoreFactory::toResponseDTO).collect(Collectors.toList());
}
```

### Assign a song to a show
```
POST /api/shows/{showId}/songs/{songId}
```
Add to `ShowController` → `ShowService`:
```java
public ShowResponseDTO addSongToShow(Long showId, Long songId) {
    Show show = showRepository.findById(showId).orElseThrow(...);
    Song song = songRepository.findById(songId).orElseThrow(...);
    show.getSongs().add(song);
    return showFactory.toResponseDTO(showRepository.save(show));
}
```

---

## 10. Error Responses

Currently errors return Spring's default `500` with a message in the body.
To return proper HTTP codes, add a `@ControllerAdvice` class:

```java
// src/main/java/com/dhbw/eurovision/controller/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {
        if (ex.getMessage().contains("not found")) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
        return ResponseEntity.status(500).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }
}
```

---

## 11. cURL Quick-Reference

```bash
BASE=http://localhost:8080/api

# ── Countries ──────────────────────────────────────────────
curl $BASE/countries
curl $BASE/countries/SE
curl -X POST $BASE/countries \
  -H "Content-Type: application/json" \
  -d '{"countryCode":"SE","countryName":"Sweden"}'
curl -X DELETE $BASE/countries/SE

# ── Songs ──────────────────────────────────────────────────
curl $BASE/songs
curl $BASE/songs/1
curl -X POST $BASE/songs \
  -H "Content-Type: application/json" \
  -d '{"singerName":"KAJ","countryCode":"SE"}'
curl -X DELETE $BASE/songs/1

# ── Jury ───────────────────────────────────────────────────
curl $BASE/jury
curl -X POST $BASE/jury \
  -H "Content-Type: application/json" \
  -d '{"countryCode":"DE","professionalBg":"Music Producer"}'

# ── Citizens ───────────────────────────────────────────────
curl $BASE/citizens
curl -X POST $BASE/citizens \
  -H "Content-Type: application/json" \
  -d '{"countryCode":"SE"}'

# ── Shows ──────────────────────────────────────────────────
curl $BASE/shows
curl -X POST $BASE/shows \
  -H "Content-Type: application/json" -d '{}'
curl -X POST $BASE/admins/1/shows/1   # assign admin to show

# ── Votes ──────────────────────────────────────────────────
curl $BASE/votes
# Cast jury vote:
curl -X POST $BASE/votes \
  -H "Content-Type: application/json" \
  -d '{"songId":1,"juryId":1}'
# Cast public vote:
curl -X POST $BASE/votes \
  -H "Content-Type: application/json" \
  -d '{"songId":1,"citizenId":10}'

# ── Scores ─────────────────────────────────────────────────
curl $BASE/scores
curl $BASE/scores/song/1
curl -X POST $BASE/scores/calculate/1
```
