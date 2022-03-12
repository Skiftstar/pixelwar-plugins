package kyu.cities.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import kyu.cities.Main;
import kyu.cities.Util.CPlayer;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        new CPlayer(e.getPlayer());
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        CPlayer.players.remove(e.getPlayer());
    }
    
}
