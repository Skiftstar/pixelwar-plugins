package Kyu.LobbyCore.Listeners;

import Kyu.LobbyCore.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class LobbyListeners implements Listener {

    public LobbyListeners(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onHunger(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    private void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onItemDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().hasPermission("lobbycore.bypassCancelDropPickup")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onItemPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            if (!( (Player) e.getEntity()).hasPermission("lobbycore.bypassCancelDropPickup")) {
                e.setCancelled(true);
            }
        }
    }

}
