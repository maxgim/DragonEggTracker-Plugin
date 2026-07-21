package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EggBlockListener implements Listener {

    private final EggManager eggManager;
    private final JavaPlugin plugin;

    public EggBlockListener(EggManager eggManager, JavaPlugin plugin) {
        this.eggManager = eggManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onEggPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.DRAGON_EGG) return;

        if (plugin.getConfig().getBoolean("prevent-egg-placement", false)) {
            event.setCancelled(true);
            return;
        }

        eggManager.setEggLocation(
                event.getBlockPlaced().getLocation(),
                EggState.BLOCK
        );
    }

    @EventHandler
    public void onEggBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) return;

        eggManager.setEggLocation(
                event.getBlock().getLocation(),
                EggState.DROPPED
        );
    }
}