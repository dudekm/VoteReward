# Installation

[← Contents](../README.md) · [Polski](pl/installation.md)

## Requirements

- A Minecraft **1.18 – 26.3** server based on the Bukkit API (see below).
- **Java 17 or newer** — in practice whatever your Minecraft version already requires
  (1.18–1.20.4: Java 17, 1.20.5–1.21.x: Java 21, 26.x: Java 25).
- Outbound internet access (HTTPS) to the addresses of the lists your server is on.

The plugin needs no additional libraries or plugins.

## Supported servers

| Software | Support | Notes |
|---|---|---|
| Spigot / CraftBukkit | ✅ | |
| Paper and forks (Purpur, Pufferfish…) | ✅ | |
| Folia | ✅ | detected automatically, uses region schedulers |
| Hybrids (Mohist, Arclight…) | ⚠️ | should work since they load Bukkit plugins — untested |
| Vanilla (Mojang's server) | ❌ | vanilla doesn't load plugins — use e.g. Paper (a drop-in replacement for vanilla) |
| Fabric / NeoForge / Sponge | ❌ | different modding APIs; out of scope for this plugin |

The plugin is compiled against the oldest supported API (Spigot 1.18.2) and only uses methods
available in all versions up to 26.3, so **a single `.jar` works on every version**.

## Step-by-step installation

1. Download the latest `VoteReward-<version>.jar` from
   [Releases](https://github.com/dudekm/VoteReward/releases/latest).
2. Put the file in the server's `plugins/` folder and start (restart) the server.
3. A `plugins/VoteReward/` folder is created with `settings.yml` and `messages.yml`.
4. In `settings.yml`, under `servers`, set your server's `uuid` on each list and the reward commands
   — see [configuration](configuration.md).
5. Run `/votereward reload` (or restart the server). The console will show
   `Checking N vote list(s) every 30s.`

## Upgrading from 3.x

- A 3.x `settings.yml` works unchanged (the `servers` section has the same format).
  A missing `polling.interval` defaults to `30s`.
- **Rewards are now given automatically** — players no longer have to type `/votereward` after voting.
  The command still exists and claims any pending rewards.
- `messages.yml` has new keys and uses `&` colour codes (instead of `<red>`). The simplest option is to delete
  the old file — a new one is created on startup. Missing keys are taken from the default file in the `.jar` anyway.
- The 3.x `data/votes.yml` file is no longer used and can be deleted; the new data lives in `data/ledger.yml`.
- `votereward.check` is now available to players by default (viewing their own votes);
  viewing other players requires `votereward.check.others`.
