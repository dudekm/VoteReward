package me.adixe.votereward.votemanager;

import me.adixe.commonutilslib.placeholder.PlaceholderManager;
import me.adixe.commonutilslib.util.MessageUtil;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.votemanager.verifier.VoteVerifier;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

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

    private void update() {
        YamlFile votes = VoteReward.getInstance().getConfiguration().get("votes");

        LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

        if (!votes.contains("date") || !LocalDate.parse(votes.getString("date"),
                DateTimeFormatter.ISO_LOCAL_DATE).isEqual(timeNow)) {
            votes.set("date", timeNow.format(DateTimeFormatter.ISO_LOCAL_DATE));

            votes.set("votes", null);
        }
    }

    public void verify(Server server, OfflinePlayer player, VerificationListener listener) {
        for (VoteVerifier verifier : verifiers) {
            if (!verifier.canVerify(server)) {
                continue;
            }

            try {
                if (verifier.verify(server, player)) {
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

    public void verifyAsync(Server server, OfflinePlayer player, VerificationListener listener) {
        Bukkit.getScheduler().runTaskAsynchronously(VoteReward.getInstance(),
                () -> verify(server, player, listener));
    }

    public void registerVerifier(VoteVerifier verifier) {
        verifiers.add(verifier);
    }

    public boolean rewardPlayer(Player player, Server server) {
        VoteReward plugin = VoteReward.getInstance();

        YamlFile votes = plugin.getConfiguration().get("votes");

        String playerPath = "votes." + player.getUniqueId();

        List<String> alreadyVotedServers = new ArrayList<>(votes.getStringList(playerPath));

        if (votes.contains(playerPath) && alreadyVotedServers.contains(server.uuid())) {
            return false;
        }

        alreadyVotedServers.add(server.uuid());

        votes.set(playerPath, alreadyVotedServers);

        Bukkit.getScheduler().runTask(plugin, () -> {
            PlaceholderManager placeholderManager = plugin.getPlaceholderManager();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.putAll(placeholderManager.get(Player.class).getUnique(player));
            placeholders.putAll(placeholderManager.get(Server.class).getUnique(server));

            for (String command : server.rewardCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        MessageUtil.translate(command, placeholders));
            }
        });

        return true;
    }
}
