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
        if (config.get("Villagers." + uuid.toString() + ".Trades.buy") != null) {
            for (String tradeUUID : config.getConfigurationSection("Villagers." + uuid.toString() + ".Trades.buy")
                    .getKeys(false)) {
                String key = "Villagers." + uuid.toString() + ".Trades.buy." + tradeUUID;
                double price = config.getDouble(key + ".money");
                ItemStack item = itemFromConf(config, key + ".item");
                Trade trade = new Trade(TradeType.BUY, item, price);
                sells.add(trade);
            }
        }

        if (config.get("Villagers." + uuid.toString() + ".Trades.sell") != null) {
            for (String tradeUUID : config.getConfigurationSection("Villagers." + uuid.toString() + ".Trades.sell")
                    .getKeys(false)) {
                String key = "Villagers." + uuid.toString() + ".Trades.sell." + tradeUUID;
                double price = config.getDouble(key + ".money");
                ItemStack item = itemFromConf(config, key + ".item");
                Trade trade = new Trade(TradeType.SELL, item, price);
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
        if (config.get(key + ".enchatnts") != null) {
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
                config.set(key + "item.enchants." + ench.toString(), item.getItemMeta().getEnchants().get(ench));
            }
        }
        Main.getInstance().saveConfig();

        if (trade.getType().equals(TradeType.BUY)) {
            sells.add(trade);
        } else {
            buys.add(trade);
        }

    }

}
