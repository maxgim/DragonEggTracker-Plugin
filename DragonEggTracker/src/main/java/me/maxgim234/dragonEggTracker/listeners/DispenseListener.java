package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.DragonEggTracker;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

public class DispenseListener implements Listener {

    public DispenseListener(DragonEggTracker dragonEggTracker) {
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        if (event.getItem().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }
}
