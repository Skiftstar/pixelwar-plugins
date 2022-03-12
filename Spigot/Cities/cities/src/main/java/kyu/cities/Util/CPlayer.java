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
            if (!City.cities.containsKey(cityName)) {
                if (!City.exists(cityName)) {
                    pConf.set(p.getUniqueId().toString() + ".city", null);
                    Main.saveConfig(pConf);
                    return;
                }
                city = new City(cityName);
            } else {
                city = City.cities.get(cityName);
            }
        }
    }

    public void setCity(City city) {
        this.city = city;
        YamlConfiguration config = Main.getInstance().getPlayersConfig();
        config.set(p.getUniqueId().toString() + ".city", city.getName());
        Main.saveConfig(config);
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


}
