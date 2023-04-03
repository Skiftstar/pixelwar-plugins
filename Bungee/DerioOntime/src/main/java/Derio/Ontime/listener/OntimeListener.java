package Derio.Ontime.listener;

import java.io.IOException;
import java.util.UUID;
import Derio.Ontime.Main;
import Derio.Ontime.commands.OntimeCommand;
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

        long current = System.currentTimeMillis();

        Cache.lastLogin.put(uuid.toString(), current);

        Util.tryReset(uuid.toString());
        Util.setLastUpdate(uuid.toString(), current);

        data = Cache.data;

        if (!data.isInConfig(uuid, e.getConnection().getName().toLowerCase())){
            data.set(uuid,e.getConnection().getName().toLowerCase());
        }else {
            Cache.lastLogin.remove(uuid.toString());
            Cache.playtimeDay.remove(uuid.toString());
            Cache.playtimeWeek.remove(uuid.toString());
            Cache.playtimeTotal.remove(uuid.toString());
            Cache.playtimeMonth.remove(uuid.toString());
        }

        long playtimeFromDB = OntimeCommand.getPlaytime(uuid.toString())[3];

        System.out.println("=======ONJOIN=======\nPlaytime from DB: " + playtimeFromDB);
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        long newPlaytimeManualBeforeSave = System.currentTimeMillis() - Cache.lastLogin.get(uuid.toString()) + Cache.playtimeTotal.get(uuid.toString());
        long newPlaytimeFromCacheBeforeSave = OntimeCommand.getPlaytime(uuid.toString())[3];

        Cache.lastLogin.remove(uuid.toString());
        Cache.playtimeDay.remove(uuid.toString());
        Cache.playtimeWeek.remove(uuid.toString());
        Cache.playtimeTotal.remove(uuid.toString());
        Cache.playtimeMonth.remove(uuid.toString());
        
        Util.addPlaytime(uuid.toString());

        long newPlaytimeFromDBAfterSave = Util.getPlaytime(uuid.toString())[3];

        System.out.println("=======ONLEAVE=======\nPlaytime BS Manual: " + newPlaytimeManualBeforeSave + "\n" +
            "Playtime BS From Cache: " + newPlaytimeFromCacheBeforeSave + "\n" +
            "Playtime AS from DB: " + newPlaytimeFromDBAfterSave);


    }
}