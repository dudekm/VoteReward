package me.adixe.votereward.commands;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.executors.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandsService implements org.bukkit.command.CommandExecutor, TabCompleter {
    private final Map<CommandExecutor, List<String>> commandExecutors = new LinkedHashMap<>();

    public void register(CommandExecutor command, String... triggers) {
        commandExecutors.put(command, Arrays.asList(triggers));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        List<String> input = new ArrayList<>(Arrays.asList(args));

        if (input.isEmpty())
            return commandExecutors.keySet().iterator().next().execute(sender, input);

        String trigger = input.remove(0);

        for (Map.Entry<CommandExecutor, List<String>> entry : commandExecutors.entrySet()) {
            if (entry.getValue().contains(trigger))
                return entry.getKey().execute(sender, input);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sender", sender.getName());
        placeholders.put("trigger", trigger);

        VoteReward.getInstance().getMessagesUtility()
                .sendMessage(sender, "Commands.UnknownCommand", placeholders);

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        List<String> entries = new ArrayList<>();

        List<String> input = new ArrayList<>(Arrays.asList(args));

        if (input.size() == 1)
            commandExecutors.entrySet().stream()
                    .filter(entry -> entry.getKey().hasPermission(sender))
                    .forEach(entry -> entries.addAll(entry.getValue()));
        else {
            String trigger = input.remove(0);

            commandExecutors.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(trigger))
                    .findFirst()
                    .ifPresent(entry -> entries.addAll(entry.getKey().tabComplete(sender, input)));
        }

        return entries.stream()
                .filter(entry -> entry.startsWith(input.get(input.size() - 1)))
                .collect(Collectors.toList());
    }
}
