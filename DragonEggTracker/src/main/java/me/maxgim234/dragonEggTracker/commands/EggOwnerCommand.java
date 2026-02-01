package me.maxgim234.dragonEggTracker.commands;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EggOwnerCommand implements CommandExecutor {

    private final EggManager eggManager;

    public EggOwnerCommand(EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String @NotNull [] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        OfflinePlayer owner = eggManager.getOwner();

        if (owner == null || eggManager.getOwnerName() == null) {
            sender.sendMessage("§cNo one has picked up the Dragon Egg yet.");
            return true;
        }

        sender.sendMessage("§dDragon Egg Owner");
        sender.sendMessage("§7• Player: §f" + eggManager.getOwnerName());
        sender.sendMessage("§7• Online: §f" + (owner.isOnline() ? "Yes" : "No"));

        return true;
    }
}
