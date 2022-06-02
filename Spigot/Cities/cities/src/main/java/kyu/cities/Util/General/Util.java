package kyu.cities.Util.General;

import net.md_5.bungee.api.ChatColor;

public class Util {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static double round(double in, RoundingType rounding) {
        switch(rounding) {
            case UP:
                return Math.ceil(in);
            case DOWN:
                return Math.floor(in);
            case HALF:
                return Math.round(in);
            case NONE:
                return in;
            default:
                return in;
        }
    }
    
}
