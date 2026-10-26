package me.adixe.votereward.site;

import me.adixe.votereward.text.Placeholders;
import me.adixe.votereward.vote.ServerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardCommandTest {
    @Test
    void rendersPlaceholders() {
        RewardCommand command = new RewardCommand("give %player_name% diamond %player_name%");

        assertEquals("give Steve diamond Steve", command.render(Placeholders.of("player_name", "Steve")));
    }

    @Test
    void dropsLeadingSlashBecauseTheConsoleDoesNotUseIt() {
        assertEquals("say hi", new RewardCommand(" /say hi ").template());
    }

    @Test
    void rejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new RewardCommand(" "));
    }

    @Test
    void sitesMustHaveUniqueNames() {
        VoteSite site = new VoteSite("a", ListAddress.of("https://a.pl"),
                ServerId.of("183413bc-4588-4265-8f06-346963e5d7df"), List.of());

        assertThrows(IllegalArgumentException.class, () -> new VoteSites(List.of(site, site)));
    }
}
