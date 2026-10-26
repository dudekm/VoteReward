package me.adixe.votereward.reward;

/**
 * Outcome of one check of every list.
 *
 * @param skipped true when the check did not run because the previous one was still going
 */
public record PollReport(int sitesChecked, int sitesFailed, int votesRead, int rewardsEarned, boolean skipped) {
    public static final PollReport SKIPPED = new PollReport(0, 0, 0, 0, true);
}
