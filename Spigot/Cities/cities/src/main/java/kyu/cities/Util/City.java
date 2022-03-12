package kyu.cities.Util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import kyu.cities.Main;

public class City {

    public static Map<String, City> cities = new HashMap<>();
    public static EXPCurveType expCurveType;
    public static double base, exponent, multiplier;

    private double exp;
    private String name;
    
    public City(String name) {
        this.name = name;
        load();
        cities.put(name, this);
    }

    private void load() {
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        exp = cityConf.getDouble(name.toLowerCase() + ".exp");
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        if (expCurveType.equals(EXPCurveType.EXPONENTIAL)) {
            return (int) Math.floor(((Math.log(exp)/Math.log(base)) - exponent) / multiplier);
        } else {
            return (int) Math.floor(exp / base);
        }
    }


    public static boolean exists(String name) {
        YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
        return cityConf.get(name.toLowerCase()) != null;
    }
    
}
