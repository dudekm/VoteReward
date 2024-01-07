package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import me.adixe.votereward.commands.args.CommandArg;
import me.adixe.votereward.utils.MessagesUtility;
import org.bukkit.command.CommandSender;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandExecutor {
    protected final VoteReward plugin;
    private final String identifier;
    private final List<CommandArg> args;
    private final int requiredArgs;
    private final String permission;

    public CommandExecutor(VoteReward plugin, String identifier, List<CommandArg> args,
                           int requiredArgs, String permission) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.args = args;
        this.requiredArgs = requiredArgs;
        this.permission = permission;
    }

    public boolean execute(CommandSender sender, List<String> input) {
        try {
            if (!hasPermission(sender)) {
                throw new CommandException("NoPermission");
            }

            Map<String, Object> argsValues = new HashMap<>();

            int argIndex = 0;

            for (CommandArg arg : args) {
                if (argIndex > input.size() - 1) {
                    break; // Cancel when data is missing in input for args
                }

                String argIdentifier = arg.getIdentifier();

                StringBuilder argInput = new StringBuilder();

                String simpleInput = input.get(argIndex);

                if (simpleInput.startsWith("\"")) {
                    while (true) {
                        if (argIndex > input.size() - 1) {
                            throw new CommandException("NoArgEnd",
                                    Collections.singletonMap("arg", argIdentifier));
                        }

                        String entry = input.get(argIndex);

                        argInput.append(entry.replace("\"", ""));

                        if (entry.endsWith("\"")) {
                            break;
                        }

                        argInput.append(" ");

                        argIndex++;
                    }
                } else {
                    argInput.append(simpleInput.replace("\"", ""));
                }

                argsValues.put(argIdentifier, arg.buildValue(sender, argInput.toString()));

                argIndex++;
            }

            if (argsValues.size() < requiredArgs) {
                StringBuilder usage = new StringBuilder();

                int index = 0;

                for (CommandArg arg : args) {
                    String argIdentifier = arg.getIdentifier();

                    if (index <= requiredArgs - 1) {
                        usage.append("<").append(argIdentifier).append(">");
                    } else {
                        usage.append("[").append(argIdentifier).append("]");
                    }

                    if (index + 1 < args.size()) {
                        usage.append(" ");
                    }

                    index++;
                }

                throw new CommandException("InvalidUsage",
                        Collections.singletonMap("usage", usage.toString()));
            }

            perform(sender, argsValues);

            return true;
        } catch (CommandException exception) {
            sendMessage(sender, exception.getMessage(), exception.getPlaceholders());

            return false;
        }
    }

    protected abstract void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException;

    public List<String> tabComplete(CommandSender sender, List<String> input) {
        if (input.size() <= args.size()) {
            return args.get(input.size() - 1).tabComplete(sender); // FIXME: Include quote tab-completing
        }

        return Collections.emptyList();
    }

    protected void sendMessage(CommandSender recipient, String message,
                               Map<String, String> placeholders) {
        Map<String, String> newPlaceholders = new HashMap<>(placeholders);
        newPlaceholders.putAll(plugin.getPlaceholdersManager()
                .get(CommandSender.class).getUnique(recipient));

        YamlFile messages = plugin.getConfiguration().get("messages");

        try {
            MessagesUtility.sendMessage(recipient, messages,
                    "Commands." + identifier + "." + message,
                    newPlaceholders);
        } catch (IllegalArgumentException exception) {
            MessagesUtility.sendMessage(recipient, messages,
                    "Commands." + message,
                    newPlaceholders);
        }
    }

    protected void sendMessage(CommandSender recipient, String message) {
        sendMessage(recipient, message, Collections.emptyMap());
    }

    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission);
    }
}
