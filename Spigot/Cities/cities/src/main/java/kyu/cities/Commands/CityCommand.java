package kyu.cities.Commands;

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
            if (e.args()[0].equalsIgnoreCase("info") && e.args().length == 1) {
                //TODO: Show info for players City
                return;
            }

            if (e.args().length < 2) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "NEArgs", true)));
                return;
            }


            if (e.args()[0].equalsIgnoreCase("create")) {
                CPlayer p = CPlayer.players.get(e.player());
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
