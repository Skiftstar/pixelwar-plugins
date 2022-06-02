package kyu.cities.Listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import kyu.cities.Main;
import kyu.cities.Util.City.CityRank;
import kyu.cities.Util.Player.CPlayer;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        CPlayer p = new CPlayer(e.getPlayer());
        if (p.getRank().getVal() >= CityRank.CITY_COUNCIL.getVal()) {
            boolean showEmptyMessage = false;
            p.getCity().displayJoinRequests(p, showEmptyMessage);
        }
        p.handleOfflineMessages();

        YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();
        nameMapper.set(e.getPlayer().getName().toLowerCase(), e.getPlayer().getUniqueId().toString());
        Main.saveConfig(nameMapper);
        
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
