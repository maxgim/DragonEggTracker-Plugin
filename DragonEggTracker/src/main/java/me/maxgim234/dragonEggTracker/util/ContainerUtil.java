package me.maxgim234.dragonEggTracker.util;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerUtil {

    private static final Set<InventoryType> blockedContainers = new HashSet<>();
    private static boolean blockAll = false;

    public static void load(JavaPlugin plugin) {
        blockedContainers.clear();
        blockAll = plugin.getConfig().getBoolean("prevent-all-containers", false);

        List<String> list = plugin.getConfig().getStringList("prevent-containers");
        for (String name : list) {
            try {
                blockedContainers.add(InventoryType.valueOf(name));
            } catch (IllegalArgumentException e) {
                if (name.equals("HOPPER_MINECART")) {
                    plugin.getLogger().info("[DragonEggTracker] Note: HOPPER_MINECART is not a valid InventoryType. Hopper minecarts use the HOPPER type, which is already blocked by the code.");
                } else {
                    plugin.getLogger().warning("[DragonEggTracker] Invalid container type in config: " + name);
                }
            }
        }
    }

    public static boolean isAllBlocked() {
        return blockAll;
    }

    public static boolean isBlocked(InventoryType type) {
        return blockedContainers.contains(type);
    }
}