package Derio.Ontime.Ontime.listener;

import java.util.UUID;

import Derio.Ontime.Main;
import Derio.Ontime.Ontime.utils.Util;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OntimeListener implements Listener {
    public OntimeListener(Main main) {
        main.getProxy().getPluginManager().registerListener(main, this);
    }

    @EventHandler
    public void onJoin(LoginEvent e) {
        UUID uuid = e.getConnection().getUniqueId();
        if (Util.getLastUpdate(uuid.toString()) == -1L) {
            Util.registerUser(uuid.toString());
        }

        Util.setLastUpdate(uuid.toString(), System.currentTimeMillis());
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Util.addPlaytime(uuid.toString());
    }
}