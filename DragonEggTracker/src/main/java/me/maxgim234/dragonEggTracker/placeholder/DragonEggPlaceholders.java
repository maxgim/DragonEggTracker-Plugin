package me.maxgim234.dragonEggTracker.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggStatsManager;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DragonEggPlaceholders extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final EggManager eggManager;
    private final EggStatsManager statsManager;

    public DragonEggPlaceholders(JavaPlugin plugin, EggManager eggManager, EggStatsManager statsManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
        this.statsManager = statsManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dragoneggtracker";
    }

    @Override
    public @NotNull String getAuthor() {
        return "maxgim234";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String key = params.toLowerCase();

        switch (key) {
            case "holder":
                return eggManager.getOwnerName() == null ? "None" : eggManager.getOwnerName();

            case "state":
                return eggManager.getEggState() == null ? "UNKNOWN" : eggManager.getEggState().name();

            case "world": {
                Location loc = eggManager.getEggLocation();
                return (loc == null || loc.getWorld() == null) ? "None" : loc.getWorld().getName();
            }

            case "x": {
                Location loc = eggManager.getEggLocation();
                return loc == null ? "0" : String.valueOf(loc.getBlockX());
            }

            case "y": {
                Location loc = eggManager.getEggLocation();
                return loc == null ? "0" : String.valueOf(loc.getBlockY());
            }

            case "z": {
                Location loc = eggManager.getEggLocation();
                return loc == null ? "0" : String.valueOf(loc.getBlockZ());
            }

            case "online": {
                OfflinePlayer owner = eggManager.getOwner();
                return (owner != null && owner.isOnline()) ? "Yes" : "No";
            }

            default:
                break;
        }

        if (key.startsWith("top_")) {
            return resolveTop(key);
        }

        return null;
    }

    /**
     * Handles %dragoneggtracker_top_1%, _top_2%, _top_3% (player name) and
     * %dragoneggtracker_top_1_time%, etc. (formatted total hold duration).
     */
    private String resolveTop(String key) {
        String[] parts = key.split("_"); // ["top", "<rank>", "time"?]
        if (parts.length < 2) return null;

        int rank;
        try {
            rank = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (rank < 1) return null;

        boolean wantsTime = parts.length >= 3 && parts[2].equals("time");

        List<Map.Entry<UUID, Long>> top = statsManager.getTop(rank);
        if (top.size() < rank) {
            return wantsTime ? "0s" : "None";
        }

        Map.Entry<UUID, Long> entry = top.get(rank - 1);

        return wantsTime
                ? EggStatsManager.formatDuration(entry.getValue())
                : statsManager.getName(entry.getKey());
    }
}
