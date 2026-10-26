# Development

[← Spis treści](../../README-PL.md) · [English](../development.md)

## Wymagania

- **Docker** z Compose — wystarczy do wszystkiego poniżej, albo
- **JDK 17+** i **Maven 3.9+**, jeśli wolisz budować bez Dockera.

## Budowanie

```bash
# W Dockerze: testy + jar (kopiowany też do docker/plugins/ dla serwera testowego)
docker compose run --rm build

# Lokalnie
mvn verify
```

Wynik: `target/VoteReward-<wersja>.jar` (plik `original-…jar` to wersja bez dołączonych bibliotek — pomiń go).

## Serwer testowy z atrapą API

`compose.yaml` uruchamia:

- `mock-api` — WireMock udający `GET /api/v1/servers/{uuid}/votes`; zwraca jeden głos gracza
  **DevPlayer** z bieżącą datą (`docker/mock-api/mappings/votes.json`),
- `minecraft` — serwer [itzg/minecraft-server](https://docker-minecraft-server.readthedocs.io/)
  z pluginem i `settings.yml` wskazującym na atrapę (`docker/plugins/VoteReward/settings.yml`, sprawdzanie co 10 s).

```bash
docker compose run --rm build      # zbuduj jar
docker compose up -d minecraft     # serwer + atrapa API
docker compose logs -f minecraft   # konsola
docker compose attach minecraft    # konsola interaktywna (wyjście: Ctrl+P, Ctrl+Q)
```

Połącz się z `localhost:25565` jako **DevPlayer** (serwer działa w trybie offline, więc konto Mojang nie jest
potrzebne). Po maks. 10 s dostaniesz diament.

### Inny silnik lub wersja

```bash
MC_TYPE=SPIGOT MC_VERSION=1.18.2 MC_JAVA=java17 docker compose up -d minecraft
MC_TYPE=PAPER  MC_VERSION=1.21.4 MC_JAVA=java21 docker compose up -d minecraft
MC_TYPE=FOLIA  MC_VERSION=1.21.4 MC_JAVA=java21 docker compose up -d minecraft
MC_TYPE=PAPER  MC_VERSION=26.3   MC_JAVA=java25 docker compose up -d minecraft
```

| Wersja Minecrafta | `MC_JAVA` |
|---|---|
| 1.18 – 1.20.4 | `java17` |
| 1.20.5 – 1.21.x | `java21` |
| 26.x | `java25` |

Przy zmianie wersji usuń świat, żeby uniknąć konwersji: `docker compose down -v`.

### Atrapa API

- Zmień nick lub dodaj głosy w `docker/mock-api/mappings/votes.json`, potem przeładuj:
  `curl -X POST localhost:8089/__admin/mappings/reset`.
- Podgląd odpowiedzi: `curl 'localhost:8089/api/v1/servers/183413bc-4588-4265-8f06-346963e5d7df/votes'`.
- Ten sam gracz dostaje nagrodę raz na dzień UTC. Żeby przetestować ponownie: zatrzymaj serwer i usuń
  `plugins/VoteReward/data/ledger.yml` z wolumenu (`docker compose down -v` czyści wszystko).

### Prawdziwa lista zamiast atrapy

W `docker/plugins/VoteReward/settings.yml` ustaw `address` na adres listy (lub lokalnie uruchomionej
aplikacji listy, np. `http://host.docker.internal:8080`) i prawdziwe `uuid`.

## Konwencje

- Logika domenowa nie importuje `org.bukkit` — wszystko, co dotyka serwera, jest w pakiecie `bukkit`.
- Java 17 (najstarsza wymagana przez wspierane wersje Minecrafta) — bez API z nowszych JDK.
- API serwera: tylko metody obecne w Spigot 1.18.2 i nieusunięte do 26.3 (kompilacja odbywa się na 1.18.2).
- Nowa wartość konfiguracji → domyślna wartość w kodzie, wpis w `settings.yml`, test w `SettingsParserTest`
  i opis w [konfiguracji](configuration.md).
