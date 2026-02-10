package me.maxgim234.dragonEggTracker.tracking;

import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassTrackerTask implements Runnable {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    public CompassTrackerTask(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
    }

    @Override
    public void run() {
        Location eggLoc = eggManager.getEggLocation();
        if (eggLoc == null) return;

        // Offline-owner
        if (plugin.getConfig().getBoolean("disable-tracking-if-owner-offline")) {
            if (eggManager.getOwner() != null && !eggManager.getOwner().isOnline()) {
                return;
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (!TrackerCompass.isTracker(item)) continue;

                TrackerCompass.setTarget(item, eggLoc);
            }
        }
    }
}
