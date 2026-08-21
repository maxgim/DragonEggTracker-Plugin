package me.maxgim234.dragonEggTracker.tracking;

import me.maxgim234.dragonEggTracker.economy.EconomyHook;
import me.maxgim234.dragonEggTracker.util.EggItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Runs once per second. Tracks all-time hold-time stats (for the leaderboard
 * and placeholders) and continuous-hold time (for the hourly economy reward,
 * which resets if the egg changes hands before an hour is reached).
 */
public class EggHoldTrackerTask implements Runnable {

    private static final long HOUR_SECONDS = 3600L;

    private final JavaPlugin plugin;
    private final EggManager eggManager;
    private final EggStatsManager statsManager;

    private UUID lastHolder;
    private long continuousHeldSeconds = 0;

    public EggHoldTrackerTask(JavaPlugin plugin, EggManager eggManager, EggStatsManager statsManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
        this.statsManager = statsManager;
    }

    @Override
    public void run() {
        OfflinePlayer owner = eggManager.getOwner();
        if (owner == null || !owner.isOnline()) {
            resetContinuous();
            return;
        }

        Player player = owner.getPlayer();
        if (player == null || !EggItemUtil.hasDragonEggAnywhere(player)) {
            resetContinuous();
            return;
        }

        UUID uuid = player.getUniqueId();

        // The holder changed since the last tick — restart the continuous counter.
        if (!uuid.equals(lastHolder)) {
            lastHolder = uuid;
            continuousHeldSeconds = 0;
        }

        statsManager.addHeldSeconds(uuid, player.getName(), 1);
        continuousHeldSeconds++;

        if (continuousHeldSeconds >= HOUR_SECONDS) {
            continuousHeldSeconds = 0;
            payHourlyReward(player);
        }
    }

    private void resetContinuous() {
        lastHolder = null;
        continuousHeldSeconds = 0;
    }

    private void payHourlyReward(Player player) {
        if (!plugin.getConfig().getBoolean("economy-rewards.hourly-holding.enabled", false)) return;
        if (!EconomyHook.isEnabled()) return;

        double amount = plugin.getConfig().getDouble("economy-rewards.hourly-holding.amount", 0);
        EconomyHook.deposit(player, amount);

        String msg = plugin.getConfig().getString(
                "economy-rewards.hourly-holding.message",
                "&d&lYou earned &e$%amount% &d&lfor holding the Dragon Egg for an hour!"
        ).replace("%amount%", String.valueOf(amount));

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}
