package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EggPickupListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    // ✅ Constructor
    public EggPickupListener(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();

        if (item.getType() != Material.DRAGON_EGG) return;

        Player player = event.getPlayer();

        // Set egg owner
        eggManager.setOwner(
                player.getUniqueId(),
                player.getName()
        );


        // 📢 Announcement
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
