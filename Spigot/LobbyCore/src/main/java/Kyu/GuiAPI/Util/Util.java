package Kyu.GuiAPI.Util;

import org.bukkit.ChatColor;

public class Util {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
