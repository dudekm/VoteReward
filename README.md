The Polish version can be found [here](README-PL.md).

# VoteReward

VoteReward is a Spigot plugin that allows you to award players rewards for voting on a server. It was created for
versions above 1.18. Project statistics available on the [bStats](https://bstats.org/plugin/bukkit/VoteReward/20120)
website.

# Installation

Download the latest version of the plugin [here](https://github.com/dudekm/VoteReward/releases/latest).

After downloading the `.jar` file, place it in the "plugins" folder in the main server directory and then restart the
server. After restarting, a "VoteReward" folder should be created containing the configuration file.

Now in `settings.yml` you need to configure the servers that players can vote for. To do this, go to the `Servers`
section. Its default form looks like this:

```yaml
servers:
  server1:
    address: https://lista-serwerow.emecz.pl
    uuid: b23d452d-fbf1-47cf-a1d9-801a3e0bb516
    reward-commands:
      - xp add %player_name% 100
      - give %player_name% diamond
  server2:
    address: https://lista-minecraft.pl
    uuid: a5a29c24-6514-472d-add0-0b2be060e277
    reward-commands:
      - xp add %player_name% 100
      - give %player_name% emerald
```

The values "server1" or "server2" can be anything - they are just names.

`address` refers to the address of the page where the server was added. Supported addresses are:

- https://lista-serwerow.emecz.pl,
- https://lista-minecraft.pl,
- https://minecraft-list.info.

`uuid` means a unique server ID assigned exclusively to it. To check the ID, go to the server's website and copy the
last element of the website address. For example, if it looks like
this: https://lista-minecraft.pl/serwery/minecraft/b23d452d-fbf1-47cf-a1d9-801a3e0bb516 - the ID will
be `b23d452d-fbf1-47cf-a1d9-801a3e0bb516`.

`reward-commands` is a list of commands that will be sent to the server console after the player's vote has been
successfully verified. The following placeholders are available:

- `%player_name%` - name of the player,
- `%player_display_name%` - display name of the player,
- `%player_uuid%` - unique player ID,
- `%server_name%` - server name,
- `%server_address%` - server page address,
- `%server_uuid%` - unique server ID.

If the plugin has been configured correctly, after voting, the player can use the `/votereward` command to receive the
vote reward.