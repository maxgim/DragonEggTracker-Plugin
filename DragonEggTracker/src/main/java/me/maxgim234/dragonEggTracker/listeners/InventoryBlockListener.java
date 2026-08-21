package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Check both the slot being clicked AND whatever is on the cursor.
        // Placing an item from the cursor into an empty slot never shows up
        // in getCurrentItem(), and a dragon egg can also be hidden inside a
        // bundle, so both items need to be unwrapped/checked.
        boolean hasEgg = ContainerUtil.containsDragonEgg(current)
                || ContainerUtil.containsDragonEgg(cursor);

        if (!hasEgg) return;

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
    public void onInventoryDrag(InventoryDragEvent event) {
        // Dragging (splitting a stack across slots, or a single-item drag)
        // never fires InventoryClickEvent, so it needs its own check.
        ItemStack dragged = event.getOldCursor();
        if (!ContainerUtil.containsDragonEgg(dragged)) return;

        InventoryType targetType = event.getInventory().getType();

        if (targetType == InventoryType.PLAYER || targetType == InventoryType.CRAFTING) return;

        boolean blocked = (ContainerUtil.isAllBlocked() && targetType != InventoryType.PLAYER)
                || targetType == InventoryType.HOPPER
                || (!plugin.getConfig().getBoolean("allow-enderchest-egg") && targetType == InventoryType.ENDER_CHEST)
                || ContainerUtil.isBlocked(targetType);

        if (blocked) {
            event.setCancelled(true);
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
            if (ContainerUtil.containsDragonEgg(item)) {
                // Drop the whole stack (egg or egg-containing bundle) rather
                // than trying to unpack just the egg from inside the bundle.
                event.getInventory().setItem(i, null);
                event.getInventory().getLocation().getWorld().dropItemNaturally(
                        event.getInventory().getLocation(), item
                );
            }
        }
    }
}
