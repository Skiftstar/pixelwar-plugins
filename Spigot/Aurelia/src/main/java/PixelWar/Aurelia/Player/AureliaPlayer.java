package PixelWar.Aurelia.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import PixelWar.Aurelia.World.AureliaWorld;

public class AureliaPlayer {

    private static Map<Player, AureliaPlayer> players = new HashMap<>();
    
    private Player player;
    private AureliaWorld world;
    private UUID profileUUID;
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
        
        setDefaultStats();
        player.teleport(world.getSpawnLocation());
        players.put(player, this);
    }

    public Player getPlayer() {
        return player;
    }

    public int getHealth() {
        return health;
    }

    private void loadStats() {
        health = 10;
        //TODO: this (and remove hardcode above)
    }

    private void setDefaultStats() {
        health = 10;
        //TODO: this (and remove hardcore above)
    }


    public static AureliaPlayer createNew(Player player) {
        return new AureliaPlayer(player);
    }

    public static AureliaPlayer getPlayer(Player player) {
        return players.getOrDefault(player, null);
    }

    public static void removePlayer(Player player) {
        players.remove(player);
    }

}
