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
import org.bukkit.inventory.ItemStack;

import kyu.npcshop.Main;
import kyu.npcshop.CustomVillagers.CstmVillager;
import kyu.npcshop.CustomVillagers.Trade;
import kyu.npcshop.CustomVillagers.TradeType;
import kyu.npcshop.CustomVillagers.GUI.GUI;
import kyu.npcshop.CustomVillagers.GUI.Item.GuiItem;
import kyu.npcshop.CustomVillagers.GUI.Windows.ChestWindow;
import kyu.npcshop.CustomVillagers.GUI.Windows.TaskbarStyles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ClickListener implements Listener {

    public static Map<UUID, CstmVillager> villagers = new HashMap<>();

    public ClickListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onRightClickVillager(PlayerInteractEntityEvent e) {
        if (!villagers.containsKey(e.getRightClicked().getUniqueId()))
            return;
        e.setCancelled(true);

        Player p = e.getPlayer();
        CstmVillager vill = villagers.get(e.getRightClicked().getUniqueId());

        GUI gui = new GUI(e.getPlayer(), Main.getInstance());
        ChestWindow mainMenu = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerMainMenuTitle")
                .replace("%VillName", e.getRightClicked().getCustomName()), 1);

        // Window where players can BUY items
        ChestWindow buyWindow = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerBuyMenuTitle")
                .replace("%VillName", e.getRightClicked().getCustomName()), 6);
        buyWindow.setMultiPage(true);
        buyWindow.setTaskBarEnabled(true);
        buyWindow.setTaskbarStyle(TaskbarStyles.BOTH);
        buyWindow.setOnClose(ev -> {
            gui.openWindow(mainMenu);
        });

        for (Trade trade : vill.getSells()) {
            GuiItem item = buyWindow.addItem(trade.getItem());
            // Check if player has money, add to inv, etc.
            item.setOnClick(buyAction -> {

            });
        }

        // Window where players can SELL items
        ChestWindow sellWindow = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerSellMenuTitle")
                .replace("%VillName", e.getRightClicked().getCustomName()), 6);
        sellWindow.setMultiPage(true);
        sellWindow.setTaskBarEnabled(true);
        sellWindow.setTaskbarStyle(TaskbarStyles.BOTH);
        sellWindow.setOnClose(ev -> {
            gui.openWindow(mainMenu);
        });

        GuiItem testItem = sellWindow.setItem(Material.BARRIER, "abc", 0);
        testItem.setOnClick(ev -> {
            gui.openWindow(mainMenu);
        });

        for (Trade trade : vill.getBuys()) {
            GuiItem item = sellWindow.addItem(trade.getItem());
            // Check if player has item, remove from inventory, etc.
            item.setOnClick(sellAction -> {

            });
        }

        GuiItem buyItem = mainMenu.setItem(Material.DIAMOND_BLOCK, Main.helper().getMess(p, "BuyItemName"), 0);
        buyItem.setOnClick(ev -> {
            gui.openWindow(buyWindow);
        });
        GuiItem sellItem = mainMenu.setItem(Material.GOLD_BLOCK, Main.helper().getMess(p, "SellItemName"), 8);
        sellItem.setOnClick(ev -> {
            gui.openWindow(sellWindow);
        });

        if (p.hasPermission("npcshop.admin")) {

            ChestWindow adminWindow = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerAdminMenuTitle")
                    .replace("%VillName", e.getRightClicked().getCustomName()), 1);
            adminWindow.setOnClose(ev -> {
                gui.openWindow(mainMenu);
            });

            // #region Add Trade Item
            GuiItem addTradeItem = adminWindow.setItem(Material.EMERALD_BLOCK, Main.helper().getMess(p, "AddTradeItem"),
                    0);
            addTradeItem.setOnClick(ev -> {
                final int[] tradeTypeIndex = new int[] { 0 };
                final int[] amount = new int[] { 1 };
                final ItemStack[] item = new ItemStack[] { null };

                ChestWindow addTradeWindow = gui
                        .createChestWindow(Main.helper().getMess(p, "NPCVillagerAddTradeMenuTitle").replace("%VillName",
                                e.getRightClicked().getCustomName()), 1);
                addTradeWindow.setOnClose(ev1 -> {
                    gui.openWindow(adminWindow);
                });

                // #region Amount Item
                GuiItem amountItem = addTradeWindow.setItem(Material.FEATHER,
                        Main.helper().getMess(p, "AmountItemName").replace("%amount", "" + amount[0]), 1);
                amountItem.setOnClick(ev1 -> {
                    ChestWindow changeAmountWindow = gui.createChestWindow(
                            Main.helper().getMess(p, "NPCVillagerAddTradeChangeAmountTitle").replace("%VillName",
                                    e.getRightClicked().getCustomName()),
                            1);
                    changeAmountWindow.setOnClose(ev2 -> {
                        gui.openWindow(addTradeWindow);
                    });

                    GuiItem displayItem = changeAmountWindow.setItem(Material.FEATHER,
                            Main.helper().getMess(p, "AmountItemName").replace("%amount", "" + amount[0]), 4);
                    int[] values = new int[] { -64, -10, -1, 1, 10, 64 };
                    for (int i = 0; i < 6; i++) {
                        GuiItem button = changeAmountWindow.setItem(Material.STONE_BUTTON, "" + values[i],
                                i < 3 ? i : i + 3);
                        final int finalI = i;
                        button.setOnClick(ev2 -> {
                            amount[0] += values[finalI];
                            if (amount[0] < 1)
                                amount[0] = 1;
                            displayItem.setName(
                                    Main.helper().getMess(p, "AmountItemName").replace("%amount", "" + amount[0]));
                            amountItem.setName(
                                    Main.helper().getMess(p, "AmountItemName").replace("%amount", "" + amount[0]));
                        });
                    }
                    gui.openWindow(changeAmountWindow);
                });
                // #endregion Amount Item

                // #region TradeItem Item
                GuiItem tradeItemItem = addTradeWindow.setItem(Material.BARRIER,
                        Main.helper().getMess(p, "TradeItemItemName").replace("%itemType",
                                item[0] == null ? Main.helper().getMess(p, "NoneItem")
                                        : (item[0].getItemMeta().hasDisplayName()
                                                ? item[0].getItemMeta().displayName().toString()
                                                : item[0].getType().toString())),
                        0);

                tradeItemItem.setOnClick(ev1 -> {
                    ChestWindow tradeItemWindow = gui.createChestWindow(
                            Main.helper().getMess(p, "NPCVillagerChangeTradeItemTitle").replace("%VillName",
                                    e.getRightClicked().getCustomName()),
                            1);
                    tradeItemWindow.setOnClose(ev2 -> {
                        gui.openWindow(addTradeWindow);
                    });

                    //TODO: Add Item via own inv and add Item via Material List

                    gui.openWindow(tradeItemWindow);
                });
                // #endregion TradeItem Item

                // #region Trade Type Item
                GuiItem tradeTypeItem = addTradeWindow.setItem(Material.REDSTONE, Main.helper()
                        .getMess(p, "TradeTypeItem").replace("%type", TradeType.values()[tradeTypeIndex[0]].toString()),
                        3);
                tradeTypeItem.setOnClick(ev1 -> {
                    tradeTypeIndex[0]++;
                    if (tradeTypeIndex[0] > TradeType.values().length - 1)
                        tradeTypeIndex[0] = 0;
                    tradeTypeItem.setName(Main.helper().getMess(p, "TradeTypeItem").replace("%type",
                            TradeType.values()[tradeTypeIndex[0]].toString()));
                });
                // #endregion Trade Type Item

                gui.openWindow(addTradeWindow);
            });

            // #endregion Add Trade Item

            GuiItem adminItem = mainMenu.setItem(Material.REDSTONE_TORCH, Main.helper().getMess(p, "AdminItemName"), 4);
            adminItem.setOnClick(ev -> {
                gui.openWindow(adminWindow);
            });

        }

        gui.openWindow(mainMenu);
    }

    @EventHandler
    private void onVillDamage(EntityDamageEvent e) {
        if (villagers.containsKey(e.getEntity().getUniqueId()))
            e.setCancelled(true);
    }
}
