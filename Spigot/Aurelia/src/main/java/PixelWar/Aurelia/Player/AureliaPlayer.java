package PixelWar.Aurelia.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import PixelWar.Aurelia.Player.Attributes.PlayerAttribute;
import PixelWar.Aurelia.World.AureliaWorld;

public class AureliaPlayer {

    private static Map<Player, AureliaPlayer> players = new HashMap<>();
    
    private Player player;
    private AureliaWorld world;
    private UUID profileUUID;
    private int level;
    private int chunkPoints;
    private int attributePoints;
    private Map<PlayerAttribute, Integer> attributes;
    private int health;

    public AureliaPlayer(Player player, UUID profileUUID) {
        this.player = player;
        this.profileUUID = profileUUID;
        NetworkPlayer.get(player).setLastUsedProfile(profileUUID);
        loadStats();
        players.put(player, this);
    }

    /**
     * Used for when creating a new Profile, do not use to load profiles!
     * @param player
     */
    public AureliaPlayer(Player player) {
        //TODO: Test if UUID is taken
        UUID profileUUID = UUID.randomUUID();
        AureliaWorld world = AureliaWorld.createNewWorld(profileUUID);

        this.player = player;
        this.profileUUID = profileUUID;
        this.world = world;
        
        applyDefaultStats();
        player.teleport(world.getSpawnLocation());
        players.put(player, this);
    }

    private void loadStats() {
        health = 10;
        //TODO: this (and remove hardcoded above)
    }

    private void applyDefaultStats() {
        health = 10;
        //TODO: this (and remove hardcoded above)
    }

    //#region Setter



    //#endregion

    //#region Getter

    public Player getPlayer() {
        return player;
    }

    public int getHealth() {
        return health;
    }

    public int getAttributePoints() {
        return attributePoints;
    }

    public int getChunkPoints() {
        return chunkPoints;
    }

    public Integer getAttributeLevel(PlayerAttribute attribute) {
        return attributes.getOrDefault(attribute, attribute.getStartValue());
    }

    //#endregion

    //#region Static Methods

    public static AureliaPlayer createNew(Player player) {
        return new AureliaPlayer(player);
    }

    public static AureliaPlayer getPlayer(Player player) {
        return players.getOrDefault(player, null);
    }

    public static void removePlayer(Player player) {
        players.remove(player);
        //TODO: Unload world if noone is online in it
    }

    //#endregion
}
