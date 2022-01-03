package Kyu.ServerCore.Util;

import Kyu.ServerCore.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.sql.Date;

public class Util {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String getColors(String s) {
        //let say str = "§ahello"  which is green color, but the § sign is invisible

        // String formatCode = "";
        String colorCode = "";

        for (String string : s.split("&")) {
            if (string.length() == 0) {
                continue;
            }
            if (string.substring(0, 1).matches("[a-f]") || string.substring(0, 1).matches("[0-9]")) {
                colorCode = Character.toString(string.charAt(0));
            }
            if (string.substring(0, 1).matches("[k-o]")) {
                // formatCode += "&" + string.charAt(0);
            }
            if (string.substring(0, 1).equalsIgnoreCase("r")) {
                colorCode = "";
                // formatCode = "";
            }
        }
        return colorCode;
    }

    public static String getRemainingTime(Date unbanDate, Player p) {
        long millisDiff = unbanDate.getTime() - System.currentTimeMillis();

        System.out.println(millisDiff);

        int months = (int) (millisDiff / 1000 / 60 / 60 / 24 / 30);
        millisDiff -= (long) months * 1000 * 60 * 60 * 24 * 30;

        int days = (int) (millisDiff / 1000 / 60 / 60 / 24);
        millisDiff -= (long) days * 1000 * 60 * 60 * 24;

        long hours = (millisDiff / 1000 / 60 / 60);
        millisDiff -= hours * 1000 * 60 * 60;

        long minutes = (millisDiff / 1000 / 60);
        millisDiff -= minutes * 1000 * 60;

        long seconds = millisDiff / 1000;

        System.out.println(months + " " + days + " " + hours + " " + minutes + " " + seconds);

        long[] array = new long[]{months, days, hours, minutes, seconds};
        String[] nameArray = new String[]{"Months", "Days", "Hours", "Minutes", "Seconds"};
        int used = 0;
        StringBuilder duration = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) continue;
            duration.append(array[i]).append(" ").append(Main.helper.getMess(p, nameArray[i]));
            if (used == 0) duration.append(" ");
            else break;
            used++;
        }
        return duration.toString();
    }

}
