# Testy

[← Spis treści](../../README-PL.md) · [English](../testing.md)

## Uruchamianie

```bash
mvn test                                  # wszystkie testy
mvn test -Dtest=VoteLedgerTest            # jedna klasa
mvn test -Dtest='VoteLedgerTest#nextUtcDayEarnsAgain'
docker compose run --rm build             # to samo w Dockerze (mvn verify)
```

Testy używają JUnit 5 i nie potrzebują serwera Minecraft ani internetu.

## CI

GitHub Actions (`.github/workflows/tests.yml`) uruchamia `mvn -B verify` na Javie 17, 21 i 25 przy każdym pull requeście
i każdym pushu na branch inny niż `main`. Przy niepowodzeniu raporty testów są dołączane do przebiegu jako artefakt.

## Co jest testowane

| Obszar | Testy |
|---|---|
| Value Objecty | `NicknameTest`, `ServerIdTest`, `VoteTest`, `ListAddressTest`, `RewardCommandTest`, `PlaceholdersTest` |
| Konfiguracja | `SettingsParserTest` (w tym zgodność z plikami 3.x), `DurationsTest` |
| API list | `VotePageParserTest` (JSON-LD, `hydra:`, zwykły JSON, błędne dane), `ListApiVoteSourceTest` (parametry zapytania, paginacja, błędy HTTP), `ListApiVoteSourceIntegrationTest` (prawdziwe HTTP na lokalnym serwerze) |
| Zasady nagród | `VoteLedgerTest` (kursor, zakładka, raz dziennie, deduplikacja, wygasanie), `RewardServiceTest`, `VotePollerTest` |
| Zapis stanu | `YamlLedgerRepositoryTest` |

Warstwa `bukkit` jest celowo cienka (wywołania API serwera) i sprawdzana ręcznie na serwerze testowym
z [developmentu](development.md#serwer-testowy-z-atrapą-api).

## Pisanie testów

- Zależności zewnętrzne zastępuj prostymi atrapami interfejsów (`FakeHttpTransport`, `MapConfigView`,
  lambdy dla `RewardDispatcher`/`PlayerDirectory`) — bez bibliotek do mockowania.
- Czas: wstrzykuj `Clock` / podawaj `Instant` jawnie, nigdy `Instant.now()` w asercjach.
- Wspólne dane testowe: `TestData` (lista, głos i strona w formacie API).

## Testy ręczne przed wydaniem

Na serwerze z `compose.yaml` (patrz [development](development.md)) sprawdź co najmniej:

| Silnik | Wersja |
|---|---|
| Spigot | 1.18.2 |
| Paper | najnowsza 1.21.x |
| Folia | najnowsza dostępna |
| Paper | 26.3 |

Scenariusze: nagroda dla gracza online, nagroda po wejściu (głos oddany offline), `/vr check`,
`/vr reload` ze zmianą `polling.interval`, lista niedostępna (zatrzymaj `mock-api`) i jej powrót.
