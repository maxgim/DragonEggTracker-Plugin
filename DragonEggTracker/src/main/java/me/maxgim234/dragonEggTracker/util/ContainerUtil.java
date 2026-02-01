package me.maxgim234.dragonEggTracker.util;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerUtil {

    private static final Set<InventoryType> blockedContainers = new HashSet<>();

    public static void load(JavaPlugin plugin) {
        blockedContainers.clear();

        List<String> list = plugin.getConfig().getStringList("prevent-containers");
        for (String name : list) {
            try {
                blockedContainers.add(InventoryType.valueOf(name));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[DragonEggTracker] Invalid container type: " + name);
            }
        }
    }

    public static boolean isBlocked(InventoryType type) {
        return blockedContainers.contains(type);
    }
}
