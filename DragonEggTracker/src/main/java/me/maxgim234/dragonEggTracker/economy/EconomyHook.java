package me.maxgim234.dragonEggTracker.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyHook {

    private static Economy economy;

    private EconomyHook() {
    }

    /**
     * Attempts to hook into Vault's Economy service. Safe to call even if
     * Vault or an economy plugin isn't installed — it just leaves the hook
     * disabled with no error/warning, per the plugin's requirements.
     */
    public static boolean setup(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            economy = null;
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            economy = null;
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public static boolean isEnabled() {
        return economy != null;
    }

    /**
     * Deposits money into a player's account. No-op if the economy hook
     * isn't available or the amount is not positive.
     */
    public static void deposit(OfflinePlayer player, double amount) {
        if (economy == null) return;
        if (amount <= 0) return;

        economy.depositPlayer(player, amount);
    }
}
