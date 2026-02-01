package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class EggBlockListener implements Listener {

    private final EggManager eggManager;

    public EggBlockListener(EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @EventHandler
    public void onEggPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.DRAGON_EGG) return;

        eggManager.setEggLocation(
                event.getBlockPlaced().getLocation(),
                EggState.BLOCK
        );
    }

    @EventHandler
    public void onEggBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) return;

        // Location will be updated again on pickup
        eggManager.setEggLocation(
                event.getBlock().getLocation(),
                EggState.DROPPED
        );
    }
}
