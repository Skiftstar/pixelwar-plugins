package PixelWar.Aurelia.GUI.MainMneu;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import PixelWar.Aurelia.Main;
import PixelWar.Aurelia.API.GuiAPI.GUI;
import PixelWar.Aurelia.API.GuiAPI.Item.GuiItem;
import PixelWar.Aurelia.API.GuiAPI.Window.Window;
import PixelWar.Aurelia.GUI.AdminMenu.AdminMainMenu;
import PixelWar.Aurelia.Player.AureliaPlayer;
import PixelWar.Aurelia.Util.StringArray;
import PixelWar.Aurelia.Util.Util;

public class MainMenu {

    public static Window build(GUI gui, AureliaPlayer aureliaPlayer) {
        Player player = aureliaPlayer.getPlayer();

        Window mainMenu = new Window(gui, 6, Main.helper.getMess(player, "MainMenuTitle"));

        List<String> statsTemplate = Main.helper.getLore(player, "StatsLore");
        String[] statsLore = new StringArray(statsTemplate)
            .replace("%health", "" + aureliaPlayer.getHealth())
            .getArray();
        GuiItem statsItem = new GuiItem(Util.getPlayerHead(player), Main.helper.getMess(player, "&6Your Profile"), statsLore);
        mainMenu.setItem(statsItem, 13);

        if (player.hasPermission("aurelia.admin")) {
            GuiItem adminItem = new GuiItem(Material.REDSTONE, Main.helper.getMess(player, "&cAdmin-View"), 1)
                .withListener(e -> {
                    gui.openWindow(AdminMainMenu.build(gui, aureliaPlayer));
                });
            mainMenu.setItem(adminItem, 53);
        }

        return mainMenu;
    }
    
}
