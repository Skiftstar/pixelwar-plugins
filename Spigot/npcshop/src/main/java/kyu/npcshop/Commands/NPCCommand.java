package kyu.npcshop.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kyu.npcshop.Main;
import kyu.npcshop.CustomVillagers.CstmVillager;
import kyu.npcshop.CustomVillagers.Trade;
import kyu.npcshop.CustomVillagers.TradeType;
import kyu.npcshop.Listeners.ClickListener;
import kyu.npcshop.Util.Pair;
import kyu.npcshop.Util.Util;
import net.kyori.adventure.text.Component;

public class NPCCommand implements CommandExecutor, TabCompleter {

    public static Map<Player, Entity> selectedVills = new HashMap<>();

    public NPCCommand(Main plugin) {
        plugin.getCommand("npc").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String string,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.helper().getMess("PlayerOnly", true));
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("npcshop.admin")) {
            p.sendMessage(Component.text(Main.helper().getMess(p, "NEPerms", true)));
            return false;
        }

        if (args.length < 1) {
            p.sendMessage(Component.text(Main.helper().getMess(p, "NEArgs", true)));
            return false;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                p.sendMessage(Component.text(Main.helper().getMess(p, "NEArgs", true)));
                return false;
            }
            Villager villager = (Villager) p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER,
                    CreatureSpawnEvent.SpawnReason.CUSTOM);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setCollidable(false);
            villager.setPersistent(true);
            villager.setRemoveWhenFarAway(false);
            villager.setSilent(true);
            StringBuilder name = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                name.append(args[i]);
                if (i < args.length - 1) {
                    name.append(" ");
                }
            }
            villager.setCustomName(Util.color(name.toString()));
            ClickListener.villagers.put(villager.getUniqueId(),
                    new CstmVillager(villager.getUniqueId(), name.toString()));
            Main.getInstance().getConfig().set("Villagers." + villager.getUniqueId() + ".name", name.toString());
            Main.getInstance().saveConfig();
            p.sendMessage(
                    Component.text(Main.helper().getMess(p, "VillerCreated", true).replace("%name", name.toString())));
            return true;
        }

        if (!selectedVills.containsKey(p)) {
            p.sendMessage(Component.text(Main.helper().getMess(p, "NoVillSelected", true)));
            return false;
        }

        if (args[0].equalsIgnoreCase("rename")) {
            if (args.length < 2) {
                p.sendMessage(Component.text(Main.helper().getMess(p, "NEArgs", true)));
                return false;
            }
            StringBuilder name = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                name.append(args[i]);
                if (i < args.length - 1) {
                    name.append(" ");
                }
            }
            Entity vill = selectedVills.get(p);
            vill.setCustomName(Util.color(name.toString()));
            Main.getInstance().getConfig().set("Villagers." + vill.getUniqueId() + ".name", name.toString());
            Main.getInstance().saveConfig();
            p.sendMessage(Component
                    .text(Main.helper().getMess(p, "VillagerRenamed", true).replace("%name", name.toString())));
            return true;
        }

        if (args[0].equalsIgnoreCase("top")) {
            CstmVillager vill = ClickListener.villagers.get(selectedVills.get(p).getUniqueId());
            List<Pair<Trade, Integer>> villagerSells = new ArrayList<>();
            List<Pair<Trade, Integer>> villagerBuys = new ArrayList<>();

            for (Trade trade : vill.getSells()) {
                String key = "Villagers." + vill.getUuid().toString() + ".Trades."
                        + trade.getType().toString().toLowerCase() + "." + trade.getUuid().toString() + ".usedSoFar";
                int amount = 0;
                if (Main.getInstance().getConfig().get(key) != null) {
                    amount = Main.getInstance().getConfig().getInt(key);
                }
                villagerSells.add(new Pair<Trade, Integer>(trade, amount));
            }
            for (Trade trade : vill.getBuys()) {
                String key = "Villagers." + vill.getUuid().toString() + ".Trades."
                        + trade.getType().toString().toLowerCase() + "." + trade.getUuid().toString() + ".usedSoFar";
                int amount = 0;
                if (Main.getInstance().getConfig().get(key) != null) {
                    amount = Main.getInstance().getConfig().getInt(key);
                }
                villagerBuys.add(new Pair<Trade, Integer>(trade, amount));
            }

            Collections.sort(villagerSells, new Comparator<Pair<Trade, Integer>>() {
                public int compare(Pair<Trade, Integer> o1, Pair<Trade, Integer> o2) {
                    return o2.second - o1.second;
                }
            });
            Collections.sort(villagerBuys, new Comparator<Pair<Trade, Integer>>() {
                public int compare(Pair<Trade, Integer> o1, Pair<Trade, Integer> o2) {
                    return o2.second - o1.second;
                }
            });

            StringBuilder builder = new StringBuilder(Main.helper().getMess(p, "VillTradesTopTitle", true)
                    .replace("%VillName", Util.color(vill.getName())));
            int amount = villagerSells.size() >= 10 ? 10 : villagerSells.size();
            builder.append("\n").append(Main.helper().getMess(p, "VillTradesTopCategoryTitle")
                    .replace("%Category", TradeType.VILLAGER_SELLS.toString().toLowerCase()));

            for (int i = 0; i < amount; i++) {
                builder.append("\n").append(Main.helper().getMess(p, "VillTradesTopEntry")
                        .replace("%itemName", villagerSells.get(i).first.getItem().getType().toString())
                        .replace("%pixel", villagerSells.get(i).first.getMoney() + "")
                        .replace("%amount", villagerSells.get(i).second + ""));
            }
            builder.append("\n");
            amount = villagerBuys.size() >= 10 ? 10 : villagerBuys.size();
            builder.append("\n").append(Main.helper().getMess(p, "VillTradesTopCategoryTitle")
                    .replace("%Category", TradeType.VILLAGER_BUYS.toString().toLowerCase()));
            for (int i = 0; i < amount; i++) {
                builder.append("\n").append(Main.helper().getMess(p, "VillTradesTopEntry")
                        .replace("%itemName", villagerBuys.get(i).first.getItem().getType().toString())
                        .replace("%pixel", villagerBuys.get(i).first.getMoney() + "")
                        .replace("%amount", villagerBuys.get(i).second + ""));
            }
            p.sendMessage(Component.text(builder.toString()));
            return true;
        }

        if (args[0].equalsIgnoreCase("tphere")) {
            Entity vill = selectedVills.get(p);
            vill.teleport(p);
            p.sendMessage(Component
                    .text(Main.helper().getMess(p, "VillagerTeleported", true).replace("%name", vill.getCustomName())));
            return true;
        }
        p.sendMessage(Component.text(Main.helper().getMess(p, "NotAValidSubCommand", true)));
        return true;
    }

    public static void setSelectedVill(Player p, Entity e) {
        selectedVills.remove(p);
        selectedVills.put(p, e);
    }

    public static void removeVill(Entity e) {
        for (Player p : selectedVills.keySet()) {
            if (selectedVills.get(p).equals(e))
                selectedVills.remove(p);
        }
        Main.getInstance().getConfig().set("Villagers." + e.getUniqueId(), null);
        Main.getInstance().saveConfig();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender arg0, @NotNull Command arg1,
            @NotNull String arg2, @NotNull String[] arg3) {
        if (arg3.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("create", "rename", "tphere", "top"));
            return list;
        }
        return Collections.emptyList();
    }
}
