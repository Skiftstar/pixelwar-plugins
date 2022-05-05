package kyu.cities.Listeners;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import kyu.cities.Main;
import kyu.cities.Util.CPlayer;
import kyu.cities.Util.CityRank;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        CPlayer p = new CPlayer(e.getPlayer());
        if (p.getRank().getVal() >= CityRank.CITY_COUNCIL.getVal()) {
            displayJoinRequests(p);
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

    private void displayJoinRequests(CPlayer p) {
        List<String> requests = p.getCity().getJoinRequestNames();
        if (requests.size() == 0) return;
        StringBuilder sb = new StringBuilder();
        sb.append(Main.helper.getMess(p.getPlayer(), "OpenJoinRequestsHeader", true));
        for (String s : requests) {
            sb.append(Main.helper.getMess(p.getPlayer(), "OpenJoinRequestsEntry", false)
                .replace("%name", s));
        }
        p.sendMessage(sb.toString());
    }
    
}
