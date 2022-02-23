package kyu.npcshop.CustomVillagers.GUI.Windows;

import org.bukkit.plugin.java.JavaPlugin;

import kyu.npcshop.CustomVillagers.GUI.GUI;


import javax.annotation.Nullable;

public class ChestWindow extends DefaultWindow {

    /**
     * Creates a new Default (Chest) Window
     * 
     * @param title  Nullable - Title of the Window
     * @param rows   Amount of rows. Throws Exception if out of bounds
     * @param gui    GUI the window is part of
     * @param plugin Your Plugin
     */
    public ChestWindow(@Nullable String title, int rows, GUI gui, JavaPlugin plugin) {
        super(title, rows, gui, plugin);
        // getHolder().sendMessage(Component.text("Created new Inv with size " + getInv().getSize()));
        // this.plugin = plugin;

        // boolean isRegistered = false;
        // for (RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
        //     if (listener.getListener().equals(this)) {
        //         isRegistered = true;
        //         break;
        //     }
        // }
        // if (!isRegistered) {
        //     plugin.getServer().getPluginManager().registerEvents(this, plugin);
        //     getHolder().sendMessage(Component.text("Registering inventory..."));
        // } else {
        //     getHolder().sendMessage(Component.text("Inventory is already registered!"));
        // }
    }

    @Override
    public void open() {
            super.open();
    }

    /*
     * =============================================================================
     * 
     * Event Handler
     * 
     * =============================================================================
     */

    // // @EventHandler
    // private void onInvClick(InventoryClickEvent e) {
    //     handleInvClick(e);
    // }

    // // @EventHandler
    // private void onCloseEvent(InventoryCloseEvent e) {
        
    // }


    // @EventHandler
    // private void test(InventoryOpenEvent e) {
    //     getHolder().sendMessage(Component.text("Open Inventory Called"));
    // }
}
