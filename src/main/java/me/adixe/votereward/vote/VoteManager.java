package me.adixe.votereward.vote;

import me.adixe.commonutilslib.util.MessageUtil;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.vote.verifier.VoteVerifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteManager {
    private final List<VoteVerifier> verifiers;

    public VoteManager() {
        this.verifiers = new ArrayList<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(
                VoteReward.getInstance(), this::update, 0L, 20L * 60 * 10);
    }

    public void register(VoteVerifier verifier) {
        verifiers.add(verifier);
    }

    private void update() {
        var votes = VoteReward.getInstance().getConfiguration().get("votes");

        LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

        if (!votes.contains("date") || !LocalDate.parse(votes.getString("date"),
                DateTimeFormatter.ISO_LOCAL_DATE).isEqual(timeNow)) {
            votes.set("date", timeNow.format(DateTimeFormatter.ISO_LOCAL_DATE));

            votes.set("votes", null);
        }
    }

    public void verify(Player player, ServerHolder server, VerificationListener listener) {
        if (isRewardGranted(player, server)) {
            listener.rewardGranted();

            return;
        }

        for (VoteVerifier verifier : verifiers) {
            if (!verifier.canVerify(server)) {
                continue;
            }

            try {
                if (verifier.verify(player, server)) {
                    listener.success();
                } else {
                    listener.notFound();
                }
            } catch (Exception exception) {
                listener.exceptionCaught(exception);
            }

            return;
        }

        throw new IllegalArgumentException("Invalid server address: " + server.address());
    }

    public void verifyAsync(Player player, ServerHolder server, VerificationListener listener) {
        Bukkit.getScheduler().runTaskAsynchronously(VoteReward.getInstance(),
                () -> verify(player, server, listener));
    }

    public void rewardPlayer(Player player, ServerHolder server) {
        VoteReward plugin = VoteReward.getInstance();

        if (!isRewardGranted(player, server)) {
            var votes = plugin.getConfiguration().get("votes");

            var votesSettings = votes.contains("votes") ?
                    votes.getConfigurationSection("votes") :
                    votes.createSection("votes");

            List<String> playerServers = getPlayerServers(player);

            playerServers.add(server.name());

            votesSettings.set(player.getUniqueId().toString(), playerServers);
        }

        Bukkit.getScheduler().runTask(plugin, () -> executeRewardCommands(player, server));
    }

    public boolean isRewardGranted(Player player, ServerHolder server) {
        return getPlayerServers(player).contains(server.name());
    }

    private List<String> getPlayerServers(Player player) {
        List<String> playerServers = new ArrayList<>();

        var votesSettings = VoteReward.getInstance().getConfiguration()
                .get("votes").getConfigurationSection("votes");

        String playerUuid = player.getUniqueId().toString();

        if (votesSettings != null && votesSettings.contains(playerUuid)) {
            playerServers.addAll(votesSettings.getStringList(playerUuid));
        }

        return playerServers;
    }

    private void executeRewardCommands(Player player, ServerHolder server) {
        var placeholderManager = VoteReward.getInstance().getPlaceholderManager();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.putAll(placeholderManager.get(Player.class).getUnique(player));
        placeholders.putAll(placeholderManager.get(ServerHolder.class).getUnique(server));

        for (String command : server.rewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtil.translate(command, placeholders));
        }
    }
}
