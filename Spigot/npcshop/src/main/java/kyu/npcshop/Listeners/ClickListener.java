package kyu.npcshop.Listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import kyu.npcshop.Main;
import kyu.npcshop.Commands.NPCCommand;
import kyu.npcshop.CustomVillagers.CstmVillager;
import kyu.npcshop.CustomVillagers.Trade;
import kyu.npcshop.CustomVillagers.TradeType;
import kyu.npcshop.CustomVillagers.GUI.GUI;
import kyu.npcshop.CustomVillagers.GUI.Item.GuiItem;
import kyu.npcshop.CustomVillagers.GUI.Windows.ChestWindow;
import kyu.npcshop.CustomVillagers.GUI.Windows.SignWindow;
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

        // #region Buy Item
        GuiItem buyItem = mainMenu.setItem(Material.DIAMOND_BLOCK, Main.helper().getMess(p, "BuyItemName"), 0);
        buyItem.setOnClick(ev -> {
            ChestWindow buyWindow = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerBuyMenuTitle")
                    .replace("%VillName", e.getRightClicked().getCustomName()), 6);
            buyWindow.setMultiPage(true);
            buyWindow.setTaskBarEnabled(true);
            buyWindow.setTaskbarStyle(TaskbarStyles.BOTH);
            buyWindow.setOnClose(ev1 -> {
                gui.openWindow(mainMenu);
            });
            for (Trade trade : vill.getSells()) {
                GuiItem item = buyWindow.addItem(new ItemStack(trade.getItem()));
                String Pricelore = Main.helper().getMess(p, "ItemBuyDescription").replace("%price",
                        "" + trade.getMoney());
                List<String> lore = new ArrayList<>();
                lore.add(Pricelore);
                item.setBasicLore(lore);
                item.setOnClick(buyAction -> {
                    ChestWindow buySelectionWindow = gui
                            .createChestWindow(Main.helper().getMess(p, "NPCVillagerBuyMenuTitle"), 1);

                    buySelectionWindow.setOnClose(ev2 -> {
                        gui.openWindow(buyWindow);
                    });

                    // #region BuyMaxItem
                    GuiItem maxItem = buySelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "FullInventoryItemName"), 3);
                    maxItem.setOnClick(ev2 -> {
                        int totalItems = getFreeSpace(trade.getItem(), p);
                        double pBal = Main.econ.getBalance(p);
                        if (pBal < totalItems * trade.getMoney()) {
                            p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughMoney", true)));
                            return;
                        }
                        Main.econ.withdrawPlayer(p, totalItems * trade.getMoney());
                        p.sendMessage(Component.text(Main.helper().getMess(p, "ItemBuySuccess", true)
                                .replace("%money", "" + totalItems * trade.getMoney())
                                .replace("%count", "" + totalItems)));
                        for (int i = 0; i < totalItems; i++) {
                            p.getInventory().addItem(trade.getItem());
                        }
                    });
                    // #endregion BuyMaxItem

                    // #region BuyOneItem
                    GuiItem oneItem = buySelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "OnlyOneItemName"), 0);
                    oneItem.setOnClick(ev2 -> {
                        if (p.getInventory().firstEmpty() == -1 && getFreeSpace(trade.getItem(), p) == 0) {
                            p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughInvSpace", true)));
                            return;

                        }
                        double pBal = Main.econ.getBalance(p);
                        if (pBal < trade.getMoney()) {
                            p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughMoney", true)));
                            return;
                        }
                        Main.econ.withdrawPlayer(p, trade.getMoney());
                        p.sendMessage(Component.text(Main.helper().getMess(p, "ItemBuySuccess", true)
                                .replace("%money", "" + trade.getMoney())
                                .replace("%count", "" + 1)));
                        p.getInventory().addItem(trade.getItem());
                    });
                    // #endregion BuyOneItem

                    // #region BuyStackItem
                    GuiItem stackItem = buySelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "OneStackItemName"), 1);
                    stackItem.setOnClick(ev2 -> {
                        int spaceLeft = getFreeSpace(trade.getItem(), p);
                        if (spaceLeft < 64) {
                            p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughInvSpace", true)));
                            return;
                        }
                        double pBal = Main.econ.getBalance(p);
                        if (pBal < 64 * trade.getMoney()) {
                            p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughMoney", true)));
                            return;
                        }
                        Main.econ.withdrawPlayer(p, trade.getMoney());
                        p.sendMessage(Component.text(Main.helper().getMess(p, "ItemBuySuccess", true)
                                .replace("%money", "" + 64 * trade.getMoney())
                                .replace("%count", "" + 64)));
                        for (int i = 0; i < 64; i++) {
                            p.getInventory().addItem(trade.getItem());
                        }
                    });
                    // #endregion BuyStackItem

                    // #region BuyCustomAmountItem
                    GuiItem customAmountItem = buySelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "CustomAmountItemName"), 2);
                    customAmountItem.setOnClick(ev1 -> {
                        SignWindow window = gui
                                .createSignWindow(Main.helper().getLore(p, "BuySignText").toArray(new String[0]));
                        window.setOnFinish(ev2 -> {
                            String[] lines = ev2.getPacket().getStringArrays().read(0);
                            int count;
                            try {
                                count = Integer.parseInt(lines[0]);
                            } catch (Exception ex) {
                                p.sendMessage(Component.text(Main.helper().getMess(p, "NotANumber", true)));
                                Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                    gui.openWindow(buySelectionWindow);
                                });
                                return;
                            }

                            int spaceLeft = getFreeSpace(trade.getItem(), p);
                            if (spaceLeft < count) {
                                p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughInvSpace", true)));
                                Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                    gui.openWindow(buySelectionWindow);
                                });
                                return;
                            }
                            double pBal = Main.econ.getBalance(p);
                            if (pBal < count * trade.getMoney()) {
                                p.sendMessage(Component.text(Main.helper().getMess(p, "NotEnoughMoney", true)));
                                Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                    gui.openWindow(buySelectionWindow);
                                });
                                return;
                            }
                            Main.econ.withdrawPlayer(p, trade.getMoney());
                            p.sendMessage(Component.text(Main.helper().getMess(p, "ItemBuySuccess", true)
                                    .replace("%money", "" + count * trade.getMoney())
                                    .replace("%count", "" + count)));
                            for (int i = 0; i < count; i++) {
                                p.getInventory().addItem(trade.getItem());
                            }
                            Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                gui.openWindow(buySelectionWindow);
                            });

                        });
                        gui.openWindow(window);
                    });
                    // #endregion BuyCustomAmountItem

                    gui.openWindow(buySelectionWindow);
                });
            }
            gui.openWindow(buyWindow);
        });
        // #endregion Buy Item

        // #region Sell Item
        GuiItem sellItem = mainMenu.setItem(Material.GOLD_BLOCK, Main.helper().getMess(p, "SellItemName"), 8);
        sellItem.setOnClick(ev -> {
            ChestWindow sellWindow = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerSellMenuTitle")
                    .replace("%VillName", e.getRightClicked().getCustomName()), 6);
            sellWindow.setMultiPage(true);
            sellWindow.setTaskBarEnabled(true);
            sellWindow.setTaskbarStyle(TaskbarStyles.BOTH);
            sellWindow.setOnClose(ev1 -> {
                gui.openWindow(mainMenu);
            });
            for (Trade trade : vill.getBuys()) {
                GuiItem item = sellWindow.addItem(new ItemStack(trade.getItem()));
                String Pricelore = Main.helper().getMess(p, "ItemSellDescription").replace("%price",
                        "" + trade.getMoney());
                List<String> lore = new ArrayList<>();
                lore.add(Pricelore);
                item.setBasicLore(lore);
                
                item.setOnClick(sellAction -> {
                    if (!p.getInventory().containsAtLeast(trade.getItem(), 1)) {
                        p.sendMessage(Component.text(Main.helper().getMess(p, "SellItemNotInInv", true)));
                        return;
                    }
                    ChestWindow sellSelectionWindow = gui
                            .createChestWindow(Main.helper().getMess(p, "NPCVillagerSellMenuTitle"), 1);

                    sellSelectionWindow.setOnClose(ev2 -> {
                        gui.openWindow(sellWindow);
                    });

                    // #region SellMaxItem
                    GuiItem maxItem = sellSelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "AllOfKindItemName"), 3);
                    maxItem.setOnClick(ev2 -> {
                        Map<Integer, ? extends ItemStack> map = p.getInventory().all(trade.getItem().getType());
                        int totalCount = 0;
                        for (int i : map.keySet()) {
                            ItemStack itemStack = p.getInventory().getItem(i);
                            if (!itemStack.getItemMeta().equals(trade.getItem().getItemMeta()))
                                continue;
                            totalCount += itemStack.getAmount();
                            p.getInventory().getItem(i).setAmount(0);
                        }
                        Main.econ.depositPlayer(p, totalCount * trade.getMoney());
                        p.sendMessage(Component.text(Main.helper().getMess(p, "ItemSoldSuccess", true)
                                .replace("%money", "" + totalCount * trade.getMoney())
                                .replace("%count", "" + totalCount)));
                    });
                    // #endregion SellMaxItem

                    // #region SellOneItem
                    GuiItem sellOneItem = sellSelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "OnlyOneItemName"), 0);
                    sellOneItem.setOnClick(ev2 -> {
                        Map<Integer, ? extends ItemStack> map = p.getInventory().all(trade.getItem().getType());
                        for (int i : map.keySet()) {
                            ItemStack itemStack = p.getInventory().getItem(i);
                            if (!itemStack.getItemMeta().equals(trade.getItem().getItemMeta()))
                                continue;
                            itemStack.subtract(1);
                            break;
                        }
                        Main.econ.depositPlayer(p, trade.getMoney());
                        p.sendMessage(Component.text(Main.helper().getMess(p, "ItemSoldSuccess", true)
                                .replace("%money", "" + trade.getMoney())
                                .replace("%count", "" + 1)));
                    });
                    // #endregion SellOneItem

                    // #region SellStackItem
                    GuiItem sellOneStackItem = sellSelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "OneStackItemName"), 1);
                    sellOneStackItem.setOnClick(ev2 -> {
                        if (!p.getInventory().containsAtLeast(trade.getItem(), 64)) {
                            p.sendMessage(Component.text(Main.helper().getMess(p, "SellItemNotEnoughItems", true)));
                            return;
                        }
                        Map<Integer, ? extends ItemStack> map = p.getInventory().all(trade.getItem().getType());
                        int countToRemove = 64;
                        for (int i : map.keySet()) {
                            ItemStack itemStack = p.getInventory().getItem(i);
                            if (!itemStack.getItemMeta().equals(trade.getItem().getItemMeta()))
                                continue;
                            int count = itemStack.getAmount();
                            p.sendMessage("" + count);
                            if (count >= countToRemove) {
                                itemStack.subtract(countToRemove);
                                countToRemove = 0;
                                break;
                            } else {
                                itemStack.subtract(count);
                                countToRemove -= count;
                            }
                        }
                        Main.econ.depositPlayer(p, 64 * trade.getMoney());
                        p.sendMessage(Component.text(Main.helper().getMess(p, "ItemSoldSuccess", true)
                                .replace("%money", "" + 64 * trade.getMoney())
                                .replace("%count", "" + 64)));
                    });
                    // #endregion SellStackItem

                    // #region SellCustomAmountItem
                    GuiItem sellCustomAmountItem = sellSelectionWindow.setItem(Material.OAK_SIGN,
                            Main.helper().getMess(p, "CustomAmountItemName"), 2);
                    sellCustomAmountItem.setOnClick(ev2 -> {
                        SignWindow window = gui
                                .createSignWindow(Main.helper().getLore(p, "SellSignText").toArray(new String[0]));
                        window.setOnFinish(ev3 -> {
                            String[] lines = ev3.getPacket().getStringArrays().read(0);

                            int sellCount;
                            try {
                                sellCount = Integer.parseInt(lines[0]);
                            } catch (Exception ex) {
                                p.sendMessage(Component.text(Main.helper().getMess(p, "NotANumber", true)));
                                Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                    gui.openWindow(sellSelectionWindow);
                                });
                                return;
                            }
                            if (!p.getInventory().containsAtLeast(trade.getItem(), sellCount)) {
                                p.sendMessage(Component.text(Main.helper().getMess(p, "SellItemNotEnoughItems", true)));
                                Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                    gui.openWindow(sellSelectionWindow);
                                });
                                return;
                            }
                            Map<Integer, ? extends ItemStack> map = p.getInventory().all(trade.getItem().getType());
                            int countToRemove = sellCount;
                            for (int i : map.keySet()) {
                                ItemStack itemStack = p.getInventory().getItem(i);
                                if (!itemStack.getItemMeta().equals(trade.getItem().getItemMeta()))
                                    continue;
                                int count = itemStack.getAmount();
                                p.sendMessage("" + count);
                                if (count >= countToRemove) {
                                    itemStack.subtract(countToRemove);
                                    countToRemove = 0;
                                    break;
                                } else {
                                    itemStack.subtract(count);
                                    countToRemove -= count;
                                }
                            }
                            Main.econ.depositPlayer(p, sellCount * trade.getMoney());
                            p.sendMessage(Component.text(Main.helper().getMess(p, "ItemSoldSuccess", true)
                                    .replace("%money", "" + sellCount * trade.getMoney())
                                    .replace("%count", "" + sellCount)));
                            Bukkit.getScheduler().runTask(Main.getInstance(), r -> {
                                gui.openWindow(sellSelectionWindow);
                            });
                        });
                        gui.openWindow(window);
                    });
                    // #endregion SellCustomAmountItem

                    gui.openWindow(sellSelectionWindow);
                });
            }
            gui.openWindow(sellWindow);
        });
        // #endregion Sell Item

        // #region AdminMenu

        p.getServer().getName();

        if (p.hasPermission("npcshop.admin")) {

            ChestWindow adminWindow = gui.createChestWindow(Main.helper().getMess(p, "NPCVillagerAdminMenuTitle")
                    .replace("%VillName", e.getRightClicked().getCustomName()), 1);
            adminWindow.setOnClose(ev -> {
                gui.openWindow(mainMenu);
            });

            // #region Change Profession Item
            GuiItem changeProfessionItem = adminWindow.setItem(Material.VILLAGER_SPAWN_EGG,
                    Main.helper().getMess(p, "ChangeProfessionItemName"),
                    3);

            changeProfessionItem.setOnClick(ev -> {
                ChestWindow changeProfessionWindow = gui
                        .createChestWindow(Main.helper().getMess(p, "NPCVillagerChangeProfessionTitle")
                                .replace("%VillName", e.getRightClicked().getCustomName()), 6);

                changeProfessionWindow.setOnClose(ev1 -> {
                    gui.openWindow(adminWindow);
                });
                changeProfessionWindow.setMultiPage(true);
                for (Profession prof : Profession.values()) {
                    GuiItem item = changeProfessionWindow.addItem(Material.PAPER, prof.name());
                    item.setOnClick(ev1 -> {
                        ((Villager) e.getRightClicked()).setProfession(prof);
                        gui.openWindow(adminWindow);
                    });
                }

                gui.openWindow(changeProfessionWindow);
            });
            // #endregion Change Profession Item

            // #region Add Trade Item
            GuiItem addTradeItem = adminWindow.setItem(Material.GREEN_WOOL, Main.helper().getMess(p, "AddTradeItem"),
                    0);
            addTradeItem.setOnClick(ev -> {
                final int[] tradeTypeIndex = new int[] { 0 };
                final double[] price = new double[] { 1.0 };
                final ItemStack[] item = new ItemStack[] { null };

                ChestWindow addTradeWindow = gui
                        .createChestWindow(Main.helper().getMess(p, "NPCVillagerAddTradeMenuTitle").replace("%VillName",
                                e.getRightClicked().getCustomName()), 1);
                addTradeWindow.setOnClose(ev1 -> {
                    gui.openWindow(adminWindow);
                });

                // #region Price Item
                GuiItem priceItem = addTradeWindow.setItem(Material.GOLD_NUGGET,
                        Main.helper().getMess(p, "PriceItemName").replace("%price", "" + price[0]), 1);
                priceItem.setOnClick(ev1 -> {
                    ChestWindow changePriceWindow = gui.createChestWindow(
                            Main.helper().getMess(p, "NPCVillagerAddTradeChangePriceTitle").replace("%VillName",
                                    e.getRightClicked().getCustomName()),
                            1);
                    changePriceWindow.setOnClose(ev2 -> {
                        gui.openWindow(addTradeWindow);
                    });

                    GuiItem displayItem = changePriceWindow.setItem(Material.GOLD_NUGGET,
                            Main.helper().getMess(p, "PriceItemName").replace("%price", "" + price[0]), 4);
                    double[] values = new double[] { -100, -10, -1, -0.1, 0.1, 1, 10, 100 };
                    for (int i = 0; i < 8; i++) {
                        GuiItem button = changePriceWindow.setItem(Material.STONE_BUTTON, "" + values[i],
                                i < 4 ? i : i + 1);
                        final int finalI = i;
                        button.setOnClick(ev2 -> {
                            price[0] += values[finalI];
                            price[0] = round(price[0], 1);
                            if (price[0] < 0.1)
                                price[0] = 0.1;
                            displayItem.setName(
                                    Main.helper().getMess(p, "AmountItemName").replace("%amount", "" + price[0]));
                            priceItem.setName(
                                    Main.helper().getMess(p, "AmountItemName").replace("%amount", "" + price[0]));
                        });
                    }
                    gui.openWindow(changePriceWindow);
                });
                // #endregion Price Item

                // #region TradeItem Item
                GuiItem tradeItemItem = addTradeWindow.setItem(Material.BARRIER,
                        Main.helper().getMess(p, "TradeItemItemName").replace("%itemType",
                                item[0] == null ? Main.helper().getMess(p, "NoneItem")
                                        : (item[0].getItemMeta().hasDisplayName()
                                                ? ((TextComponent) item[0].getItemMeta().displayName()).content()
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

                    tradeItemWindow.setOnPInvClick(ev2 -> {
                        item[0] = ev2.getCurrentItem();
                        tradeItemItem.setItemStack(new ItemStack(ev2.getCurrentItem()));
                        tradeItemItem.setName(Main.helper().getMess(p, "TradeItemItemName").replace("%itemType",
                                item[0] == null ? Main.helper().getMess(p, "NoneItem")
                                        : (item[0].getItemMeta().hasDisplayName()
                                                ? ((TextComponent) item[0].getItemMeta().displayName()).content()
                                                : item[0].getType().toString())));
                        gui.openWindow(addTradeWindow);
                    });

                    GuiItem helpItem = tradeItemWindow.setItem(Material.PAPER, Main.helper().getMess(p, "HelpItemName"),
                            8);
                    helpItem.setBasicLore(Main.helper().getLore(p, "TradeAddItemHelpDescription"));

                    // #region Add from Material List Item
                    GuiItem fromMaterials = tradeItemWindow.setItem(Material.BOOK,
                            Main.helper().getMess(p, "TradeItemAddFromMaterialsName"), 0);
                    fromMaterials.setOnClick(ev2 -> {
                        ChestWindow fromMaterialsWindow = gui
                                .createChestWindow(Main.helper().getMess(p, "SelectItemFromMaterialsTitle"), 6);
                        fromMaterialsWindow.setMultiPage(true);
                        fromMaterialsWindow.setTaskBarEnabled(true);
                        fromMaterialsWindow.setTaskbarStyle(TaskbarStyles.MIDDLE);
                        fromMaterialsWindow.setOnClose(ev3 -> {
                            gui.openWindow(tradeItemWindow);
                        });
                        List<Material> list = new ArrayList<>(Arrays.asList(Material.values()));
                        Collections.sort(list, new Comparator<Material>() {
                            @Override
                            public int compare(Material o1, Material o2) {
                                return o1.toString().compareTo(o2.toString());
                            }
                        });
                        for (Material mat : list) {
                            ItemStack is = new ItemStack(mat);
                            if (is.getItemMeta() == null || !mat.isItem())
                                continue;
                            GuiItem matItem = fromMaterialsWindow.addItem(is);
                            matItem.setOnClick(ev3 -> {
                                item[0] = new ItemStack(mat);
                                tradeItemItem.setItemStack(new ItemStack(is));
                                tradeItemItem.setName(Main.helper().getMess(p, "TradeItemItemName").replace("%itemType",
                                        item[0] == null ? Main.helper().getMess(p, "NoneItem")
                                                : (item[0].getItemMeta().hasDisplayName()
                                                        ? ((TextComponent) item[0].getItemMeta().displayName())
                                                                .content()
                                                        : item[0].getType().toString())));
                                gui.openWindow(addTradeWindow);
                            });
                        }

                        gui.openWindow(fromMaterialsWindow);
                    });
                    // #endregion Add from Material List Item

                    gui.openWindow(tradeItemWindow);
                });
                // #endregion TradeItem Item

                // #region Trade Type Item
                GuiItem tradeTypeItem = addTradeWindow.setItem(Material.REDSTONE, Main.helper()
                        .getMess(p, "TradeTypeItem").replace("%type", TradeType.values()[tradeTypeIndex[0]].toString()),
                        2);
                tradeTypeItem.setOnClick(ev1 -> {
                    tradeTypeIndex[0]++;
                    if (tradeTypeIndex[0] > TradeType.values().length - 1)
                        tradeTypeIndex[0] = 0;
                    tradeTypeItem.setName(Main.helper().getMess(p, "TradeTypeItem").replace("%type",
                            TradeType.values()[tradeTypeIndex[0]].toString()));
                });
                // #endregion Trade Type Item

                // #region Save Trade Item

                GuiItem saveTradeItem = addTradeWindow.setItem(Material.GREEN_WOOL,
                        Main.helper().getMess(p, "ConfirmAddTradeItem"), 8);
                saveTradeItem.setOnClick(ev1 -> {
                    if (item[0] == null) {
                        p.sendMessage(Component.text(Main.helper().getMess(p, "AddTradeItemMissingError", true)));
                        return;
                    }
                    Trade trade = new Trade(TradeType.values()[tradeTypeIndex[0]], item[0], price[0]);
                    vill.addTrade(trade);
                    p.sendMessage(Component.text(Main.helper().getMess(p, "TradeAddSuccess", true)));
                    gui.openWindow(adminWindow);
                });

                // #endregion Save Trade Item

                gui.openWindow(addTradeWindow);
            });
            // #endregion Add Trade Item

            // #region Remove Trade Item
            GuiItem removeTradeItem = adminWindow.setItem(Material.RED_WOOL,
                    Main.helper().getMess(p, "RemoveTradeItem"),
                    8);

            removeTradeItem.setOnClick(ev1 -> {
                ChestWindow removeTradesMenu = gui
                        .createChestWindow(Main.helper().getMess(p, "NPCVillagerRemoveTradeTitle")
                                .replace("%VillName", e.getRightClicked().getCustomName()), 1);
                removeTradesMenu.setOnClose(ev2 -> {
                    gui.openWindow(adminWindow);
                });

                GuiItem VillSellsItem = removeTradesMenu.setItem(Material.DIAMOND_BLOCK,
                        Main.helper().getMess(p, "ItemsThatVillSellsItenName"),
                        0);
                VillSellsItem.setOnClick(ev2 -> {
                    ChestWindow buysListWindow = gui
                            .createChestWindow(Main.helper().getMess(p, "NPCVillagerRemoveTradeTitle")
                                    .replace("%VillName", e.getRightClicked().getCustomName()), 6);

                    buysListWindow.setMultiPage(true);
                    buysListWindow.setTaskBarEnabled(true);
                    buysListWindow.setTaskbarStyle(TaskbarStyles.BOTH);
                    buysListWindow.setOnClose(ev3 -> {
                        gui.openWindow(removeTradesMenu);
                    });

                    for (Trade trade : vill.getSells()) {
                        GuiItem item = buysListWindow.addItem(new ItemStack(trade.getItem()));
                        String Pricelore = Main.helper().getMess(p, "ItemBuyDescription").replace("%price",
                                "" + trade.getMoney());
                        String typelore = Main.helper().getMess(p, "ItemTypeDescription").replace("%type",
                                trade.getType().toString());
                        List<String> lore = new ArrayList<>();
                        lore.add(Pricelore);
                        lore.add(typelore);
                        item.setBasicLore(lore);
                        item.setOnClick(ev3 -> {
                            vill.removeFromSells(trade);
                            gui.openWindow(removeTradesMenu);
                        });
                    }

                    gui.openWindow(buysListWindow);
                });

                GuiItem VillBuysItem = removeTradesMenu.setItem(Material.GOLD_BLOCK,
                        Main.helper().getMess(p, "ItemsThatVillBuysItenName"), 8);
                VillBuysItem.setOnClick(ev2 -> {
                    ChestWindow sellsListWindow = gui
                            .createChestWindow(Main.helper().getMess(p, "NPCVillagerRemoveTradeTitle")
                                    .replace("%VillName", e.getRightClicked().getCustomName()), 6);

                    sellsListWindow.setMultiPage(true);
                    sellsListWindow.setTaskBarEnabled(true);
                    sellsListWindow.setTaskbarStyle(TaskbarStyles.BOTH);
                    sellsListWindow.setOnClose(ev3 -> {
                        gui.openWindow(removeTradesMenu);
                    });

                    for (Trade trade : vill.getBuys()) {
                        GuiItem item = sellsListWindow.addItem(new ItemStack(trade.getItem()));
                        String Pricelore = Main.helper().getMess(p, "ItemSellDescription").replace("%price",
                                "" + trade.getMoney());
                        String typelore = Main.helper().getMess(p, "ItemTypeDescription").replace("%type",
                                trade.getType().toString());
                        List<String> lore = new ArrayList<>();
                        lore.add(Pricelore);
                        lore.add(typelore);
                        item.setBasicLore(lore);
                        item.setOnClick(ev3 -> {
                            vill.removeFromBuys(trade);
                            gui.openWindow(removeTradesMenu);
                        });
                    }

                    gui.openWindow(sellsListWindow);
                });

                gui.openWindow(removeTradesMenu);
            });
            // #endregion Remove Trade Item

            // #region Select Villager Item
            GuiItem selectVillagerItem = adminWindow.setItem(Material.STICK,
                    Main.helper().getMess("SelectVillagerItemName"),
                    5);
            selectVillagerItem.setOnClick(ev1 -> {
                NPCCommand.setSelectedVill(p, e.getRightClicked());
                p.sendMessage(Component.text(Main.helper().getMess(p, "VillagerSelected", true).replace("%VillName",
                        e.getRightClicked().getCustomName())));
                gui.close();
            });
            // #endregion Select Villager Item

            // #region Delete Villager Item
            GuiItem deleteVillagerItem = adminWindow.setItem(Material.BARRIER,
                    Main.helper().getMess("DeleteVillagerItemName").replace("%VillName",
                            e.getRightClicked().getCustomName()),
                    4);
            deleteVillagerItem.setOnClick(ev1 -> {
                ChestWindow confirmWindow = gui
                        .createChestWindow(Main.helper().getMess(p, "DeleteVillTitle").replace("%VillName",
                                e.getRightClicked().getCustomName()), 1);
                confirmWindow.setOnClose(ev2 -> {
                    gui.openWindow(adminWindow);
                });

                GuiItem confirmItem = confirmWindow.setItem(Material.GREEN_WOOL,
                        Main.helper().getMess(p, "ConfirmItem"), 0);
                confirmItem.setOnClick(ev2 -> {
                    NPCCommand.removeVill(e.getRightClicked());
                    gui.close();
                    e.getRightClicked().remove();
                    p.sendMessage(Component.text(Main.helper().getMess(p, "VillagerDeleted", true).replace("%VillName",
                            e.getRightClicked().getCustomName())));
                });

                GuiItem whoopsItem = confirmWindow.setItem(Material.RED_WOOL, Main.helper().getMess(p, "WhoopsItem"),
                        8);
                whoopsItem.setOnClick(ev2 -> {
                    gui.openWindow(adminWindow);
                });

                gui.openWindow(confirmWindow);
            });
            // #endregion Delete Villager Item

            GuiItem adminItem = mainMenu.setItem(Material.REDSTONE_TORCH, Main.helper().getMess(p, "AdminItemName"), 4);
            adminItem.setOnClick(ev -> {
                gui.openWindow(adminWindow);
            });

        }

        // #endregion AdminMenu

        gui.openWindow(mainMenu);
    }

    @EventHandler
    private void onVillDamage(EntityDamageEvent e) {
        if (villagers.containsKey(e.getEntity().getUniqueId()))
            e.setCancelled(true);
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private int getFreeSpace(ItemStack item, Player p) {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = p.getInventory().getItem(i);
            if (itemStack == null) {
                count += item.getType().getMaxStackSize();
                continue;
            }
            if (itemStack.getType().equals(item.getType())) {
                if (!itemStack.getItemMeta().equals(item.getItemMeta()))
                    continue;
                count += itemStack.getType().getMaxStackSize() - itemStack.getAmount();
            }
        }
        return count;
    }
}
