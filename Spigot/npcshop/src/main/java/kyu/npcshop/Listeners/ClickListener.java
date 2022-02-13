package kyu.npcshop.Listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import kyu.npcshop.Main;
import kyu.npcshop.CustomVillagers.CstmVillager;
import kyu.npcshop.CustomVillagers.GUI.GUI;
import kyu.npcshop.CustomVillagers.GUI.Item.GuiItem;
import kyu.npcshop.CustomVillagers.GUI.Windows.ChestWindow;

public class ClickListener implements Listener {

    public static Map<UUID, CstmVillager> villagers = new HashMap<>();
    
    public ClickListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onRightClickVillager(PlayerInteractEntityEvent e) {
        if (!villagers.containsKey(e.getRightClicked().getUniqueId())) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        GUI gui = new GUI(e.getPlayer(), Main.getInstance());
        ChestWindow window = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerMainMenuTitle").replace("%VillName", e.getRightClicked().getCustomName()), 1);
        GuiItem buyItem = window.setItem(Material.DIAMOND_BLOCK, Main.helper().getMess(p, "BuyItemName"), 0);
        GuiItem sellItem = window.setItem(Material.GOLD_BLOCK, Main.helper().getMess(p, "SellItemName"), 8);
        if (p.hasPermission("npcshop.admin")) {
            GuiItem adminItem = window.setItem(Material.REDSTONE_TORCH, Main.helper().getMess(p, "AdminItemName"), 4);

        }
        gui.openWindow(window);
    }

    @EventHandler
    private void onVillDamage(EntityDamageEvent e) {
        if (villagers.containsKey(e.getEntity().getUniqueId())) e.setCancelled(true);
    }
}
