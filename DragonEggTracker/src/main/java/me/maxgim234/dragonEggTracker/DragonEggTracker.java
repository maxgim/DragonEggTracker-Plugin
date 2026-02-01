package me.maxgim234.dragonEggTracker;

import me.maxgim234.dragonEggTracker.commands.EggLocateCommand;
import me.maxgim234.dragonEggTracker.commands.EggOwnerCommand;
import me.maxgim234.dragonEggTracker.commands.DragonTrackerCommand;
import me.maxgim234.dragonEggTracker.gui.RecipePreviewGUI;
import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.listeners.*;
import me.maxgim234.dragonEggTracker.tracking.CompassTrackerTask;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonEggTracker extends JavaPlugin {

    private static DragonEggTracker instance;
    private EggManager eggManager;

    @Override
    public void onEnable() {
        instance = this;

        // Create config.yml if not exists
        saveDefaultConfig();

        // Initialize egg manager
        eggManager = new EggManager(this);

        // --------------------
        // Register listeners
        // --------------------
        getServer().getPluginManager().registerEvents(
                new EggPickupListener(this, eggManager), this
        );
        getServer().getPluginManager().registerEvents(
                new InventoryBlockListener(this, eggManager), this
        );
        getServer().getPluginManager().registerEvents(
                new HopperMoveListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new DispenseListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new EggBlockListener(eggManager), this
        );

        // --------------------
        // Register commands
        // --------------------
        getCommand("dragoneggowner").setExecutor(
                new EggOwnerCommand(eggManager)
        );
        getCommand("dragonegglocate").setExecutor(
                new EggLocateCommand(eggManager)
        );
        getCommand("dragontracker").setExecutor(
                new DragonTrackerCommand(this)
        );

        // --------------------
        // Tracker compass
        // --------------------
        TrackerCompass.init(this);
        TrackerRecipe.register(this);

        long interval = getConfig().getLong(
                "compass-update-interval", 40L
        );

        getServer().getScheduler().runTaskTimer(
                this,
                new CompassTrackerTask(this, eggManager),
                20L,
                interval
        );

        getLogger().info("DragonEggTracker enabled");

        getServer().getPluginManager().registerEvents(
                new RecipePreviewListener(), this
        );

    }

    @Override
    public void onDisable() {
        if (eggManager != null) {
            eggManager.saveData();
        }
        getLogger().info("DragonEggTracker disabled.");
    }

    // --------------------
    // Getters
    // --------------------
    public static DragonEggTracker getInstance() {
        return instance;
    }

    public EggManager getEggManager() {
        return eggManager;
    }
}
