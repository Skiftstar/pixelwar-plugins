package kyu.cities.Commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import Kyu.SCommand;
import kyu.cities.Main;
import kyu.cities.Util.CPlayer;
import kyu.cities.Util.City;
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

            if (e.args()[0].equalsIgnoreCase("info") && e.args().length == 1) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMDExtended", true));
                    return;
                }
                City city = p.getCity();
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                String mayorName = Bukkit.getOfflinePlayer(UUID.fromString(cityConfig.getString(city.getName().toLowerCase() + ".mayor"))).getName();
                StringBuilder cityCouncilNames = new StringBuilder();
                int councilCount = 0;
                StringBuilder fullMemberNames = new StringBuilder();
                int fullMemberCount = 0;
                StringBuilder newMemberNames = new StringBuilder();
                int newMemberCount = 0;
                for (String uuid : cityConfig.getStringList(city.getName().toLowerCase() + ".cityCouncil")) {
                    cityCouncilNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
                    councilCount++;
                }
                for (String uuid : cityConfig.getStringList(city.getName().toLowerCase() + ".fullMembers")) {
                    fullMemberNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
                    fullMemberCount++;
                }
                for (String uuid : cityConfig.getStringList(city.getName().toLowerCase() + ".newMembers")) {
                    newMemberNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
                    newMemberCount++;
                }
                //Remove the ", " at the end of the Strings
                cityCouncilNames = new StringBuilder(cityCouncilNames.substring(0, cityCouncilNames.length() - 3));
                fullMemberNames = new StringBuilder(fullMemberNames.substring(0, cityCouncilNames.length() - 3));
                newMemberNames = new StringBuilder(newMemberNames.substring(0, cityCouncilNames.length() - 3));
                int level = city.getLevel();
                p.sendMessage(Main.helper.getMess(e.player(), "CityInfo", true)
                    .replace("%name", city.getName())
                    .replace("%level", "" + level)
                    .replace("%entryReq", Main.helper.getMess(e.player(), city.getEntryRequirement().toString()))
                    .replace("%mayor", mayorName)
                    .replace("%cityCouncil", cityCouncilNames.toString())
                    .replace("%councilCount", councilCount + "")
                    .replace("%fullMembers", fullMemberNames.toString())
                    .replace("%fullCount", fullMemberCount + "")
                    .replace("%newMembers", newMemberNames.toString())
                    .replace("%newCount", newMemberCount + ""));
                return;
            }

            if (e.args().length < 2) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "NEArgs", true)));
                return;
            }


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
        });
    }

}
