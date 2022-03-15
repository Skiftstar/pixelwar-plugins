package kyu.cities.Commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import Kyu.SCommand;
import kyu.cities.Main;
import kyu.cities.Util.CPlayer;
import kyu.cities.Util.City;
import kyu.cities.Util.CityRank;
import kyu.cities.Util.EntryRequirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class CityCommand {
    
    public static void init() {
        SCommand cityCmd = new SCommand(Main.getInstance(), "city", Main.helper);
        cityCmd.playerOnly(true);
        cityCmd.minArgs(1);
        cityCmd.execPerm("cities.city");
        cityCmd.exec(e -> {
            CPlayer p = CPlayer.players.get(e.player());

            //#region info command without args
            if (e.args()[0].equalsIgnoreCase("info") && e.args().length == 1) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMDExtended", true));
                    return;
                }
                City city = p.getCity();
                sendInfoCommand(p, city.getName());
                return;
            }
            //#endregion info command without args

            if (e.args().length < 2) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "NEArgs", true)));
                return;
            }

            //#region JoinRequests
            if (e.args()[0].equalsIgnoreCase("allow") || e.args()[0].equalsIgnoreCase("deny")) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }
                String playerName = e.args()[1];
                YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
                City city = p.getCity();

                if (!(p.getRank().equals(CityRank.MAYOR) || p.getRank().equals(CityRank.CITY_COUNCIL))) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankNotHighEnough", true));
                    return;
                }

                if (cityConf.get(city.getName().toLowerCase() + ".joinRequests") == null || !cityConf.getStringList(city.getName().toLowerCase() + ".joinRequests").contains(playerName.toLowerCase())) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NoJoinRequestFromPlayer", true));
                    return;
                }

                city.removeJoinRequest(playerName);

                YamlConfiguration playerConf = Main.getInstance().getPlayersConfig();    

                if (e.args()[0].equalsIgnoreCase("allow")) {
                    YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();
                    if (nameMapper.get(playerName.toLowerCase()) == null) {
                        p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotFoundInMapper", true));
                        return;
                    }
                    //TODO: Check if player is already in a city, if yes, erorr
                    //TODO: Add player to City and send messages

                } else {
                    Map<String, String> replaceValues = new HashMap<>();
                    replaceValues.put("%name", city.getName());
                    CPlayer.sendOfflineMess(playerName, "joinRequestDenied", replaceValues);
                    p.sendMessage(Main.helper.getMess(e.player(), "joinRequestDenied", true)
                        .replace("%name", playerName));
                }
                return;
            }
            //#endregion JoinRequests

            //#region join command
            if (e.args()[0].equals("join")) {
                String cityName = e.args()[1];
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                if (cityConfig.get(cityName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityDoesNotExist", true));
                    return;
                }
                EntryRequirement req = EntryRequirement.valueOf(cityConfig.getString(cityName.toLowerCase() + ".entryReq"));
                switch (req) {
                    case INVITE_ONLY:
                        p.sendMessage(Main.helper.getMess(e.player(), "YouMustBeInvited", true));
                        break;
                    case NONE:
                        City.joinCity(p, cityName);
                        break;
                    case REQUEST:
                        //TODO: Join Requests
                        City.requestJoin(p, cityName);
                        break;
                }
            }
            //#endregion join command

            //#region info command with args
            if (e.args()[0].equals("info")) {
                String cityName = e.args()[1];
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                if (cityConfig.get(cityName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityDoesNotExist", true));
                    return;
                }
                sendInfoCommand(p, cityName);
                return;
            }
            //#endregion info commadn with args

            //#region create command
            if (e.args()[0].equalsIgnoreCase("create")) {
                if (p.getCity() != null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "AlreadyInACity", true));
                    return;
                }

                String cityName = e.args()[1];
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                if (cityConfig.get(cityName.toLowerCase()) != null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityNameTaken", true));
                    return;
                }
                cityConfig.set(cityName.toLowerCase() + ".caseSensitiveName", cityName);
                cityConfig.set(cityName.toLowerCase() + ".exp", 0);
                cityConfig.set(cityName.toLowerCase() + ".mayor", ((TextComponent) e.player().displayName()).content());
                Main.saveConfig(cityConfig);
                City city = new City(cityName);
                p.setCity(city);
                p.sendMessage(Main.helper.getMess(e.player(), "CityCreated", true).replace("%name", cityName));
                return;
            }
            //#endregion create command
        });
    }

    private static void sendInfoCommand(CPlayer p, String cityName) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                String mayorName = Bukkit.getOfflinePlayer(UUID.fromString(cityConfig.getString(cityName.toLowerCase() + ".mayor"))).getName();
                StringBuilder cityCouncilNames = new StringBuilder();
                int councilCount = 0;
                StringBuilder fullMemberNames = new StringBuilder();
                int fullMemberCount = 0;
                StringBuilder newMemberNames = new StringBuilder();
                int newMemberCount = 0;
                for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".cityCouncil")) {
                    cityCouncilNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
                    councilCount++;
                }
                for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".fullMembers")) {
                    fullMemberNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
                    fullMemberCount++;
                }
                for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".newMembers")) {
                    newMemberNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
                    newMemberCount++;
                }
                //Remove the ", " at the end of the Strings
                cityCouncilNames = new StringBuilder(cityCouncilNames.substring(0, cityCouncilNames.length() - 3));
                fullMemberNames = new StringBuilder(fullMemberNames.substring(0, cityCouncilNames.length() - 3));
                newMemberNames = new StringBuilder(newMemberNames.substring(0, cityCouncilNames.length() - 3));
                int level = City.getLevel(cityConfig.getDouble(cityName.toLowerCase() + ".exp"));
                p.sendMessage(Main.helper.getMess(p.getPlayer(), "CityInfo", true)
                    .replace("%name", cityConfig.getString(cityName.toLowerCase() + ".caseSensitiveName"))
                    .replace("%level", "" + level)
                    .replace("%entryReq", Main.helper.getMess(p.getPlayer(), cityConfig.getString(cityName.toLowerCase() + ".entryReq")))
                    .replace("%mayor", mayorName)
                    .replace("%cityCouncil", cityCouncilNames.toString())
                    .replace("%councilCount", councilCount + "")
                    .replace("%fullMembers", fullMemberNames.toString())
                    .replace("%fullCount", fullMemberCount + "")
                    .replace("%newMembers", newMemberNames.toString())
                    .replace("%newCount", newMemberCount + ""));
    }

}
