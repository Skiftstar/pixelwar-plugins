package Kyu.LobbyCore.Listeners;

import Kyu.GuiAPI.GUI;
import Kyu.GuiAPI.Item.GuiItem;
import Kyu.GuiAPI.Util.Util;
import Kyu.GuiAPI.Windows.ChestWindow;
import Kyu.LobbyCore.Main;
import Kyu.LobbyCore.Server;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {

    private Main plugin;

    public ItemListener(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onItemUse(PlayerInteractEvent e) {
        if (e.getItem() == null) {
            return;
        }
        if (!e.getAction().isRightClick()) {
            return;
        }
        if (e.getItem().equals(JoinListener.compass)) {
            onCompassUse(e);
            return;
        }
    }

    private void onCompassUse(PlayerInteractEvent e) {
        openServerGUI(e.getPlayer());
    }

    public void openServerGUI(Player p) {
        GUI gui = new GUI(p, plugin);
        ChestWindow window = gui.createChestWindow(Util.color(Main.navigatorTitle), Main.navigatorRows);
        for (Server server : Main.servers) {
            GuiItem item = window.setItem(server.getGuiBlock(), server.getGuiName(), server.getSlot());
            item.setLore(server.getDescription());
            item.setOnClick(click -> {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(server.getServerName());
                p.sendPluginMessage(this.plugin, "BungeeCord", out.toByteArray());
            });
        }
        gui.openWindow(window);
    }
}
