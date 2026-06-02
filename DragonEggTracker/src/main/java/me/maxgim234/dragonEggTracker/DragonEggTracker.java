package me.maxgim234.dragonEggTracker;

import me.maxgim234.dragonEggTracker.commands.DragonCommand;
import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.listeners.*;
import me.maxgim234.dragonEggTracker.tracking.CompassTrackerTask;
import me.maxgim234.dragonEggTracker.tracking.EggBuffTask;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonEggTracker extends JavaPlugin {

    private static DragonEggTracker instance;
    private EggManager eggManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        eggManager = new EggManager(this);

        int pluginId = 31633;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(
                new SimplePie("chart_id", () -> "My value")
        );

        // Register listeners
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
        getServer().getPluginManager().registerEvents(
                new RecipePreviewListener(), this
        );
        getServer().getPluginManager().registerEvents(
                new RecipeCraftListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new EggDropListener(eggManager), this
        );
        getServer().getPluginManager().registerEvents(
                new HopperPickupListener(), this
        );
        getServer().getPluginManager().registerEvents(
                new EggDestructionListener(this, eggManager), this
        );

        // Void listener — kept as a field so we can schedule its polling task
        EggVoidListener eggVoidListener = new EggVoidListener(this, eggManager);
        getServer().getPluginManager().registerEvents(eggVoidListener, this);

        // Poll every 5 ticks — catches items the engine removes before the damage event fires
        getServer().getScheduler().runTaskTimer(
                this,
                eggVoidListener::runVoidCheck,
                20L,
                5L
        );

        // Store EggBuffListener reference for use with EggBuffTask
        EggBuffListener eggBuffListener = new EggBuffListener(this, eggManager);
        getServer().getPluginManager().registerEvents(eggBuffListener, this);

        // tab completion
        DragonCommand dragonCommand = new DragonCommand(this, eggManager);
        getCommand("dragon").setExecutor(dragonCommand);
        getCommand("dragon").setTabCompleter(dragonCommand);

        // Tracker compass
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

        // Buff check task
        if (getConfig().getBoolean("egg-holder-buffs.enabled")) {
            EggBuffTask buffTask = new EggBuffTask(this, eggManager);
            buffTask.setBuffListener(eggBuffListener); // Connect the listener

            getServer().getScheduler().runTaskTimer(
                    this,
                    buffTask,
                    20L,
                    40L // Check every 2 seconds
            );
        }


        getLogger().info("DragonEggTracker enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (eggManager != null) {
            eggManager.saveData();
        }
        getLogger().info("DragonEggTracker disabled.");
    }

    // Getters
    public static DragonEggTracker getInstance() {
        return instance;
    }

    public EggManager getEggManager() {
        return eggManager;
    }
}
