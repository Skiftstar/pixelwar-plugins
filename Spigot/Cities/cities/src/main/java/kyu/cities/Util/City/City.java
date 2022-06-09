package kyu.cities.Util.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;

import kyu.cities.Main;
import kyu.cities.Util.General.EXPCurveType;
import kyu.cities.Util.Player.CPlayer;

public class City {

    public static Map<String, City> cities = new HashMap<>();
    public static EXPCurveType expCurveType;
    public static double base, exponent, multiplier;
    public static int defaultClaimableChunks;
    public static int levelsPerNewChunk;
    public static int cost;

    private double exp;
    private String name;
    private EntryRequirement entryRequirement;
    private List<CPlayer> onlinePlayers = new ArrayList<>();
    private List<Chunk> claimedChunks = new ArrayList<>();
    private CityRank minClaimRank;
    private CityRank minEditRank;
    private int claimAbleChunks;
    private boolean canNewcommersBreakPlace;
    private boolean pvpEnabled;

    public City(String name) {
        this.name = name;
        load();
        cities.put(name.toLowerCase(), this);
    }

    public static void initNew(UUID mayorUUID, String cityName) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        cityConfig.set(cityName.toLowerCase() + ".caseSensitiveName", cityName);
        cityConfig.set(cityName.toLowerCase() + ".exp", 0);
        cityConfig.set(cityName.toLowerCase() + ".claimableChunks", City.defaultClaimableChunks);
        cityConfig.set(cityName.toLowerCase() + ".canNewcommersBreakPlace", false);
        cityConfig.set(cityName.toLowerCase() + ".mayor", mayorUUID.toString());
        cityConfig.set(cityName.toLowerCase() + ".entryReq", EntryRequirement.NONE.toString());
        cityConfig.set(cityName.toLowerCase() + ".pvpEnabled", false);
        cityConfig.set(cityName.toLowerCase() + ".minClaimRank", CityRank.CITY_COUNCIL.toString());
        cityConfig.set(cityName.toLowerCase() + ".minEditRank", CityRank.MAYOR.toString());
        Main.saveConfig(cityConfig);
    }

    private void load() {
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        name = cityConf.getString(name.toLowerCase() + ".caseSensitiveName");
        exp = cityConf.getDouble(name.toLowerCase() + ".exp");
        claimAbleChunks = cityConf.getInt(name.toLowerCase() + ".claimableChunks");
        entryRequirement = EntryRequirement.valueOf(cityConf.getString(name.toLowerCase() + ".entryReq"));
        canNewcommersBreakPlace = cityConf.getBoolean(name.toLowerCase() + ".canNewcommersBreakPlace");
        pvpEnabled = cityConf.getBoolean(name.toLowerCase() + ".pvpEnabled");
        minClaimRank = CityRank.valueOf(cityConf.getString(name.toLowerCase() + ".minClaimRank"));
        minEditRank = CityRank.valueOf(cityConf.getString(name.toLowerCase() + ".minEditRank"));
    }

    public void addExp(double exp) {
        int levelBefore = getLevel();

        this.exp += exp;
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        cityConf.set(name.toLowerCase() + ".exp", this.exp);
        Main.saveConfig(cityConf);

        int levelAfter = getLevel();

        if (levelAfter > levelBefore) {
            handleLevelup();
        }
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
        if (!invites.contains(uuid.toString()))
            invites.add(uuid.toString());
        cityConfig.set(name.toLowerCase() + ".invites", invites);
        Main.saveConfig(cityConfig);
    }

    public boolean hasInviteFor(UUID uuid) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        List<String> invites = new ArrayList<>();
        if (cityConfig.get(name.toLowerCase() + ".invites") != null) {
            invites = cityConfig.getStringList(name.toLowerCase() + ".invites");
        }
        return invites.contains(uuid.toString());
    }

    public void removeInvite(UUID uuid) {
        City.removeInvite(uuid, name);
    }

    public void removePlayer(CPlayer p) {
        removePlayer(p.getPlayer().getUniqueId());
        onlinePlayers.remove(p);
    }

    public void removePlayer(UUID uuid) {
        String key;
        CityRank rank = CPlayer.getCityRank(uuid);
        if (rank.equals(CityRank.NEW_MEMBER))
            key = name.toLowerCase() + ".newMembers";
        else if (rank.equals(CityRank.FULL_MEMBER))
            key = name.toLowerCase() + ".fullMembers";
        else if (rank.equals(CityRank.CITY_COUNCIL))
            key = name.toLowerCase() + ".cityCouncil";
        else
            return;
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        List<String> members = cityConfig.getStringList(key);
        members.remove(uuid.toString());
        cityConfig.set(key, members);
        Main.saveConfig(cityConfig);
    }

    public void displayJoinRequests(CPlayer p, boolean showEmptyMessage) {
        List<String> requests = p.getCity().getJoinRequestNames();
        if (requests.size() == 0) {
            if (showEmptyMessage) {
                p.sendMessage(Main.helper.getMess(p.getPlayer(), "NoOpenJoinRequests", true));
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(Main.helper.getMess(p.getPlayer(), "OpenJoinRequestsHeader", true));
        for (String s : requests) {
            sb.append(Main.helper.getMess(p.getPlayer(), "OpenJoinRequestsEntry", false)
                .replace("%name", s));
        }
        p.sendMessage(sb.toString());
    }

    public List<String> getJoinRequestNames() {
        List<String> list = new ArrayList<>();
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        if (cityConfig.get(name.toLowerCase() + ".joinRequests") == null) {
            return list;
        }

        YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();
        for (String s : cityConfig.getStringList(name.toLowerCase() + ".joinRequests")) {
            if (nameMapper.get(s) != null) {
                list.add(nameMapper.getString(s));
            }
        }
        return list;
    }

    public void delete() {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        cityConfig.set(name.toLowerCase(), null);
        Main.saveConfig(cityConfig);
        cities.remove(name.toLowerCase());

        for (UUID uuid : City.getAllPlayers(getName())) {
            System.out.println(uuid.toString());
            CPlayer.removeCity(uuid);
        }
    }

    public void claimChunk(Chunk chunk) {
        claimedChunks.add(chunk);
        claimAbleChunks--;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return City.getLevel(this.exp);
    }

    public static int getLevel(double exp) {
        if (expCurveType.equals(EXPCurveType.EXPONENTIAL)) {
            return (int) Math.floor(((Math.log(exp) / Math.log(base)) - exponent) / multiplier);
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

    public CityRank getMinEditRank() {
        return minEditRank;
    }

    public void addOnlinePlayer(CPlayer p) {
        if (!onlinePlayers.contains(p))
            onlinePlayers.add(p);
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

    public void setOption(CityOption option, String value) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        switch(option) {
            case PVP:
                pvpEnabled = Boolean.parseBoolean(value);
                cityConfig.set(getName().toLowerCase() + ".pvpEnabled", pvpEnabled);
                break;
            case CAN_NEWCOMMERS_BREAK_BLOCKS:
                canNewcommersBreakPlace = Boolean.parseBoolean(value);
                cityConfig.set(getName().toLowerCase() + ".canNewcommersBreakPlace", canNewcommersBreakPlace);
                break;
            case JOIN_REQUIREMENT:
                entryRequirement = EntryRequirement.valueOf(value);
                cityConfig.set(getName().toLowerCase() + ".entryRequirement", entryRequirement.toString());
                break;
            case MIN_CLAIM_RANK:
                minClaimRank = CityRank.valueOf(value);
                cityConfig.set(getName().toLowerCase() + ".minClaimRank", minClaimRank.toString());
                break;
            case CAN_CITY_COUNCIL_EDIT_OPTIONS:
                minEditRank = CityRank.valueOf(value);
                cityConfig.set(getName().toLowerCase() + ".minEditRank", minEditRank.toString());
                break;
        }
        Main.saveConfig(cityConfig);
    }

    private void handleLevelup() {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        int newLevel = getLevel();
        for (CPlayer player : onlinePlayers) {
            player.sendMessage(Main.helper.getMess(player.getPlayer(), "CityLevelUp", true)
                .replace("%newLevel", "" + newLevel));
        }

        if (newLevel % levelsPerNewChunk == 0) {
            claimAbleChunks++;
            cityConfig.set(getName().toLowerCase() + ".claimAbleChunks", claimAbleChunks);
        }

        Main.saveConfig(cityConfig);

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
        p.setRank(CityRank.NEW_MEMBER);
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

    public static void requestJoin(CPlayer p, String cityName) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        List<String> joinRequests = new ArrayList<>();
        if (cityConfig.get(cityName.toLowerCase() + ".joinRequests") != null) {
            joinRequests = cityConfig.getStringList(cityName.toLowerCase() + ".joinRequests");
        }
        if (joinRequests.contains(p.getPlayer().getName().toLowerCase())) {
            p.sendMessage(Main.helper.getMess(p.getPlayer(), "AlreadyRequestedJoin", true));
            return;
        }
        if (cities.containsKey(cityName.toLowerCase())) {
            City city = cities.get(cityName.toLowerCase());
            for (CPlayer player : city.onlinePlayers) {
                if (player.getRank().equals(CityRank.MAYOR) || player.getRank().equals(CityRank.CITY_COUNCIL)) {
                    player.sendMessage(Main.helper.getMess(player.getPlayer(), "NewJoinRequest", true)
                            .replace("%name", p.getPlayer().getName()));
                }
            }
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

    public static List<UUID> getAllPlayers(String cityName) {
        List<UUID> players = new ArrayList<>();
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();

        if (cityConfig.get(cityName.toLowerCase() + ".newMembers") != null) {
            for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".newMembers")) {
                players.add(UUID.fromString(uuid));
            }
        }
        if (cityConfig.get(cityName.toLowerCase() + ".fullMembers") != null) {
            for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".fullMembers")) {
                players.add(UUID.fromString(uuid));
            }
        }
        if (cityConfig.get(cityName.toLowerCase() + ".cityCouncil") != null) {
            for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".cityCouncil")) {
                players.add(UUID.fromString(uuid));
            }
        }
        if (cityConfig.get(cityName.toLowerCase() + ".mayor") != null) {
            players.add(UUID.fromString(cityConfig.getString(cityName.toLowerCase() + ".mayor")));
        }
        return players;
    }

}
