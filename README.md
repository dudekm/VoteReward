# VoteReward

[Polski](README-PL.md)

A plugin that rewards players for voting for your server on server lists
([lista-minecraft.pl](https://lista-minecraft.pl), [lista-serwerow.emecz.pl](https://lista-serwerow.emecz.pl),
[minecraft-list.info](https://minecraft-list.info)). Every 30 seconds (configurable) it checks the lists' APIs for new
votes and automatically runs the reward commands — right away, or the next time the player joins the server.

- **Minecraft:** 1.18 – 26.3
- **Server software:** Spigot, Paper, Purpur, Folia and others compatible with the Bukkit API (see [installation](docs/installation.md#supported-servers))
- **Java:** 17+ (whatever your Minecraft version requires)
- Statistics: [bStats](https://bstats.org/plugin/bukkit/VoteReward/20120)

## Documentation

| | |
|---|---|
| [Installation](docs/installation.md) | requirements, supported servers, upgrading from 3.x |
| [Configuration](docs/configuration.md) | `settings.yml`, `messages.yml`, commands and permissions |
| [How it works](docs/architecture.md) | API polling, reward rules, code structure |
| [Development](docs/development.md) | building, Docker, test server with a mock API |
| [Testing](docs/testing.md) | running tests, what is covered, writing new ones |
| [Release](docs/release.md) | publishing a new version |

## Quick start

1. Download `VoteReward-<version>.jar` from [Releases](https://github.com/dudekm/VoteReward/releases/latest) and put it in `plugins/`.
2. Start the server, then in `plugins/VoteReward/settings.yml` enter your server's `uuid` from each list and the reward commands.
3. Run `/votereward reload`.
