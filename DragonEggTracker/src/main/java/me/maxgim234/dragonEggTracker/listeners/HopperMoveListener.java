package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HopperMoveListener implements Listener {

    public HopperMoveListener(JavaPlugin plugin) {
        ContainerUtil.load(plugin);
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (event.getItem().getType() != Material.DRAGON_EGG) return;

        if (ContainerUtil.isBlocked(event.getSource().getType())
                || ContainerUtil.isBlocked(event.getDestination().getType())) {
            event.setCancelled(true);
        }
    }
}
