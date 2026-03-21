# Lotto Simulator — Backend API

Spring Boot REST API powering the Lotto Simulator platform. Handles authentication, multi-game betting, draw settlement, wallet management, player profiles, and real-time PCSO result scraping.

---

## Overview

| Concern | Details |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.12 |
| Database | MySQL 8+ (`lottodb`) |
| Port | 8099 |
| Auth | BCrypt password hashing, `X-User-Id` header |
| CORS | All origins allowed on `/api/**` (dev mode) |

---

## Features

- User registration, login, and demo session (no account required)
- 9 PCSO-style lotto games with configurable draw schedules
- Bet placement with automatic next-draw-slot calculation
- Lazy bet settlement triggered on fetch after draw time
- Admin result import via pasted PCSO text (any format)
- PCSO scraper: parses multi-draw, multi-game, multi-format input
- Automatic re-settlement when official results are updated
- Wallet: deposit, withdraw, funding history (min ₱50)
- Player profile stats: win rate, best match, lucky numbers
- Admin role enforcement on result management endpoints

---

## Game Catalog

| Game ID | Name | Numbers | Draw Times | Draw Days | Jackpot Multiplier |
|---|---|---|---|---|---|
| `ultra-658` | Ultra Lotto 6/58 | Pick 6 of 58 | 9PM | Tue, Fri, Sun | 50,000× |
| `grand-655` | Grand Lotto 6/55 | Pick 6 of 55 | 9PM | Mon, Wed, Sat | 5,000× |
| `super-649` | Super Lotto 6/49 | Pick 6 of 49 | 9PM | Tue, Thu, Sun | 500× |
| `mega-645` | Mega Lotto 6/45 | Pick 6 of 45 | 9PM | Mon, Wed, Fri | 50× |
| `lotto-642` | Lotto 6/42 | Pick 6 of 42 | 9PM | Tue, Thu, Sat | — |
| `6digit` | 6-Digit Lotto | 6 digits (0–9) | 9PM | Tue, Thu, Sat | — |
| `4digit` | 4-Digit Lotto | 4 digits | 9PM | Mon, Wed, Fri | 10,000× |
| `3d-swertres` | 3D Lotto (Swertres) | 3 digits | 2PM, 5PM, 9PM | Daily | 450× |
| `2d-ez2` | 2D Lotto (EZ2) | 2 of 45 | 2PM, 5PM, 9PM | Daily | 4,000× |

---

## Payout Rates

| Game | Condition | Multiplier |
|---|---|---|
| 2D / EZ2 | 2 exact matches | 4,000× stake |
| 3D / Swertres | 3 exact matches | 450× stake |
| 4D | 4 exact matches | 10,000× stake |
| 6-number games | 6 matches | 50,000× stake |
| 6-number games | 5 matches | 5,000× stake |
| 6-number games | 4 matches | 500× stake |
| 6-number games | 3 matches | 50× stake |

---

## API Reference

Base URL: `http://localhost:8099/api`

Authentication: login/register return a `userId`. Pass it as `X-User-Id` header on protected endpoints.

### Auth — `/api/auth`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/login` | Returns `{userId, username, displayName, role, demo}` |
| POST | `/register` | Creates account, returns session |
| POST | `/demo` | Returns demo-player session (no credentials needed) |
| GET | `/hash` | Generates BCrypt hash for a given password |

### Games — `/api/games`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/games` | List all games; optional `?day=today` or `?day=1-7` filter |
| GET | `/games/results` | Each game with all draw results and winner counts |

### Bets & Wallet — `/api/bets`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/bets` | Place a bet |
| GET | `/bets` | Active pending bets (triggers settlement if draw passed) |
| GET | `/bets/history` | Settled bet history (won / lost) |
| GET | `/bets/unclaimed` | Winning bets not yet claimed |
| POST | `/bets/claim` | Mark a winning bet as claimed |
| GET | `/bets/balance` | Current wallet balance |
| POST | `/bets/balance` | Deposit or withdraw (min ₱50) |
| GET | `/bets/funding` | Funding transaction history |

### Profile — `/api/profile`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/profile` | Player stats: totalPlays, prizesWon, bestMatch, winRate, luckyNumbers |

### Admin — `/api/admin` _(role=admin required)_

| Method | Endpoint | Description |
|---|---|---|
| POST | `/admin/import-manual` | Import results from pasted PCSO text |
| POST | `/admin/results` | Add a single official result |
| GET | `/admin/results` | List all official results |
| DELETE | `/admin/results/{id}` | Delete a result |

---

## PCSO Scraper

The `PcsoScraperService` parses raw pasted text from the PCSO website into structured draw results.

Supported input formats:
- Multi-game blocks with game name, date, and draw lines
- Inline multi-draw: `2PM: 11-04 5PM: 29-16 9PM: 08-31`
- Game + date on the same line
- Date formats: `March 20, 2026` / `Mar 20 2026` / `03/20/2026` / `2026-03-20`
- Falls back to today's date if none found
- STL games are automatically filtered out

On import, each parsed result upserts an `OfficialResult` record and triggers `BetService.settleByResult`, which re-settles all bets for that draw slot (reversing old payouts and applying new ones).

---

## Bet Lifecycle

```
1. User places bet → stake deducted, Bet(status=pending) created
2. User fetches /bets after draw time → settleIfNeeded() runs
   → looks up OfficialResult → calculates matches + payout
   → updates Bet(status=won|lost), credits balance
3. Admin imports result → settleByResult() re-settles ALL bets for that slot
   → reverses old payouts, applies new ones
4. User claims winning bet → POST /bets/claim → bet.claimed = true
```

---

## Local Setup

### Prerequisites
- Java 21
- MySQL 8+
- Maven (or use the included `mvnw` wrapper)

### 1) Create the database

```sql
CREATE DATABASE lottodb;
```

### 2) Configure credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lottodb
spring.datasource.username=your_user
spring.datasource.password=your_password
```

### 3) Run the API

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw spring-boot:run
```

Schema and seed data are applied automatically on first run via `schema.sql`.

### 4) Run tests

```bash
./mvnw test        # macOS / Linux
.\mvnw test        # Windows PowerShell
```

---

## Seed Accounts

| Username | Role | Starting Balance |
|---|---|---|
| `demo-player` | user | ₱5,000 |
| `admin` | admin | ₱999,999 |

---

## Example Requests

Register:
```bash
curl -X POST http://localhost:8099/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"secret123","displayName":"Player One"}'
```

Login:
```bash
curl -X POST http://localhost:8099/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"secret123"}'
```

Place bet:
```bash
curl -X POST http://localhost:8099/api/bets \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"gameId":"ultra-658","numbers":[5,12,23,34,41,55],"stake":20}'
```

Import PCSO results (admin):
```bash
curl -X POST http://localhost:8099/api/admin/import-manual \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{"rawData":"Ultra Lotto 6/58\nMarch 22, 2026\n9PM: 05-12-23-34-41-55"}'
```

---

## Architecture

```
src/main/java/com/lotto/
  controller/     # HTTP routing (Auth, Game, Bet, Profile, Admin)
  service/        # Business logic (BetService, PcsoScraperService, ProfileService, AuthService)
  repository/     # Spring Data JPA interfaces
  entity/         # Domain models (User, Bet, Balance, LottoGame, OfficialResult, FundingTransaction)
  config/         # Security and CORS configuration
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for full service method reference, entity schemas, and repository query details.

---

## Notes

- CORS is open for all origins in dev. Narrow this for production.
- Security is permissive (demo mode). Add token-based auth and environment secrets before any public deployment.
- Passwords are always BCrypt-hashed regardless of demo mode.

---

## Related

Frontend repo: [`../lottosimulator`](../lottosimulator/README.md) — Expo SDK 54, React Native 0.81, TypeScript
