package kyu.pixesssentials.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import kyu.pixesssentials.Main;
import kyu.pixesssentials.Commands.SmallCommands;
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

    @EventHandler(priority = EventPriority.MONITOR)
    private void onMove(PlayerMoveEvent e) {
        if (SmallCommands.warpTaks.containsKey(e.getPlayer())) {
            int taskId = SmallCommands.warpTaks.get(e.getPlayer());
            Bukkit.getScheduler().cancelTask(taskId);
            e.getPlayer().sendMessage(Component.text(Main.helper.getMess(e.getPlayer(), "TeleportCanceled", true)));
            SmallCommands.warpTaks.remove(e.getPlayer());
        }
    }
    
}
