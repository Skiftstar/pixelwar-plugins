package PixelWar.Aurelia.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import PixelWar.Aurelia.Main;
import PixelWar.Aurelia.Player.AureliaPlayer;
import PixelWar.Aurelia.Player.NetworkPlayer;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        NetworkPlayer networkPlayer = NetworkPlayer.addPlayer(player);
        if (!networkPlayer.hasProfiles()) {
            AureliaPlayer.createNew(player);
            return;
        }
        AureliaPlayer aureliaPlayer;
        if (networkPlayer.getLastUsedProfile() == null) {
            aureliaPlayer = new AureliaPlayer(player, networkPlayer.getProfiles().get(0));
        } else {
            aureliaPlayer = new AureliaPlayer(player, networkPlayer.getLastUsedProfile());
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        AureliaPlayer.removePlayer(player);
        NetworkPlayer.removePlayer(player);
    }
    
}
