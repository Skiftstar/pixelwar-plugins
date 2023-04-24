package PixelWar.Aurelia.World;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import PixelWar.Aurelia.Util.WorldManager;

public class AureliaWorld {
    
    private World world;
    private UUID worldUUID;
    private UUID ownerUUID;
    private Location spawnLoc;

    public AureliaWorld(UUID worldUUID) {
        this.worldUUID = worldUUID;
        loadData();
    }

    /**
     * Used when creating a new world when a new profile is created
     * @param worldUUID
     * @param ownerUUID
     */
    public AureliaWorld(UUID worldUUID, UUID ownerUUID) {
        this.worldUUID = worldUUID;
        this.ownerUUID = ownerUUID;
        setDefaultStats();
    }

    public Location getSpawnLocation() {
        spawnLoc = new Location(world, 0, 60, 0, 0, 0);
        //TODO: load spawnLoc dynamically
        return spawnLoc;
    }

    private void setDefaultStats() {
        //TODO: this
    }

    private void loadData() {
        //TODO: this
    }

    public static AureliaWorld createNewWorld(UUID ownerUUID) {
        //TODO: check if taken
        UUID worldUUID = UUID.randomUUID();
        WorldManager.copyWorld(Bukkit.getWorld("template"), worldUUID.toString());
        return new AureliaWorld(worldUUID, ownerUUID);
    }

}
