package me.maxgim234.dragonEggTracker.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

public class HopperPickupListener implements Listener {

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() != Material.DRAGON_EGG) return;

        if (event.getInventory().getType() == InventoryType.HOPPER) {
            event.setCancelled(true);
        }
    }
}
