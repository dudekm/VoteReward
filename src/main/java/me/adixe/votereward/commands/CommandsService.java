package me.adixe.votereward.commands;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.executors.CommandExecutor;
import me.adixe.votereward.utils.MessagesUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandsService implements org.bukkit.command.CommandExecutor, TabCompleter {
    private final VoteReward plugin;
    private final Map<CommandExecutor, List<String>> commandExecutors;

    public CommandsService(VoteReward plugin) {
        this.plugin = plugin;
        this.commandExecutors = new LinkedHashMap<>();
    }

    public void register(CommandExecutor executor, String... triggers) {
        commandExecutors.put(executor, Arrays.asList(triggers));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        List<String> input = new ArrayList<>(Arrays.asList(args));

        if (input.isEmpty()) {
            return commandExecutors.keySet().iterator().next().execute(sender, input);
        }

        String trigger = input.remove(0);

        Optional<CommandExecutor> optionalCommand = commandExecutors.entrySet().stream()
                .filter(entry -> entry.getValue().contains(trigger))
                .map(Map.Entry::getKey)
                .findFirst();

        if (optionalCommand.isPresent()) {
            return optionalCommand.get().execute(sender, input);
        }

        Map<String, String> placeholders = new HashMap<>(
                plugin.getPlaceholdersManager().get(CommandSender.class).getUnique(sender));
        placeholders.put("trigger", trigger);

        MessagesUtility.sendMessage(sender,
                plugin.getConfiguration().get("messages"),
                "Commands.UnknownCommand",
                placeholders);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String label, String[] args) {
        List<String> entries = new ArrayList<>();

        List<String> input = new ArrayList<>(Arrays.asList(args));

        Stream<Map.Entry<CommandExecutor, List<String>>> allowedExecutors =
                commandExecutors.entrySet().stream()
                .filter(entry -> entry.getKey().hasPermission(sender));

        if (input.size() == 1) {
            allowedExecutors.forEach(entry -> entries.addAll(entry.getValue()));
        } else {
            String trigger = input.remove(0);

            allowedExecutors.filter(entry -> entry.getValue().contains(trigger))
                    .findFirst()
                    .ifPresent(entry -> entries.addAll(entry.getKey().tabComplete(sender, input)));
        }

        return entries.stream()
                .filter(entry -> entry.startsWith(input.get(input.size() - 1)))
                .collect(Collectors.toList());
    }
}
