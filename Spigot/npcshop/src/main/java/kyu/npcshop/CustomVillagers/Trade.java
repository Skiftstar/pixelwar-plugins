package kyu.npcshop.CustomVillagers;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class Trade {
    private TradeType type;
    private double money;
    private ItemStack item;
    private UUID uuid;

    public Trade(TradeType type, ItemStack item, double money) {
        this.type = type;
        this.money = money;
        this.item = item;
    }

    public TradeType getType() {
        return type;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getMoney() {
        return money;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

}
