package kyu.cities.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
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

        // TODO: Load Job EXPs

        if (pConf.get(p.getUniqueId().toString() + ".city") != null) {
            rank = CityRank.valueOf(pConf.getString(p.getUniqueId().toString() + ".cityRank"));
            String cityName = pConf.getString(p.getUniqueId().toString() + ".city");
            if (!City.cities.containsKey(cityName.toLowerCase())) {
                if (!City.exists(cityName)) {
                    pConf.set(p.getUniqueId().toString() + ".city", null);
                    Main.saveConfig(pConf);
                    return;
                }
                city = new City(cityName);
            } else {
                city = City.cities.get(cityName.toLowerCase());
            }
            city.addOnlinePlayer(this);
        }
    }

    public void handleOfflineMessages() {
        YamlConfiguration playerConf = Main.getInstance().getPlayersConfig();
        if (playerConf.get(p.getUniqueId().toString() + ".offlineMessages") == null) {
            return;
        }
        for (String k : playerConf.getConfigurationSection(p.getUniqueId().toString() + ".offlineMessages").getKeys(false)) {
            String key = p.getUniqueId().toString() + ".offlineMessages." + k;
            String messageKey = playerConf.getString(key + ".messKey");
            boolean prefix = playerConf.getBoolean(key + ".prefix");

            String message = Main.helper.getMess(p, messageKey, prefix);
            for (String rKey : playerConf.getConfigurationSection(key + ".replaces").getKeys(false)) {
                message = message.replace(rKey, playerConf.getString(key + ".replaces." + rKey));
            }

            sendMessage(message);

            playerConf.set(key, null);
        }
        Main.saveConfig(playerConf);
    }

    public void setCity(City city) {
        this.city = city;
        YamlConfiguration config = Main.getInstance().getPlayersConfig();
        config.set(p.getUniqueId().toString() + ".city", city.getName().toLowerCase());
        Main.saveConfig(config);
    }

    public void setRank(CityRank rank) {
        this.rank = rank;
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        pConf.set(p.getUniqueId().toString() + ".cityRank", rank.toString());
        Main.saveConfig(pConf);
    }

    public void leaveCity() {
        if (city == null) {
            return;
        }
        city.removePlayer(this);
        city.removeOnlinePlayer(this);
        city = null;
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        pConf.set(p.getUniqueId().toString() + ".city", null);
        pConf.set(p.getUniqueId().toString() + ".cityRank", null);
        Main.saveConfig(pConf);
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

    public static void sendOfflineMess(String playerName, String messageKey, Map<String, String> replaceValues, boolean prefix) {
        //Ignore if not in mapper -> Cannot send offline message anyways
        YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();
        if (nameMapper.get(playerName.toLowerCase()) == null) {
            return;
        }
        String uuid = nameMapper.getString(playerName.toLowerCase());

        if (isOnline(UUID.fromString(uuid))) {
            CPlayer p = players.get(Bukkit.getPlayer(UUID.fromString(uuid)));
            String message = Main.helper.getMess(p.getPlayer(), messageKey, prefix);
            for (String k : replaceValues.keySet()) {
                message = message.replace(k, replaceValues.get(k));
            }
            p.sendMessage(message);
            return;
        }

        //If not online

        YamlConfiguration playerConf = Main.getInstance().getPlayersConfig();
        int keys = playerConf.get(uuid + ".offlineMessages") == null ? 0 : playerConf.getConfigurationSection(uuid + ".offlineMessages").getKeys(false).size();
        String key = uuid + ".offlineMessages." + keys;
        playerConf.set(key + ".messKey", messageKey);
        playerConf.set(key + ".prefix", prefix);
        for (String k : replaceValues.keySet()) {
            playerConf.set(key + ".replaces." + k, replaceValues.get(k));
        }
        Main.saveConfig(playerConf);
    }

    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public static boolean isInCity(UUID uuid) {
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        return pConf.get(uuid.toString() + ".city") != null;
    }

    public static void setRank(UUID uuid, CityRank rank) {
        if (CPlayer.isOnline(uuid)) {
            players.get(Bukkit.getPlayer(uuid)).setRank(rank);
            return;
        }
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        pConf.set(uuid.toString() + ".cityRank", rank.toString());
        Main.saveConfig(pConf);
    }

    public static CityRank getCityRank(UUID uuid) {
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        return CityRank.valueOf(pConf.getString(uuid.toString() + ".cityRank"));
    }

    public static String getCityName(UUID uuid) {
        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        if (pConf.get(uuid.toString() + ".city") == null) {
            return null;
        }
        return pConf.getString(uuid.toString() + ".city");
    }

    public static void removeCity(UUID uuid) {
        if (isOnline(uuid)) {
            CPlayer p = players.get(Bukkit.getPlayer(uuid));
            p.leaveCity();
            return;
        }

        YamlConfiguration pConf = Main.getInstance().getPlayersConfig();
        pConf.set(uuid.toString() + ".city", null);
        pConf.set(uuid.toString() + ".cityRank", null);
        Main.saveConfig(pConf);
    }

    public static CPlayer getCPlayer(Player p) {
        if (!players.containsKey(p)) {
            return new CPlayer(p);
        }
        return players.get(p);
    }

}
