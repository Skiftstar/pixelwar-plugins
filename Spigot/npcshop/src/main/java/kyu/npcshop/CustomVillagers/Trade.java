package kyu.npcshop.CustomVillagers;

import net.minecraft.world.item.ItemStack;

public class Trade {
    private TradeType type;
    private double money;
    private ItemStack item;

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

}
