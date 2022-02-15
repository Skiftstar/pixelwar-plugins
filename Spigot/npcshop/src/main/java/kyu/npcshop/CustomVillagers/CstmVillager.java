package kyu.npcshop.CustomVillagers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import kyu.npcshop.Main;

public class CstmVillager {
    private List<Trade> buys = new ArrayList<>();
    private List<Trade> sells = new ArrayList<>();
    private UUID uuid;


    public CstmVillager(UUID uuid) {
        this.uuid = uuid;
        loadTrades();
    }

    private void loadTrades() {
        YamlConfiguration config = Main.getInstance().getConfig();
        if (config.get("Villagers." + uuid.toString() + ".Trades.buys") != null) {
            //Load Item
            //Load Price
        }

        if (config.get("Villagers." + uuid.toString() + ".Trades.sells") != null) {
            //Load Item
            //Load Price
        }
    }

    public List<Trade> getBuys() {
        return buys;
    }

    public List<Trade> getSells() {
        return sells;
    }
    
}
