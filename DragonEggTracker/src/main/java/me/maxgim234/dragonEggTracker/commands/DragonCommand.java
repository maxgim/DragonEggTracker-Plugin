package me.maxgim234.dragonEggTracker.commands;

import me.maxgim234.dragonEggTracker.gui.RecipePreviewGUI;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DragonCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final EggManager eggManager;

    public DragonCommand(JavaPlugin plugin, EggManager eggManager) {
        this.plugin = plugin;
        this.eggManager = eggManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // If no arguments, show help
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "owner":
                return handleOwner(sender);

            case "locate":
                return handleLocate(sender);

            case "recipe":
                return handleRecipe(sender);

            case "reload":
                return handleReload(sender);

            default:
                sender.sendMessage("§cUnknown subcommand. Use §e/" + label + " help §cfor a list of commands.");
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("help", "owner", "recipe"));

            if (sender.hasPermission("dragoneggtracker.admin")) {
                completions.add("locate");
                completions.add("reload");
            }

            // autofill
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Tracker §7- §fCommands");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("");
        sender.sendMessage("§e/dragon help §7- §fShows all commands");
        sender.sendMessage("§e/dragon owner §7- §fShows who owns the Dragon Egg");
        sender.sendMessage("§e/dragon recipe §7- §fDisplays the Tracker Compass recipe");

        if (sender.hasPermission("dragoneggtracker.admin")) {
            sender.sendMessage("§e/dragon locate §7- §fShows the Dragon Egg location §c(Admin)");
            sender.sendMessage("§e/dragon reload §7- §fReloads the plugin config §c(Admin)");
        }

        sender.sendMessage("");
        sender.sendMessage("§7You can also use §e/dragoneggtracker §7or §e/det §7instead of §e/dragon");
        sender.sendMessage("§d§l═══════════════════════════════");
    }

    private boolean handleOwner(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        OfflinePlayer owner = eggManager.getOwner();

        if (owner == null || eggManager.getOwnerName() == null) {
            sender.sendMessage("§cNo one has picked up the Dragon Egg yet.");
            return true;
        }

        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Owner");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§7• Player: §f" + eggManager.getOwnerName());
        sender.sendMessage("§7• Online: " + (owner.isOnline() ? "§a✓ Yes" : "§c✗ No"));
        sender.sendMessage("§d§l═══════════════════════════════");

        return true;
    }

    private boolean handleLocate(CommandSender sender) {
        if (!sender.hasPermission("dragoneggtracker.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        Location loc = eggManager.getEggLocation();
        EggState state = eggManager.getEggState();

        if (loc == null || state == EggState.UNKNOWN) {
            sender.sendMessage("§cDragon Egg location is currently unknown.");
            return true;
        }

        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Location");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§7• World: §f" + loc.getWorld().getName());
        sender.sendMessage("§7• X: §f" + loc.getBlockX());
        sender.sendMessage("§7• Y: §f" + loc.getBlockY());
        sender.sendMessage("§7• Z: §f" + loc.getBlockZ());
        sender.sendMessage("§7• State: §f" + state.name());
        sender.sendMessage("§d§l═══════════════════════════════");

        return true;
    }

    private boolean handleRecipe(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can view the recipe.");
            return true;
        }

        player.openInventory(RecipePreviewGUI.create(plugin));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dragoneggtracker.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        try {
            plugin.reloadConfig();
            ContainerUtil.load(plugin);
            TrackerRecipe.register(plugin);

            sender.sendMessage("§a§l✓ §aDragonEggTracker configuration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage("§c§l✗ §cError reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
