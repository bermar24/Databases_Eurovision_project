# Eurovision Project - REST API Documentation (Current Implementation)

> **Base URL:** `http://localhost:8080/api`  
> **Content-Type:** `application/json`  
> **Auth:** not implemented yet

---

## 1. Countries

### GET `/api/countries`

List all countries.

### GET `/api/countries/{code}`

Get one country by ISO code.

### POST `/api/countries`

Create country.

Request:
```json
{ "countryCode": "SE", "countryName": "Sweden" }
```

### DELETE `/api/countries/{code}`

Delete country.

---

## 2. Songs

### GET `/api/songs`

List songs.

### GET `/api/songs/{id}`

Get one song.

### POST `/api/songs`

Create song.

Request:
```json
{ "singerName": "KAJ", "countryCode": "SE" }
```

### DELETE `/api/songs/{id}`

Delete song.

---

## 3. Shows

### GET `/api/shows`

List shows.

### GET `/api/shows/{id}`

Get one show.

### POST `/api/shows`

Create show.

Request:
```json
{ "showName": "Grand Final" }
```

### POST `/api/shows/{showId}/songs/{songId}`

Assign song to show (`show_song` M:N).

### DELETE `/api/shows/{id}`

Delete show.

---

## 4. Jury

### GET `/api/jury`

List jury members.

### GET `/api/jury/{id}`

Get jury member by user id.

### POST `/api/jury`

Create jury member.

Request:
```json
{ "countryCode": "DE", "professionalBg": "Music Producer" }
```

### DELETE `/api/jury/{id}`

Delete jury member.

---

## 5. Citizens

### GET `/api/citizens`

List citizens.

### GET `/api/citizens/{id}`

Get citizen by user id.

### GET `/api/citizens/by-phone/{phoneNumber}`

Lookup citizen by phone number.

### POST `/api/citizens`

Find or create citizen by phone number.

Request:
```json
{ "countryCode": "SE", "phoneNumber": "+46700000000" }
```

Behavior:
- if phone already exists, existing citizen is returned
- for new phone numbers, `countryCode` is required

### DELETE `/api/citizens/{id}`

Delete citizen.

---

## 6. Admins

### GET `/api/admins`

List admins.

### GET `/api/admins/{id}`

Get admin by user id.

### POST `/api/admins`

Create admin.

Request:
```json
{ "countryCode": "CH", "adminLevel": 1 }
```

### POST `/api/admins/{adminId}/shows/{showId}`

Assign admin to manage show (`admin_show` M:N).

### DELETE `/api/admins/{id}`

Delete admin.

---

## 7. Votes

### GET `/api/votes`

List vote rows (`VoteLogResponseDTO`).

Each row includes:
- `voteLogId`
- `songId`
- `showId`
- `juryId` or `citizenId`
- `points`
- `voteSessionId`

### POST `/api/votes/session`

Submit one complete ballot of 10 entries.

Request (`BallotRequestDTO`):
```json
{
  "showId": 3,
  "juryId": 1,
  "citizenId": null,
  "votes": [
    { "songId": 11, "points": 12 },
    { "songId": 2,  "points": 10 },
    { "songId": 7,  "points": 8 },
    { "songId": 9,  "points": 7 },
    { "songId": 15, "points": 6 },
    { "songId": 3,  "points": 5 },
    { "songId": 8,  "points": 4 },
    { "songId": 6,  "points": 3 },
    { "songId": 4,  "points": 2 },
    { "songId": 5,  "points": 1 }
  ]
}
```

Rules enforced in service:
- exactly one of `juryId` or `citizenId`
- exactly 10 vote entries
- point values must be `{12,10,8,7,6,5,4,3,2,1}` exactly once
- voter can vote only once per show
- no voting for own country
- voted songs must be in the selected show
- in semi-finals, voter country must participate in that show

Response (`201 Created`):
```json
{
  "voteSessionId": "7f9d4f8e-2cd7-4b62-a5aa-f267f86cf0c1",
  "showId": 3,
  "entries": [
    { "voteLogId": 101, "songId": 11, "showId": 3, "juryId": 1, "citizenId": null, "points": 12, "voteSessionId": "7f9d4f8e-2cd7-4b62-a5aa-f267f86cf0c1" }
  ]
}
```

---

## 8. Scores

Score is stored per `(song, show)` and returned with extra context fields.

### GET `/api/scores`

All scores across all shows.

### GET `/api/scores/show/{showId}`

Leaderboard for one show (sorted descending by `songScore`).

### GET `/api/scores/song/{songId}`

All show-specific scores for one song.

### POST `/api/scores/calculate-show/{showId}`

Recalculate full show:
- Grand Final: jury aggregation + citizen aggregation
- Semi-finals: citizen aggregation only

### POST `/api/scores/calculate/{showId}/{songId}`

Recalculate one song in one show (quick direct sum mode).

### POST `/api/scores/calculate-all`

Recalculate all shows.

Example score response:
```json
{
  "scoreId": 1,
  "songScore": 243,
  "songId": 1,
  "showId": 3,
  "showName": "Grand Final",
  "singerName": "KAJ",
  "countryCode": "SE"
}
```

---

## 9. Error Responses (Current State)

- No global `@ControllerAdvice` is implemented yet
- many service errors are thrown as `RuntimeException` or `IllegalArgumentException`
- `CitizenService` also uses `ResponseStatusException` (`400`/`404`) for some cases
- because there is no global exception mapping, response codes are currently inconsistent

---

## 10. Quick cURL Reference

```bash
BASE=http://localhost:8080/api

curl "$BASE/countries"
curl "$BASE/songs"
curl "$BASE/shows"

curl -X POST "$BASE/citizens" \
  -H "Content-Type: application/json" \
  -d '{"countryCode":"SE","phoneNumber":"+46700000000"}'

curl "$BASE/citizens/by-phone/+46700000000"

curl -X POST "$BASE/shows/1/songs/10"

curl -X POST "$BASE/votes/session" \
  -H "Content-Type: application/json" \
  -d '{"showId":1,"citizenId":10,"votes":[{"songId":2,"points":12},{"songId":3,"points":10},{"songId":4,"points":8},{"songId":5,"points":7},{"songId":6,"points":6},{"songId":7,"points":5},{"songId":8,"points":4},{"songId":9,"points":3},{"songId":11,"points":2},{"songId":12,"points":1}]}'

curl -X POST "$BASE/scores/calculate-show/1"
curl "$BASE/scores/show/1"
```
