package kyu.npcshop.CustomVillagers.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import kyu.npcshop.CustomVillagers.GUI.Windows.ChestWindow;
import kyu.npcshop.CustomVillagers.GUI.Windows.Window;

public class GUI implements Listener {

    private Player holder;
    private Window currWindow;
    private JavaPlugin plugin;

    public GUI(Player holder, JavaPlugin plugin) {
        this.holder = holder;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ChestWindow createChestWindow(String title, int rows) {
        ChestWindow window = new ChestWindow(title, rows, this, plugin);
        return window;
    }

    public void openWindow(Window window) {
        currWindow = window;
        window.open();
    }

    public Player getHolder() {
        return holder;
    }

    public Window getCurrWindow() {
        return currWindow;
    }

    @EventHandler
    private void onInvCLick(InventoryClickEvent e) {
        // holder.sendMessage(Component.text("InvClick Triggered!"));
        if (!e.getInventory().equals(currWindow.getInv())) {
            return;
        }
        // holder.sendMessage(Component.text("Handling InvClick!"));
        currWindow.handleInvClick(e);
    }

    @EventHandler
    private void onInvClose(InventoryCloseEvent e) {
        // holder.sendMessage(Component.text("InvClose Triggered!"));
        if (currWindow == null || !e.getInventory().equals(currWindow.getInv())) {
            return;
        }
        // holder.sendMessage(Component.text("Handling InvClose!"));
        Bukkit.getScheduler().runTask(plugin, r -> {
            currWindow.handleClose(e);
            if (currWindow == null) {
                HandlerList.unregisterAll(this);
            }
        });
    }

    public void setCurrentWindow(Window window) {
        this.currWindow = window;
    }
}
