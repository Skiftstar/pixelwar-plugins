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
    private UUID dataUUID;
    private int health;

    public AureliaPlayer(Player p, UUID dataUUID) {
        this.player = p;
        this.dataUUID = dataUUID;
        NetworkPlayer.get(p).setLastUsedProfile(dataUUID);
        loadStats();
    }

    /**
     * Used for when creating a new Profile, do not use to load profiles!
     * @param player
     */
    public AureliaPlayer(Player player) {
        //TODO: Test if UUID is taken
        UUID profileUUID = UUID.randomUUID();
        AureliaWorld world = AureliaWorld.createNewWorld(profileUUID);
        setDefaultStats();
        player.teleport(world.getSpawnLocation());
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

    public static AureliaPlayer addPlayer(Player player, UUID dataUUID) {
        AureliaPlayer aureliaPlayer = new AureliaPlayer(player, dataUUID);
        players.put(player, aureliaPlayer);
        return aureliaPlayer;
    }

    public static void removePlayer(Player player) {
        players.remove(player);
    }

}
