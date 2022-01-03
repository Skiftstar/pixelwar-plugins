package Kyu.LobbyCore.Listeners;

import Kyu.LobbyCore.Main;
import Kyu.LobbyCore.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JoinListener implements Listener {

    public static GameMode defaultMode;
    public static ItemStack compass;

    public JoinListener(Main plugin) {
        createItems();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("lobbycore.bypassGamemode")) {
            p.setGameMode(defaultMode);
        }
        if (Main.spawnLoc != null && !p.hasPermission("lobbycore.bypassTeleport")) {
            p.teleport(Main.spawnLoc);
        }
        if (!p.hasPermission("lobbycore.bypassClear")) {
            p.getInventory().clear();
            p.getInventory().setItem(4, compass);
        }
    }

    private void createItems() {
        compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.displayName(Component.text(Util.color("&aServers")));
        compass.setItemMeta(meta);
    }
}
