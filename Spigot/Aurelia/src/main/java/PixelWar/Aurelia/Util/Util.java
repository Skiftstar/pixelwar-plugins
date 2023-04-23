package PixelWar.Aurelia.Util;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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

    public static ItemStack getPlayerHead(Player p) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(p);
        item.setItemMeta(meta);
        return item;
    }

}
