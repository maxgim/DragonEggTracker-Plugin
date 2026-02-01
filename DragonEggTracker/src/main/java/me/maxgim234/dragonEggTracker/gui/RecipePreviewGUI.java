package me.maxgim234.dragonEggTracker.gui;

import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipePreviewGUI {

    public static final String TITLE = "§5Dragon Egg Tracker Recipe";

    public static Inventory create(JavaPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Fill background
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Recipe slots (3x3)
        int[] recipeSlots = {
                10, 11, 12,
                19, 20, 21
        };

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection(
                        "tracker-compass-recipe.ingredients"
                );

        if (section != null) {
            int i = 0;
            for (String key : section.getKeys(false)) {
                if (i >= recipeSlots.length) break;

                Material mat = Material.matchMaterial(
                        section.getString(key)
                );
                if (mat != null) {
                    inv.setItem(recipeSlots[i], new ItemStack(mat));
                    i++;
                }
            }
        }

        // Result slot
        inv.setItem(16, TrackerCompass.create());

        return inv;
    }
}
