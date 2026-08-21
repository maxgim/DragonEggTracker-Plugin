package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EggVoidListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    public EggVoidListener(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {

        Entity entity = event.getEntity();

        // Dropped Dragon Egg item
        if (entity instanceof Item item) {

            if (item.getItemStack().getType() != Material.DRAGON_EGG) {
                return;
            }

            if (!isTrackedEgg(item.getLocation())) {
                return;
            }

            monitorEgg(item);
            return;
        }

        // Falling Dragon Egg block
        if (entity instanceof FallingBlock fallingBlock) {

            if (fallingBlock.getBlockData().getMaterial()
                    != Material.DRAGON_EGG) {
                return;
            }

            if (!isTrackedEgg(fallingBlock.getLocation())) {
                return;
            }

            monitorEgg(fallingBlock);
        }
    }

    /**
     * Checks whether the entity is the Dragon Egg currently
     * tracked by EggManager.
     */
    private boolean isTrackedEgg(Location location) {

        Location trackedLocation = eggManager.getEggLocation();

        if (trackedLocation == null) {
            return false;
        }

        if (trackedLocation.getWorld() == null
                || location.getWorld() == null) {
            return false;
        }

        if (!trackedLocation.getWorld().equals(location.getWorld())) {
            return false;
        }

        // Allow a small difference between the stored location
        // and the entity's actual spawn location.
        return trackedLocation.distanceSquared(location) <= 4.0;
    }

    /**
     * Monitors ONLY the tracked Dragon Egg.
     *
     * This does not scan all entities or worlds.
     */
    private void monitorEgg(Entity eggEntity) {

        new BukkitRunnable() {

            @Override
            public void run() {

                // Entity no longer exists.
                if (!eggEntity.isValid()) {
                    cancel();
                    return;
                }

                World world = eggEntity.getWorld();
                Location location = eggEntity.getLocation();

                /*
                 * Rescue the egg before Minecraft removes it.
                 */
                if (location.getY() <= world.getMinHeight() - 5) {

                    rescueEgg(eggEntity);

                    cancel();
                    return;
                }

                /*
                 * For dropped Item entities, stop monitoring once
                 * the egg has stopped falling.
                 */
                if (eggEntity instanceof Item item) {

                    if (item.getVelocity().getY() >= 0
                            && location.getY() > world.getMinHeight()) {
                        cancel();
                    }
                }
            }

        }.runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * Rescues the Dragon Egg and recreates it as an actual block
     * at a safe location.
     */
    private void rescueEgg(Entity entity) {

        World world = entity.getWorld();

        Location lastLocation = entity.getLocation().clone();

        Location safeLocation = findSafeLocation(
                world,
                lastLocation
        );

        // Remove the original falling/dropped entity.
        entity.remove();

        /*
         * Wait one tick before placing the replacement block.
         * This prevents the old entity from interfering with the
         * newly placed Dragon Egg.
         */
        Bukkit.getScheduler().runTask(plugin, () -> {

            // Make sure the location is still safe.
            if (!safeLocation.getChunk().isLoaded()) {
                safeLocation.getChunk().load();
            }

            /*
             * Place the Dragon Egg as a real Minecraft block,
             * NOT as a dropped Item.
             */
            safeLocation.getBlock().setType(Material.DRAGON_EGG);

            Location actualLocation = safeLocation.getBlock().getLocation();

            /*
             * Keep the tracked location updated.
             *
             * DROPPED is retained here because it is already used
             * by your existing EggManager/EggState setup.
             */
            eggManager.setEggLocation(
                    actualLocation,
                    EggState.DROPPED
            );

            eggManager.saveData();

            sendRescueMessage(
                    world,
                    actualLocation
            );
        });
    }

    /**
     * Sends the configured rescue message.
     */
    private void sendRescueMessage(
            World world,
            Location safeLocation
    ) {

        String msg = plugin.getConfig().getString(
                "egg-void-rescue.message",
                "&d&lThe Dragon Egg &r&dfell into the void and was rescued at "
                        + "&e%world% &7[&f%x%&7, &f%y%&7, &f%z%&7]&d!"
        );

        msg = msg
                .replace("%world%", world.getName())
                .replace("%x%", String.valueOf(safeLocation.getBlockX()))
                .replace("%y%", String.valueOf(safeLocation.getBlockY()))
                .replace("%z%", String.valueOf(safeLocation.getBlockZ()));

        Bukkit.broadcastMessage(
                ChatColor.translateAlternateColorCodes('&', msg)
        );
    }

    /**
     * Finds a safe location above the highest block
     * at the egg's X/Z coordinates.
     */
    private Location findSafeLocation(
            World world,
            Location nearLocation
    ) {

        int x = nearLocation.getBlockX();
        int z = nearLocation.getBlockZ();

        int highestY = world.getHighestBlockYAt(x, z);

        /*
         * If there is no valid terrain at this location,
         * fall back to world spawn.
         */
        if (highestY <= world.getMinHeight()) {

            Location spawn = world.getSpawnLocation();

            return new Location(
                    world,
                    spawn.getBlockX(),
                    spawn.getBlockY() + 1,
                    spawn.getBlockZ()
            );
        }

        return new Location(
                world,
                x,
                highestY + 1,
                z
        );
    }
}