package me.maxgim234.dragonEggTracker.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EggItemUtil {

    private EggItemUtil() {
    }

    /**
     * True if a dragon egg is anywhere in the player's inventory.
     */
    public static boolean hasDragonEggAnywhere(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        return false;
    }

    /**
     * True only if the player's main hand is directly holding a dragon egg
     * (used for the sell-command block — deliberately not bundle-aware,
     * since selling checks the item actually in hand).
     */
    public static boolean hasDragonEggInMainHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        return mainHand.getType() == Material.DRAGON_EGG;
    }
}
