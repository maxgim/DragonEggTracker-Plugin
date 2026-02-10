package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EggPickupListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    public EggPickupListener(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (item.getType() != Material.DRAGON_EGG) return;

        eggManager.setOwner(
                player.getUniqueId(),
                player.getName()
        );

        eggManager.setEggLocation(
                player.getLocation(),
                EggState.PLAYER
        );

        // Play sound effect
        if (plugin.getConfig().getBoolean("egg-pickup-announcement.sound-enabled", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }

        // Announcement
        if (plugin.getConfig().getBoolean("egg-pickup-announcement.enabled")) {
            String msg = plugin.getConfig()
                    .getString("egg-pickup-announcement.message", "")
                    .replace("%player%", player.getName());

            Bukkit.broadcastMessage(
                    ChatColor.translateAlternateColorCodes('&', msg)
            );
        }
    }
}
