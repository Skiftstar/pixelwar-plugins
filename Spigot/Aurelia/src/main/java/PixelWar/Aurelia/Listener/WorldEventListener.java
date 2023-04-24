package PixelWar.Aurelia.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import PixelWar.Aurelia.Main;

public class WorldEventListener implements Listener {
    
    public WorldEventListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onWorldInit(WorldInitEvent e) {
        
    }
}
