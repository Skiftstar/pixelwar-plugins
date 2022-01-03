package Kyu.ServerCore.Bans;

import Kyu.ServerCore.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteHandler implements Listener  {

    public static Map<UUID, Mute> mutedPlayers = new HashMap<>();
    public static Map<UUID, Pair<Integer, Mute>> mutedCache = new HashMap<>();

    public MuteHandler(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (mutedCache.containsKey(p.getUniqueId())) {
            Pair<Integer, Mute> pair = mutedCache.get(p.getUniqueId());
            Bukkit.getScheduler().cancelTask(pair.first);
            if (pair.second != null) mutedPlayers.put(p.getUniqueId(), pair.second);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        mutedCache.remove(p.getUniqueId());
        Mute mute = mutedPlayers.getOrDefault(p.getUniqueId(), null);
        int task = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            mutedCache.remove(p.getUniqueId());
        }, Main.cacheDelay);
        mutedCache.put(p.getUniqueId(), new Pair<>(task, mute));
    }

}
