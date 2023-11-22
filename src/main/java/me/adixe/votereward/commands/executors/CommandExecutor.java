package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import me.adixe.votereward.commands.args.CommandArg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandExecutor {
    private final String identifier;
    private final List<CommandArg> args;
    private final int requiredArgs;
    private final String permission;

    public CommandExecutor(String identifier, List<CommandArg> args,
                           int requiredArgs, String permission) {
        this.identifier = identifier;
        this.args = args;
        this.requiredArgs = requiredArgs;
        this.permission = permission;
    }

    public boolean execute(CommandSender sender, List<String> input) {
        try {
            if (!hasPermission(sender))
                throw new CommandException("NoPermission");

            Map<String, Object> argsValues = new HashMap<>();

            int argIndex = 0;

            for (CommandArg arg : args) {
                if (argIndex > input.size() - 1)
                    break; // Cancel when data is missing in input for args

                String argIdentifier = arg.getIdentifier();

                String simpleInput = input.get(argIndex);

                argsValues.put(argIdentifier, arg.buildValue(sender, simpleInput));

                argIndex++;
            }

            if (argsValues.size() < requiredArgs) {
                StringBuilder usage = new StringBuilder();

                int index = 0;

                for (CommandArg arg : args) {
                    String argIdentifier = arg.getIdentifier();

                    if (index <= requiredArgs - 1)
                        usage.append("<").append(argIdentifier).append(">");
                    else
                        usage.append("[").append(argIdentifier).append("]");

                    usage.append(" ");

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
        if (input.size() <= args.size())
            return args.get(input.size() - 1).tabComplete(sender);

        return Collections.emptyList();
    }

    protected void sendMessage(CommandSender recipient, String message,
                               Map<String, String> placeholders) {
        VoteReward voteReward = VoteReward.getInstance();

        Map<String, String> newPlaceholders = new HashMap<>(placeholders);
        newPlaceholders.putAll(voteReward.getPlaceholdersProvider().getCommandSenderDefault(recipient));

        voteReward.getMessagesUtility().sendMessage(recipient,
                "Commands." + identifier + "." + message, newPlaceholders);
    }

    protected void sendMessage(CommandSender recipient, String message) {
        sendMessage(recipient, message, Collections.emptyMap());
    }

    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission);
    }
}
