package me.maxgim234.dragonEggTracker.tracking;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EggStatsManager {

    private final JavaPlugin plugin;
    private final File statsFile;
    private final YamlConfiguration data;

    // uuid -> all-time total seconds held
    private final Map<UUID, Long> totalHeldSeconds = new HashMap<>();
    // uuid -> last known player name, so the leaderboard doesn't need OfflinePlayer lookups
    private final Map<UUID, String> playerNames = new HashMap<>();
    // players who have already received the one-time first-pickup reward
    private final Set<UUID> firstPickupRewarded = new HashSet<>();

    public EggStatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "egg-stats.yml");
        this.data = YamlConfiguration.loadConfiguration(statsFile);
        load();
    }

    public void addHeldSeconds(UUID uuid, String name, long seconds) {
        totalHeldSeconds.merge(uuid, seconds, Long::sum);
        playerNames.put(uuid, name);
    }

    public long getTotalHeldSeconds(UUID uuid) {
        return totalHeldSeconds.getOrDefault(uuid, 0L);
    }

    public boolean hasReceivedFirstPickupReward(UUID uuid) {
        return firstPickupRewarded.contains(uuid);
    }

    public void markFirstPickupRewarded(UUID uuid) {
        firstPickupRewarded.add(uuid);
    }

    public String getName(UUID uuid) {
        return playerNames.getOrDefault(uuid, "Unknown");
    }

    /**
     * Returns up to {@code count} players sorted by all-time total hold
     * time, descending.
     */
    public List<Map.Entry<UUID, Long>> getTop(int count) {
        List<Map.Entry<UUID, Long>> entries = new ArrayList<>(totalHeldSeconds.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        if (entries.size() > count) {
            return entries.subList(0, count);
        }
        return entries;
    }

    public static String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;

        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    public void load() {
        totalHeldSeconds.clear();
        playerNames.clear();
        firstPickupRewarded.clear();

        if (!statsFile.exists()) return;

        ConfigurationSection players = data.getConfigurationSection("players");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long seconds = players.getLong(key + ".seconds", 0L);
                    String name = players.getString(key + ".name", "Unknown");

                    totalHeldSeconds.put(uuid, seconds);
                    playerNames.put(uuid, name);
                } catch (IllegalArgumentException ignored) {
                    // malformed UUID key, skip
                }
            }
        }

        for (String key : data.getStringList("first-pickup-rewarded")) {
            try {
                firstPickupRewarded.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
                // malformed UUID entry, skip
            }
        }
    }

    public void save() {
        try {
            for (Map.Entry<UUID, Long> entry : totalHeldSeconds.entrySet()) {
                String key = entry.getKey().toString();
                data.set("players." + key + ".seconds", entry.getValue());
                data.set("players." + key + ".name", playerNames.getOrDefault(entry.getKey(), "Unknown"));
            }

            List<String> rewarded = new ArrayList<>();
            for (UUID uuid : firstPickupRewarded) {
                rewarded.add(uuid.toString());
            }
            data.set("first-pickup-rewarded", rewarded);

            data.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save egg stats!");
            e.printStackTrace();
        }
    }
}
