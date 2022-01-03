package Kyu.ServerCore.Listeners;

import Kyu.ServerCore.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        e.joinMessage(Component.text(""));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Main.helper.getMess(p, "Join")
                    .replace("%player", ((TextComponent) e.getPlayer().displayName()).content()));
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.text(""));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Main.helper.getMess(p, "Leave")
                    .replace("%player", ((TextComponent) e.getPlayer().displayName()).content()));
        }
    }

}
