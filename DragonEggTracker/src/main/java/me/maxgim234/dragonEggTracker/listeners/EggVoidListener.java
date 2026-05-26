package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EggVoidListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    public EggVoidListener(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
    }

    // ── Event-based attempt (best-effort; engine may still remove the item) ──

    @EventHandler(priority = EventPriority.HIGH)
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Item droppedItem)) return;
        if (droppedItem.getItemStack().getType() != Material.DRAGON_EGG) return;

        event.setCancelled(true);
        rescueItem(droppedItem);
    }

    // ── Polling check — called every few ticks from a scheduled task ──────────
    // Reliable fallback: catches cases where the engine removes the item before
    // the damage event fires (which happens at very low Y values).

    public void runVoidCheck() {
        for (World world : Bukkit.getWorlds()) {
            int voidThreshold = world.getMinHeight() + 10; // 10-block buffer above void floor

            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Item item)) continue;
                if (item.getItemStack().getType() != Material.DRAGON_EGG) continue;
                if (item.getLocation().getBlockY() > voidThreshold) continue;

                rescueItem(item);
            }
        }
    }


    private void rescueItem(Item droppedItem) {
        World world = droppedItem.getWorld();
        Location itemLoc = droppedItem.getLocation();

        Location safeLoc = findSafeLocation(world, itemLoc);

        droppedItem.teleport(safeLoc);
        droppedItem.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

        eggManager.setEggLocation(safeLoc, EggState.DROPPED);
        eggManager.saveData();

        String msg = plugin.getConfig().getString(
                "egg-void-rescue.message",
                "&d&lThe Dragon Egg &r&dfell into the void and was rescued at &e%world% &7[&f%x%&7, &f%y%&7, &f%z%&7]&d!"
        );

        msg = msg
                .replace("%world%", world.getName())
                .replace("%x%", String.valueOf(safeLoc.getBlockX()))
                .replace("%y%", String.valueOf(safeLoc.getBlockY()))
                .replace("%z%", String.valueOf(safeLoc.getBlockZ()));

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private Location findSafeLocation(World world, Location nearLoc) {
        int x = nearLoc.getBlockX();
        int z = nearLoc.getBlockZ();

        int highY = world.getHighestBlockYAt(x, z);

        if (highY <= world.getMinHeight()) {
            Location spawnLoc = world.getSpawnLocation().clone();
            spawnLoc.add(0.5, 1, 0.5);
            return spawnLoc;
        }

        return new Location(world, x + 0.5, highY + 1, z + 0.5);
    }
}
