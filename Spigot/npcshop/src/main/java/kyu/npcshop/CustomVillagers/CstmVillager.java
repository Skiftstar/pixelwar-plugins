package kyu.npcshop.CustomVillagers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kyu.npcshop.Main;
import kyu.npcshop.Util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class CstmVillager {
    private List<Trade> buys = new ArrayList<>();
    private List<Trade> sells = new ArrayList<>();
    private UUID uuid;
    private String name;

    public CstmVillager(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        loadTrades();
    }

    private void loadTrades() {
        YamlConfiguration config = Main.getInstance().getConfig();
        if (config.get("Villagers." + uuid.toString() + ".Trades.villager_sells") != null) {
            for (String tradeUUID : config.getConfigurationSection("Villagers." + uuid.toString() + ".Trades.villager_sells")
                    .getKeys(false)) {
                String key = "Villagers." + uuid.toString() + ".Trades.villager_sells." + tradeUUID;
                double price = config.getDouble(key + ".money");
                ItemStack item = itemFromConf(config, key + ".item");
                Trade trade = new Trade(TradeType.VILLAGER_SELLS, item, price);
                trade.setUUID(UUID.fromString(tradeUUID));
                sells.add(trade);
            }
        }

        if (config.get("Villagers." + uuid.toString() + ".Trades.villager_buys") != null) {
            for (String tradeUUID : config.getConfigurationSection("Villagers." + uuid.toString() + ".Trades.villager_buys")
                    .getKeys(false)) {
                String key = "Villagers." + uuid.toString() + ".Trades.villager_buys." + tradeUUID;
                double price = config.getDouble(key + ".money");
                ItemStack item = itemFromConf(config, key + ".item");
                Trade trade = new Trade(TradeType.VILLAGER_BUYS, item, price);
                trade.setUUID(UUID.fromString(tradeUUID));
                buys.add(trade);
            }
        }

    }

    public List<Trade> getBuys() {
        return buys;
    }

    public List<Trade> getSells() {
        return sells;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("deprecation")
    private ItemStack itemFromConf(YamlConfiguration config, String key) {
        Material mat = Material.getMaterial(config.getString(key + ".type"));
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        if (config.get(key + ".enchants") != null) {
            for (String enchant : config.getConfigurationSection(key + ".enchants").getKeys(false)) {
                Enchantment ench = Enchantment.getByName(enchant);
                int level = config.getInt(key + ".enchants." + enchant);
                im.addEnchant(ench, level, true);
            }
        }
        if (config.get(key + ".name") != null) {
            im.displayName(Component.text(Util.color(config.getString(key + ".name"))));
        }
        if (config.get(key + ".lore") != null) {
            im.setLore(config.getStringList(key + ".lore"));
        }
        is.setItemMeta(im);
        return is;
    }

    @SuppressWarnings("deprecation")
    public void addTrade(Trade trade) {
        UUID uuid;
        YamlConfiguration config = Main.getInstance().getConfig();
        do {
            uuid = UUID.randomUUID();
        } while (config.get("Villagers" + this.uuid.toString() + ".Trades." + trade.getType().toString().toLowerCase() + "." + uuid.toString()) != null);
        String key = "Villagers." + this.uuid.toString() + ".Trades." + trade.getType().toString().toLowerCase() + "." + uuid.toString() + ".";

        trade.setUUID(uuid);

        // Save Money
        config.set(key + "money", trade.getMoney());

        // Saving the Item
        ItemStack item = trade.getItem();
        Material mat = item.getType();
        config.set(key + "item.type", mat.toString());
        if (item.getItemMeta().hasDisplayName()) {
            String name = ((TextComponent) item.getItemMeta().displayName()).content();
            config.set(key + "item.name", name);
        }
        if (item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            config.set(key + "item.lore", lore);
        }
        if (item.getItemMeta().hasEnchants()) {
            for (Enchantment ench : item.getItemMeta().getEnchants().keySet()) {
                config.set(key + "item.enchants." + ench.getName().toString(), item.getItemMeta().getEnchants().get(ench));
            }
        }
        Main.getInstance().saveConfig();

        if (trade.getType().equals(TradeType.VILLAGER_BUYS)) {
            buys.add(trade);
        } else {
            sells.add(trade);
        }

    }

    public void removeFromBuys(Trade trade) {
        buys.remove(trade);
        YamlConfiguration config = Main.getInstance().getConfig();
        config.set("Villagers." + uuid.toString() + ".Trades.villager_buys." + trade.getUuid().toString(), null);
        Main.getInstance().saveConfig();
    }

    public void removeFromSells(Trade trade) {
        sells.remove(trade);
        YamlConfiguration config = Main.getInstance().getConfig();
        config.set("Villagers." + uuid.toString() + ".Trades.villager_sells." + trade.getUuid().toString(), null);
        Main.getInstance().saveConfig();
    }

    public void logTrade(Trade trade, int amount) {
        YamlConfiguration config = Main.getInstance().getConfig();
        String key = "Villagers." + uuid.toString() + ".Trades." + trade.getType().toString().toLowerCase() + "." + trade.getUuid().toString() + ".usedSoFar";
        int amountBeforeAdd = 0;
        if (config.get(key) != null) {
            amountBeforeAdd = config.getInt(key);
        }
        int amountAfterAdd = amountBeforeAdd + amount;
        config.set(key, amountAfterAdd);
        Main.getInstance().saveConfig();
    }

    public UUID getUuid() {
        return uuid;
    }

}
