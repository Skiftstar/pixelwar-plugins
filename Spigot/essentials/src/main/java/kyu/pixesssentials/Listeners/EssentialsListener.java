package kyu.pixesssentials.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import kyu.pixesssentials.Main;
import net.kyori.adventure.text.Component;

public class EssentialsListener implements Listener {

    public EssentialsListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        if (e.getPlayer().getBedSpawnLocation() != null) {
            return;
        }
        if (Main.spawnPos != null) {
            e.setRespawnLocation(Main.spawnPos);
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        if (Main.getInstance().getJoinedPlayersConfig().get(e.getPlayer().getUniqueId().toString()) != null) {
            return;
        }
        if (Main.spawnPos != null) {
            e.getPlayer().teleport(Main.spawnPos);
        }
        e.getPlayer().sendMessage(Component.text(Main.helper.getMess(e.getPlayer(), "WelcomeMessage")));
        Main.getInstance().getJoinedPlayersConfig().set(e.getPlayer().getUniqueId().toString(), true);
        Main.getInstance().saveJoinedPlayersConfig();
    }
    
}
