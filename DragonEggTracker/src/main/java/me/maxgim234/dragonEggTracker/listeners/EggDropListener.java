package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EggDropListener implements Listener {

    private final EggManager eggManager;

    public EggDropListener(EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.DRAGON_EGG) return;

        eggManager.setEggLocation(
                event.getItemDrop().getLocation(),
                EggState.DROPPED
        );
    }
}
