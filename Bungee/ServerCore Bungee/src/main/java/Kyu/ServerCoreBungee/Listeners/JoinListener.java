package Kyu.ServerCoreBungee.Listeners;

import java.util.UUID;

import Kyu.ServerCoreBungee.Main;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    public JoinListener(Main main) {
        main.getProxy().getPluginManager().registerListener(main, this);
    }

    @EventHandler
    public void onJoin(LoginEvent e) {
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
    
}
