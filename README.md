The Polish version can be found [here](README-PL.md).

# VoteReward

VoteReward is a Spigot plugin that allows you to award players rewards for voting on a server. It was created for
versions above 1.18. Project statistics available on the [bStats](https://bstats.org/plugin/bukkit/VoteReward/20120)
website.

# Installation

Download the latest version of the plugin [here](https://github.com/dudekm/VoteReward/releases/latest).

After downloading the `.jar` file, place it in the "plugins" folder in the main server directory and then restart the
server. After restarting, a "VoteReward" folder should be created containing the configuration file.

Now in `settings.yml` you need to configure the servers that players can vote for. To do this, go to the `servers`
section. Its default form looks like this:

```yaml
servers:
  lista-minecraft:
    address: https://lista-minecraft.pl
    uuid: 183413bc-4588-4265-8f06-346963e5d7df
    reward-commands:
      - xp add %player_name% 100
      - give %player_name% diamond
  lista-serwerow-emecz:
    address: https://lista-serwerow.emecz.pl
    uuid: 264acafc-136c-4def-a363-088587c7a405
    reward-commands: []
  minecraft-list:
    address: https://minecraft-list.info
    uuid: d6dfe480-da3a-495a-a92d-5a198a7f2f83
    reward-commands: []
```

The values "lista-minecraft" or "lista-serwerow-emecz" can be anything - they are just names. Names should be unique for
each server. As many servers can be added as needed.

`address` refers to the address of the page where the server was added. Supported addresses are:

- https://lista-minecraft.pl,
- https://lista-serwerow.emecz.pl,
- https://minecraft-list.info.

`uuid` means a unique server ID assigned exclusively to it. To check the ID, go to the server's website and copy the
last element of the website address. For example, if it looks like
this: https://lista-minecraft.pl/serwery/minecraft/183413bc-4588-4265-8f06-346963e5d7df - the ID will
be `183413bc-4588-4265-8f06-346963e5d7df`.

`reward-commands` is a list of commands that will be sent to the server console after the player's vote has been
successfully verified. The following placeholders are available:

- `%player_name%` - name of the player,
- `%player_display_name%` - display name of the player,
- `%player_uuid%` - unique player ID,
- `%server_name%` - server name,
- `%server_address%` - server page address.

If the plugin has been configured correctly, after voting, the player can use the `/votereward` command to receive the
vote reward.