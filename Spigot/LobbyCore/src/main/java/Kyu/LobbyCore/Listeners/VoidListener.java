package Kyu.LobbyCore.Listeners;

import Kyu.LobbyCore.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidListener implements Listener {

    public static boolean voidReset;

    public VoidListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPlayerVoid(PlayerMoveEvent e) {
        if (e.getTo().getY() < -65 && voidReset && Main.spawnLoc != null) {
            e.getPlayer().teleport(Main.spawnLoc);
        }
    }
}
