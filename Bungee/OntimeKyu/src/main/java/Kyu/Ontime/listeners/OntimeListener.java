package Kyu.Ontime.Listeners;

import java.util.UUID;

import Kyu.Ontime.Main;
import Kyu.Ontime.Util.Cache;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OntimeListener implements Listener {
    
    public OntimeListener(Main plugin) {
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onJoin(LoginEvent e) {
        addToUUIDCache(e);
        UUID uuid = e.getConnection().getUniqueId();
        Cache.register(uuid, true);
        Cache.checkReset(uuid, System.currentTimeMillis());
    }

    private void addToUUIDCache(LoginEvent e) {
        UUID uuid = e.getConnection().getUniqueId();
        String name = e.getConnection().getName().toLowerCase();
        if (Main.getUuidStorage().get(uuid.toString()) == null || !!Main.getUuidStorage().getString(uuid.toString()).equals(name)) {
            if (Main.getUuidStorage().getString(uuid.toString()) != null) {
                Main.getUuidStorage().set(Main.getUuidStorage().getString(uuid.toString()), null);
            }
            Main.getUuidStorage().set(uuid.toString(), name);
            Main.getUuidStorage().set(name, uuid.toString());
            Main.saveUUIDStorage();
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Cache.handlePlayerLeave(uuid);
    }

}
