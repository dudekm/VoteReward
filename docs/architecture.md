# How it works

[← Contents](../README.md) · [Polski](pl/architecture.md)

## Flow

```
every polling.interval (async thread)
  └─ VotePoller: for each list in settings.yml
       ├─ VoteLedger.checkFrom()       → where to query from
       ├─ ListApiVoteSource            → GET /api/v1/servers/{uuid}/votes?createdAt[after]=…
       ├─ VoteLedger.record()          → new votes → pending rewards
       └─ RewardService.rewardsEarned()→ online players get rewards right away
player join         → RewardService.deliver()   → pending rewards
/votereward         → RewardService.deliver()
```

## Polling the API

Endpoint: `GET {address}/api/v1/servers/{uuid}/votes` (API Platform, JSON-LD format).

Parameters sent by the plugin:

- `createdAt[after]=<instant>` — only votes since the given moment,
- `order[createdAt]=asc` — oldest first, so pages can be read in order.

Further pages are fetched from `view.next` (max. 20 pages per check; the rest is fetched by the next
check, since it starts from the last vote read). Links to a different domain are ignored.

Where to query from:

- first check of a list: the last **1 hour**,
- subsequent checks: from the previous check **minus a 5-minute overlap** — a vote saved with a slight delay
  or affected by clock skew won't be lost; votes read twice are recognised by their `uuid`,
- after a longer break (e.g. the server was offline): at most **24 hours** back.

In practice each request covers a few minutes, so the load on the list is minimal.
A list that doesn't respond is logged once (and once when it comes back), not every 30 seconds.

## State (`data/ledger.yml`)

`VoteLedger` remembers: the read position for each list, who has been rewarded on a given day, and
rewards waiting for players. It is saved after every change (written to a temporary file and swapped in —
a crash mid-write doesn't corrupt the file).

## Code structure

Packages with no Bukkit dependency (unit tested):

| Package | Contents |
|---|---|
| `vote` | Value Objects: `ServerId`, `Nickname`, `Vote`, `VoteDay` |
| `site` | list configuration: `VoteSite`, `ListAddress`, `RewardCommand` |
| `config` | `Settings`, `SettingsParser`, `Durations` |
| `api` | list API client: `ListApiVoteSource`, `VotePageParser`, `HttpTransport` |
| `ledger` | `VoteLedger` (reward rules), `YamlLedgerRepository` |
| `reward` | `VotePoller`, `RewardService` and the `RewardDispatcher`, `PlayerDirectory` ports |

Server layer — the `bukkit` package and the `VoteReward` class (entry point that wires everything together):
command, join listener, reward dispatching, messages, and `TaskScheduler` with two implementations
(Bukkit, and Folia via reflection — the Folia API is not part of the Spigot API).

Dependencies point one way: `bukkit` → logic, never the reverse. The logic talks to the outside world through
interfaces (`VoteSource`, `HttpTransport`, `LedgerRepository`, `RewardDispatcher`, `PlayerDirectory`,
`ConfigView`), so tests can plug in simple fakes.
