package kyu.cities.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;

import kyu.cities.Main;

public class City {

    public static Map<String, City> cities = new HashMap<>();
    public static EXPCurveType expCurveType;
    public static double base, exponent, multiplier;
    public static int defaultClaimableChunks;

    private double exp;
    private String name;
    private EntryRequirement entryRequirement;
    private List<CPlayer> onlinePlayers = new ArrayList<>();
    private List<Chunk> claimedChunks = new ArrayList<>();
    private CityRank minClaimRank;
    private int claimAbleChunks;
    private boolean canNewcommersBreakPlace;
    private boolean pvpEnabled;
    
    public City(String name) {
        this.name = name;
        load();
        cities.put(name.toLowerCase(), this);
    }

    private void load() {
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        name = cityConf.getString(name.toLowerCase() + ".caseSensitiveName");
        exp = cityConf.getDouble(name.toLowerCase() + ".exp");
        claimAbleChunks = cityConf.getInt(name.toLowerCase() + ".claimableChunks");
        entryRequirement = EntryRequirement.valueOf(cityConf.getString(name.toLowerCase() + ".entryReq"));
        canNewcommersBreakPlace = cityConf.getBoolean(name.toLowerCase() + ".canNewcommersBreakPlace");
        pvpEnabled = cityConf.getBoolean(name.toLowerCase() + ".pvpEnabled");
    }

    public void removeJoinRequest(String playerName) {
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        List<String> joinRequests = cityConf.getStringList(this.getName().toLowerCase() + ".joinRequests");
        joinRequests.remove(playerName.toLowerCase());
        cityConf.set(this.getName().toLowerCase() + ".joinRequests", joinRequests);
        Main.saveConfig(cityConf);
    }

    public void addInvite(UUID uuid) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        List<String> invites = new ArrayList<>();
        if (cityConfig.get(name.toLowerCase() + ".invites") != null) {
            invites = cityConfig.getStringList(name.toLowerCase() + ".invites");
        }
        invites.add(uuid.toString());
        cityConfig.set(name.toLowerCase() + ".invites", invites);
        Main.saveConfig(cityConfig);
    }

    public void removeInvite(UUID uuid) {
        City.removeInvite(uuid, name);
    }

    public static void removeInvite(UUID uuid, String name) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        if (cityConfig.get(name.toLowerCase() + ".invites") == null) {
            return;
        }
        List<String> invites = cityConfig.getStringList(name.toLowerCase() + ".invites");
        invites.remove(uuid.toString());
        cityConfig.set(name.toLowerCase() + ".invites", invites);
        Main.saveConfig(cityConfig);
    }

    public void claimChunk(Chunk chunk) {
        claimedChunks.add(chunk);
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return City.getLevel(this.exp);
    }

    public static int getLevel(double exp) {
        if (expCurveType.equals(EXPCurveType.EXPONENTIAL)) {
            return (int) Math.floor(((Math.log(exp)/Math.log(base)) - exponent) / multiplier);
        } else {
            return (int) Math.floor(exp / base);
        }
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public void setCanNewcommersBreakPlace(boolean canNewcommersBreakPlace) {
        this.canNewcommersBreakPlace = canNewcommersBreakPlace;
    }

    public void setEntryRequirement(EntryRequirement entryRequirement) {
        this.entryRequirement = entryRequirement;
    }

    public EntryRequirement getEntryRequirement() {
        return entryRequirement;
    }

    public List<CPlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public boolean canNewcommersBreakPlace() {
        return canNewcommersBreakPlace;
    }

    public void addOnlinePlayer(CPlayer p) {
        if (!onlinePlayers.contains(p)) onlinePlayers.add(p);
    }

    public void removeOnlinePlayer(CPlayer p) {
        onlinePlayers.remove(p);
    }

    public boolean canClaimChunks() {
        return claimAbleChunks > 0;
    }

    public CityRank getMinClaimRank() {
        return minClaimRank;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public List<Chunk> getClaimedChunks() {
        return claimedChunks;
    }


    public static boolean exists(String name) {
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        return cityConf.get(name.toLowerCase()) != null;
    }

    public static void joinCity(CPlayer p, String cityName) {
        City city;
        if (!cities.containsKey(cityName) && exists(cityName)) {
            city = new City(cityName);
        }
        city = getCity(cityName);
        p.setCity(city);
        city.onlinePlayers.add(p);

        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        List<String> newMembers = cityConf.getStringList(cityName.toLowerCase() + ".newMembers");
        newMembers.add(p.getPlayer().getUniqueId().toString());
        cityConf.set(cityName.toLowerCase() + ".newMembers", newMembers);
        Main.saveConfig(cityConf);

        YamlConfiguration playerConf = Main.getInstance().getPlayersConfig();
        playerConf.set(p.getPlayer().getUniqueId().toString() + ".city", cityName.toLowerCase());
        Main.saveConfig(playerConf);
    }

    public static void requestJoin(CPlayer p, String cityName) { 
        if (cities.containsKey(cityName.toLowerCase())) {
            City city = cities.get(cityName.toLowerCase());
            for (CPlayer player : city.onlinePlayers) {
                if (player.getRank().equals(CityRank.MAYOR) || player.getRank().equals(CityRank.CITY_COUNCIL)) {
                    player.sendMessage(Main.helper.getMess(player.getPlayer(), "NewJoinRequest", true)
                        .replace("%name", p.getPlayer().getName()));
                }
            }
        }
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        List<String> joinRequests = new ArrayList<>();
        if (cityConfig.get(cityName.toLowerCase() + ".joinRequests") != null) {
            joinRequests = cityConfig.getStringList(cityName.toLowerCase() + ".joinRequests");
        }
        if (joinRequests.contains(p.getPlayer().getName().toLowerCase())) {
            p.sendMessage(Main.helper.getMess(p.getPlayer(), "AlreadyRequestedJoin", true));
            return;
        }
        joinRequests.add(p.getPlayer().getName().toLowerCase());
        cityConfig.set(cityName.toLowerCase() + ".joinRequests", joinRequests);
        Main.saveConfig(cityConfig);
    }

    public static City getCity(String cityName) {
        City city;
        if (!cities.containsKey(cityName.toLowerCase())) {
            city = new City(cityName);
        } else {
            city = cities.get(cityName.toLowerCase());
        }
        return city;
    }

    public static City isChunkOwned(Chunk chunk) {
        for (City city : cities.values()) {
            if (city.getClaimedChunks().contains(chunk)) {
                return city;
            }
        }
        return null;
    }
    
}
