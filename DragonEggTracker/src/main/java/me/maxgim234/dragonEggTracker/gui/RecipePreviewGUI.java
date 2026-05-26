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
        // Use 36 slots fit full 3x3 crafting grid
        Inventory inv = Bukkit.createInventory(null, 36, TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        ConfigurationSection recipeSection =
                plugin.getConfig().getConfigurationSection("tracker-compass-recipe");

        if (recipeSection == null) return inv;

        java.util.List<String> shape = recipeSection.getStringList("shape");
        if (shape.size() != 3) return inv;

        ConfigurationSection ingredients = recipeSection.getConfigurationSection("ingredients");
        if (ingredients == null) return inv;

        // Define the 3x3 crafting grid
        int[][] craftingSlots = {
                {10, 11, 12},  // Top row
                {19, 20, 21},  // Middle row
                {28, 29, 30}   // Bottom row
        };

        // Place items
        for (int row = 0; row < 3; row++) {
            String shapeRow = shape.get(row);
            for (int col = 0; col < Math.min(shapeRow.length(), 3); col++) {
                char symbol = shapeRow.charAt(col);


                if (symbol == ' ') continue;

                String materialName = ingredients.getString(String.valueOf(symbol));
                if (materialName == null) continue;

                Material mat = Material.matchMaterial(materialName);
                if (mat != null) {
                    inv.setItem(craftingSlots[row][col], new ItemStack(mat));
                }
            }
        }

        inv.setItem(25, TrackerCompass.create());

        return inv;
    }
}
