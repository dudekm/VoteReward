# Testing

[← Contents](../README.md) · [Polski](pl/testing.md)

## Running

```bash
mvn test                                  # all tests
mvn test -Dtest=VoteLedgerTest            # a single class
mvn test -Dtest='VoteLedgerTest#nextUtcDayEarnsAgain'
docker compose run --rm build             # the same in Docker (mvn verify)
```

The tests use JUnit 5 and need neither a Minecraft server nor internet access.

## CI

GitHub Actions (`.github/workflows/tests.yml`) runs `mvn -B verify` on Java 17, 21 and 25 for every pull request
and every push to a branch other than `main`. If a run fails, the test reports are attached to it as an artifact.

## What is tested

| Area | Tests |
|---|---|
| Value Objects | `NicknameTest`, `ServerIdTest`, `VoteTest`, `ListAddressTest`, `RewardCommandTest`, `PlaceholdersTest` |
| Configuration | `SettingsParserTest` (including compatibility with 3.x files), `DurationsTest` |
| List API | `VotePageParserTest` (JSON-LD, `hydra:`, plain JSON, malformed data), `ListApiVoteSourceTest` (query parameters, pagination, HTTP errors), `ListApiVoteSourceIntegrationTest` (real HTTP against a local server) |
| Reward rules | `VoteLedgerTest` (cursor, overlap, once a day, deduplication, expiry), `RewardServiceTest`, `VotePollerTest` |
| State persistence | `YamlLedgerRepositoryTest` |

The `bukkit` layer is deliberately thin (server API calls) and is checked manually on the test server
from [development](development.md#test-server-with-a-mock-api).

## Writing tests

- Replace external dependencies with simple interface fakes (`FakeHttpTransport`, `MapConfigView`,
  lambdas for `RewardDispatcher`/`PlayerDirectory`) — no mocking libraries.
- Time: inject a `Clock` / pass an `Instant` explicitly, never `Instant.now()` in assertions.
- Shared test data: `TestData` (a list, a vote and a page in API format).

## Manual tests before a release

On the server from `compose.yaml` (see [development](development.md)) check at least:

| Software | Version |
|---|---|
| Spigot | 1.18.2 |
| Paper | latest 1.21.x |
| Folia | latest available |
| Paper | 26.3 |

Scenarios: reward for an online player, reward on join (vote cast while offline), `/vr check`,
`/vr reload` with a changed `polling.interval`, list unavailable (stop `mock-api`) and its recovery.
