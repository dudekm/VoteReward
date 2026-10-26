package me.adixe.votereward.site;

import me.adixe.votereward.text.Placeholders;

/**
 * A console command run when a player is rewarded, e.g. {@code give %player_name% diamond}.
 */
public record RewardCommand(String template) {
    public RewardCommand {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("Reward command is empty.");
        }

        template = template.trim();

        // Commands are dispatched from the console, which takes them without the slash.
        if (template.startsWith("/")) {
            template = template.substring(1);
        }
    }

    public String render(Placeholders placeholders) {
        return placeholders.apply(template);
    }
}
