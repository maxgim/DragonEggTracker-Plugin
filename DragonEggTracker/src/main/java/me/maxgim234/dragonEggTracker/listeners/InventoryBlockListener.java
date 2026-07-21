package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryBlockListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    public InventoryBlockListener(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;

        ContainerUtil.load(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.DRAGON_EGG) return;

        InventoryType targetType = event.getInventory().getType();

        // Block all containers if configured
        if (ContainerUtil.isAllBlocked()
                && targetType != InventoryType.PLAYER
                && targetType != InventoryType.CRAFTING) {
            event.setCancelled(true);
            return;
        }

        // Always block hoppers
        if (targetType == InventoryType.HOPPER) {
            event.setCancelled(true);
            return;
        }
        // Ender chest check
        if (!plugin.getConfig().getBoolean("allow-enderchest-egg")
                && targetType == InventoryType.ENDER_CHEST) {
            event.setCancelled(true);
            return;
        }

        // Prevent configured containers
        if (ContainerUtil.isBlocked(targetType)) {
            event.setCancelled(true);
            return;
        }

        // Update location if moved into a container
        if (event.getInventory().getLocation() != null) {
            eggManager.setEggLocation(
                    event.getInventory().getLocation(),
                    EggState.CONTAINER
            );
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryType type = event.getInventory().getType();

        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING) return;
        if (!ContainerUtil.isAllBlocked() && !ContainerUtil.isBlocked(type)
                && (plugin.getConfig().getBoolean("allow-enderchest-egg") || type != InventoryType.ENDER_CHEST)
                && type != InventoryType.HOPPER) return;

        if (event.getInventory().getLocation() == null) return;

        for (int i = 0; i < event.getInventory().getSize(); i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                event.getInventory().setItem(i, null);
                event.getInventory().getLocation().getWorld().dropItemNaturally(
                        event.getInventory().getLocation(), item
                );
            }
        }
    }
}
