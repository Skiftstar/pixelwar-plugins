package Derio.Ontime.listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import Derio.Ontime.Main;
import Derio.Ontime.utils.Cache;
import Derio.Ontime.utils.PlayerData;
import Derio.Ontime.utils.Util;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OntimeListener implements Listener {
    public OntimeListener(Main main) {
        main.getProxy().getPluginManager().registerListener(main, this);
    }
    PlayerData data;

    @EventHandler
    public void onJoin(LoginEvent e) throws IOException {
        UUID uuid = e.getConnection().getUniqueId();
        if (Util.getLastUpdate(uuid.toString()) == -1L) {
            Util.registerUser(uuid.toString());
        }

        Util.setLastUpdate(uuid.toString(), System.currentTimeMillis());
        Cache.lastLogin.put(uuid.toString(), System.currentTimeMillis());


        data = Cache.data;

        if (!data.isInConfig(uuid, e.getConnection().getName().toLowerCase())){
            data.set(uuid,e.getConnection().getName().toLowerCase());
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Util.addPlaytime(uuid.toString());

        Cache.lastLogin.remove(uuid);
        Cache.playtimeDay.remove(uuid);
        Cache.playtimeWeek.remove(uuid);
        Cache.playtimeTotal.remove(uuid);
        Cache.playtimeMonth.remove(uuid);
    }
}