package me.maxgim234.dragonEggTracker.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EggBuffListener implements Listener {

    private final JavaPlugin plugin;

    public EggBuffListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasEgg(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        return false;
    }

    private void applyBuffs(Player player) {
        if (!plugin.getConfig().getBoolean("egg-holder-buffs.enabled")) return;
        if (!hasEgg(player)) return;

        int extraHearts = plugin.getConfig().getInt(
                "egg-holder-buffs.extra-hearts", 0
        );

        // ❤️ Max health (works everywhere)
        player.setMaxHealth(20 + extraHearts * 2);

        ConfigurationSection effects =
                plugin.getConfig().getConfigurationSection(
                        "egg-holder-buffs.potion-effects"
                );

        if (effects == null) return;

        for (String key : effects.getKeys(false)) {
            PotionEffectType type = PotionEffectType.getByName(key);
            if (type == null) continue;

            int duration = effects.getInt(key + ".duration");
            int amplifier = effects.getInt(key + ".amplifier");

            player.addPotionEffect(
                    new PotionEffect(type, duration, amplifier, true, false)
            );
        }
    }

    private void clearBuffs(Player player) {
        player.setMaxHealth(20);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyBuffs(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearBuffs(event.getPlayer());
    }
}
