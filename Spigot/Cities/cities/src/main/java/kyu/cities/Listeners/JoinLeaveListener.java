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
        CPlayer p = new CPlayer(e.getPlayer());
        p.handleOfflineMessages();
        Main.getInstance().getNameMapperConfig().set(e.getPlayer().getName().toLowerCase(), e.getPlayer().getUniqueId().toString());
        Main.saveConfig(Main.getInstance().getNameMapperConfig());
        
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        CPlayer p = CPlayer.players.get(e.getPlayer());
        if (p.getCity() != null) {
            p.getCity().removeOnlinePlayer(p);
        }
        CPlayer.players.remove(e.getPlayer());
    }
    
}
