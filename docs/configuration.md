# Configuration

[← Contents](../README.md) · [Polski](pl/configuration.md)

The files are in `plugins/VoteReward/`. After making changes: `/votereward reload`.

## settings.yml

```yaml
polling:
  interval: 30s

servers:
  lista-minecraft:
    address: https://lista-minecraft.pl
    uuid: 183413bc-4588-4265-8f06-346963e5d7df
    reward-commands:
      - xp add %player_name% 100
      - give %player_name% diamond
```

### `polling.interval`

How often the plugin checks each list for new votes. Format: `30s`, `1m`, `2m30s` or a plain number of seconds.
Default **30s**, minimum **10s** (a lower value is raised and a warning is printed to the console).

Each check fetches **only the votes since the previous check** (the `createdAt[after]` filter),
not the whole history — see [how it works](architecture.md#polling-the-api).

### `servers`

The lists your server is on. The key names (`lista-minecraft` etc.) are arbitrary but must be unique
— they appear in messages as `%server_name%`.

| Key | Description |
|---|---|
| `address` | the list's website address, e.g. `https://lista-minecraft.pl` |
| `uuid` | your server's ID on the list — the last part of the server page URL, e.g. `…/serwery/minecraft/183413bc-4588-4265-8f06-346963e5d7df` |
| `reward-commands` | commands run from the console for each vote (without `/`) |

An invalid entry (bad address, missing `uuid`) is skipped with a warning in the console — the other lists keep working.

Placeholders in commands:

| Placeholder | Value |
|---|---|
| `%player_name%` | player's name |
| `%player_display_name%` | player's display name |
| `%player_uuid%` | player's UUID |
| `%server_name%` | list name from `settings.yml` |
| `%server_address%` | list address |

### Reward rules

- A player gets a reward **once a day per list** (the day is counted in UTC — the same way the lists count it).
- If the player is online, the reward is given as soon as the vote is detected.
- If they are offline, they get it the next time they join the server.
- An unclaimed reward expires after 7 days.
- A vote cast without a nickname gives no reward.

## messages.yml

Colours use the `&` format (`&a` green, `&c` red, `&e` yellow, `&l` bold…).
An empty message (`''`) is not sent. Missing keys are taken from the default file in the `.jar`.

## Commands and permissions

Command: `/votereward` (alias `/vr`).

| Command | Permission | Default | Description |
|---|---|---|---|
| `/vr` or `/vr reward` | `votereward.reward` | everyone | claims pending rewards |
| `/vr check` | `votereward.check` | everyone | today's vote status on each list |
| `/vr check <player>` | `votereward.check.others` | OP | another player's status |
| `/vr reload` | `votereward.reload` | OP | reloads both files and the interval |
| `/vr poll` | `votereward.poll` | OP | checks the lists immediately (e.g. for testing) |
