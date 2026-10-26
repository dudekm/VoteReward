# Development

[← Contents](../README.md) · [Polski](pl/development.md)

## Requirements

- **Docker** with Compose — enough for everything below, or
- **JDK 17+** and **Maven 3.9+**, if you prefer to build without Docker.

## Building

```bash
# In Docker: tests + jar (also copied to docker/plugins/ for the test server)
docker compose run --rm build

# Locally
mvn verify
```

Output: `target/VoteReward-<version>.jar` (the `original-…jar` file is the version without bundled libraries — ignore it).

## Test server with a mock API

`compose.yaml` starts:

- `mock-api` — WireMock imitating `GET /api/v1/servers/{uuid}/votes`; returns a single vote from player
  **DevPlayer** with the current date (`docker/mock-api/mappings/votes.json`),
- `minecraft` — an [itzg/minecraft-server](https://docker-minecraft-server.readthedocs.io/) server
  with the plugin and a `settings.yml` pointing at the mock (`docker/plugins/VoteReward/settings.yml`, checks every 10 s).

```bash
docker compose run --rm build      # build the jar
docker compose up -d minecraft     # server + mock API
docker compose logs -f minecraft   # console
docker compose attach minecraft    # interactive console (detach: Ctrl+P, Ctrl+Q)
```

Connect to `localhost:25565` as **DevPlayer** (the server runs in offline mode, so no Mojang account is
needed). Within 10 s you'll receive a diamond.

### Different server software or version

```bash
MC_TYPE=SPIGOT MC_VERSION=1.18.2 MC_JAVA=java17 docker compose up -d minecraft
MC_TYPE=PAPER  MC_VERSION=1.21.4 MC_JAVA=java21 docker compose up -d minecraft
MC_TYPE=FOLIA  MC_VERSION=1.21.4 MC_JAVA=java21 docker compose up -d minecraft
MC_TYPE=PAPER  MC_VERSION=26.3   MC_JAVA=java25 docker compose up -d minecraft
```

| Minecraft version | `MC_JAVA` |
|---|---|
| 1.18 – 1.20.4 | `java17` |
| 1.20.5 – 1.21.x | `java21` |
| 26.x | `java25` |

When switching versions, delete the world to avoid conversion: `docker compose down -v`.

### Mock API

- Change the nickname or add votes in `docker/mock-api/mappings/votes.json`, then reload:
  `curl -X POST localhost:8089/__admin/mappings/reset`.
- Preview the response: `curl 'localhost:8089/api/v1/servers/183413bc-4588-4265-8f06-346963e5d7df/votes'`.
- The same player is rewarded once per UTC day. To test again: stop the server and delete
  `plugins/VoteReward/data/ledger.yml` from the volume (`docker compose down -v` wipes everything).

### A real list instead of the mock

In `docker/plugins/VoteReward/settings.yml` set `address` to the list's address (or a locally running
list application, e.g. `http://host.docker.internal:8080`) and a real `uuid`.

## Conventions

- Domain logic doesn't import `org.bukkit` — everything that touches the server lives in the `bukkit` package.
- Java 17 (the oldest required by the supported Minecraft versions) — no APIs from newer JDKs.
- Server API: only methods present in Spigot 1.18.2 and not removed up to 26.3 (compilation targets 1.18.2).
- New configuration value → default in code, entry in `settings.yml`, test in `SettingsParserTest`
  and a description in [configuration](configuration.md).
