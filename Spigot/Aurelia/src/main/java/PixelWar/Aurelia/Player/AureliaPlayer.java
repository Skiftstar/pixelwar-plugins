package PixelWar.Aurelia.Player;

import org.bukkit.entity.Player;

public class AureliaPlayer {
    
    private Player player;
    private int health;

    public AureliaPlayer(Player p) {
        this.player = p;
        loadStats();
    }

    public Player getPlayer() {
        return player;
    }

    public int getHealth() {
        return health;
    }

    private void loadStats() {
        health = 10;
        //TODO: this
    }

}
