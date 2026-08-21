package me.maxgim234.dragonEggTracker;

import me.maxgim234.dragonEggTracker.commands.DragonCommand;
import me.maxgim234.dragonEggTracker.economy.EconomyHook;
import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.listeners.*;
import me.maxgim234.dragonEggTracker.placeholder.DragonEggPlaceholders;
import me.maxgim234.dragonEggTracker.tracking.CompassTrackerTask;
import me.maxgim234.dragonEggTracker.tracking.EggBuffTask;
import me.maxgim234.dragonEggTracker.tracking.EggHoldTrackerTask;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggStatsManager;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import dev.faststats.bukkit.BukkitContext;


import org.bukkit.plugin.java.JavaPlugin;

public final class DragonEggTracker extends JavaPlugin {


    private static DragonEggTracker instance;
    private EggManager eggManager;
    private EggStatsManager statsManager;

    private final BukkitContext context = new BukkitContext.Factory(this, "168688ab401d457188de2487dfa34ff3")
            .metrics(dev.faststats.Metrics.Factory::create)
            .create();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        eggManager = new EggManager(this);
        statsManager = new EggStatsManager(this);

        if (EconomyHook.setup(this)) {
            getLogger().info("Vault economy hook enabled.");
        }

        int pluginId = 31633;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(
                new SimplePie("chart_id", () -> "My value")
        );

        // Register listeners
        getServer().getPluginManager().registerEvents(
                new EggPickupListener(this, eggManager, statsManager), this
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
                new EggBlockListener(eggManager, this), this
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
        getServer().getPluginManager().registerEvents(
                new GuiRestrictionListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new EggTheftListener(this, eggManager), this
        );
        // Dragon Egg void rescue
        getServer().getPluginManager().registerEvents(
                new EggVoidListener(this, eggManager), this );

        // Store EggBuffListener reference for use with EggBuffTask
        EggBuffListener eggBuffListener = new EggBuffListener(this, eggManager);
        getServer().getPluginManager().registerEvents(eggBuffListener, this);

        // tab completion
        DragonCommand dragonCommand = new DragonCommand(this, eggManager, statsManager);
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

        // Hold-time tracking + hourly economy reward — runs every second
        getServer().getScheduler().runTaskTimer(
                this,
                new EggHoldTrackerTask(this, eggManager, statsManager),
                20L,
                20L
        );

        // PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DragonEggPlaceholders(this, eggManager, statsManager).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        // Periodic autosave (egg data + stats), 0 disables it (only saves on plugin disable)
        long autoSaveInterval = getConfig().getLong("auto-save-interval", 6000L);
        if (autoSaveInterval > 0) {
            getServer().getScheduler().runTaskTimer(
                    this,
                    () -> {
                        eggManager.saveData();
                        statsManager.save();
                    },
                    autoSaveInterval,
                    autoSaveInterval
            );
        }

        context.ready();

        getLogger().info("DragonEggTracker enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (eggManager != null) {
            eggManager.saveData();
        }
        if (statsManager != null) {
            statsManager.save();
        }

        context.shutdown();

        getLogger().info("DragonEggTracker disabled.");
    }

    // Getters
    public static DragonEggTracker getInstance() {
        return instance;
    }

    public EggManager getEggManager() {
        return eggManager;
    }

    public EggStatsManager getStatsManager() {
        return statsManager;
    }
}
