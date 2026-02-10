package me.maxgim234.dragonEggTracker.tracking;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;


public class EggManager {

    private final JavaPlugin plugin;

    private UUID ownerUUID;
    private String ownerName;
    private Location eggLocation;
    private EggState eggState;
    private final File dataFile;
    private final YamlConfiguration data;


    public EggManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.eggState = EggState.UNKNOWN;

        this.dataFile = new File(plugin.getDataFolder(), "egg-data.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);

        loadData();
    }


    // Ownership
    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name;
        saveData();
    }

    public OfflinePlayer getOwner() {
        return ownerUUID == null ? null : Bukkit.getOfflinePlayer(ownerUUID);
    }

    public String getOwnerName() {
        return ownerName;
    }

    // Location
    public void setEggLocation(Location location, EggState state) {
        this.eggLocation = location;
        this.eggState = state;
    }

    public Location getEggLocation() {
        return eggLocation;
    }

    public EggState getEggState() {
        return eggState;
    }

    // Persistence
    public void saveData() {
        try {
            if (ownerUUID != null) {
                data.set("owner.uuid", ownerUUID.toString());
                data.set("owner.name", ownerName);
            }

            if (eggLocation != null) {
                data.set("location.world", eggLocation.getWorld().getName());
                data.set("location.x", eggLocation.getX());
                data.set("location.y", eggLocation.getY());
                data.set("location.z", eggLocation.getZ());
                data.set("location.state", eggState.name());
            }

            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save egg data!");
            e.printStackTrace();
        }
    }


    private void loadData() {
        if (!dataFile.exists()) return;

        if (data.contains("owner.uuid")) {
            ownerUUID = UUID.fromString(data.getString("owner.uuid"));
            ownerName = data.getString("owner.name");
        }

        if (data.contains("location.world")) {
            World world = Bukkit.getWorld(data.getString("location.world"));
            if (world != null) {
                eggLocation = new Location(
                        world,
                        data.getDouble("location.x"),
                        data.getDouble("location.y"),
                        data.getDouble("location.z")
                );
                eggState = EggState.valueOf(
                        data.getString("location.state", "UNKNOWN")
                );
            }
        }
    }

}
