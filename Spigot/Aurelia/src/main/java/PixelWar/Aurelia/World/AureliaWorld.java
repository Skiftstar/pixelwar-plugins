package PixelWar.Aurelia.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;

public class AureliaWorld {
    
    private World world;
    private UUID worldUUID;
    private UUID ownerUUID;
    private Location spawnLoc;

    private static Map<UUID, AureliaWorld> worlds = new HashMap<>();

    public AureliaWorld(UUID worldUUID) {
        this.worldUUID = worldUUID;
        loadData();

        worlds.put(worldUUID, this);
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

        worlds.put(worldUUID, this);
    }

    public Location getSpawnLocation() {
        spawnLoc = new Location(world, 0, 60, 0, 0, 0);
        //TODO: load spawnLoc dynamically
        return spawnLoc;
    }

    private void setDefaultStats() {
        //TODO: the rest
        loadWorld();
    }

    private void loadData() {
        //TODO: the rest
        loadWorld();
    }

    private void loadWorld() {
        world = WorldManager.loadWorld(worldUUID.toString());
    }

    public static AureliaWorld createNewWorld(UUID ownerUUID) {
        //TODO: check if taken
        UUID worldUUID = UUID.randomUUID();
        WorldManager.copyWorld("templateWorld", worldUUID.toString());
        return new AureliaWorld(worldUUID, ownerUUID);
    }

}
