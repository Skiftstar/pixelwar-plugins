package kyu.cities.Util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import kyu.cities.Main;
import net.kyori.adventure.text.Component;

public class CPlayer {

    public static Map<Player, CPlayer> players = new HashMap<>();

    private City city = null;
    private Map<Job, Double> jobs = new HashMap<>();
    private Player p;
    private CityRank rank;
    
    public CPlayer(Player p) {
        this.p = p;
        load();
        players.put(p, this);
    }

    private void load() {
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();

        //TODO: Load Job EXPs

        if (pConf.get(p.getUniqueId().toString() + ".city") != null) {
            String cityName = pConf.getString(p.getUniqueId().toString() + ".city");
            if (!City.cities.containsKey(cityName.toLowerCase())) {
                if (!City.exists(cityName)) {
                    pConf.set(p.getUniqueId().toString() + ".city", null);
                    Main.saveConfig(pConf);
                    return;
                }
                city = new City(cityName);
            } else {
                city = City.cities.get(cityName);
            }
            city.addOnlinePlayer(this);
        }
    }

    public void setCity(City city) {
        this.city = city;
        YamlConfiguration config = Main.getInstance().getPlayersConfig();
        config.set(p.getUniqueId().toString() + ".city", city.getName());
        Main.saveConfig(config);
    }

    public void setRank(CityRank rank) {
        this.rank = rank;
    }

    public void sendMessage(String message) {
        p.sendMessage(Component.text(message));
    }

    public Player getPlayer() {
        return p;
    }

    public Map<Job, Double> getJobs() {
        return jobs;
    }

    public City getCity() {
        return city;
    }

    public CityRank getRank() {
        return rank;
    }

    public static void sendOfflineMess(String playerName, String MessageKey, Map<String, String> replaceValues) {
        //TODO: If player online, send message directly, if not add to Config and send on Next join
        //TODO: Also check if player is in mapper, if not just ignore
    }

}
