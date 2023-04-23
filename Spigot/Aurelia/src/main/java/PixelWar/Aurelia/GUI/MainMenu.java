package PixelWar.Aurelia.GUI;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import PixelWar.Aurelia.Main;
import PixelWar.Aurelia.API.GuiAPI.GUI;
import PixelWar.Aurelia.API.GuiAPI.Item.GuiItem;
import PixelWar.Aurelia.API.GuiAPI.Window.Window;
import PixelWar.Aurelia.Player.AureliaPlayer;
import PixelWar.Aurelia.Util.StringArray;
import PixelWar.Aurelia.Util.Util;

public class MainMenu {

    public static Window build(AureliaPlayer aureliaPlayer) {
        Player player = aureliaPlayer.getPlayer();

        //TODO: remove this and move it to command? idk
        GUI gui = new GUI(player, Main.getInstance());

        Window mainMenu = new Window(gui, 6, Main.helper.getMess(player, "MainMenuTitle"));

        List<String> statsTemplate = Main.helper.getLore(player, "StatsLore");
        String[] statsLore = new StringArray(statsTemplate)
            .replace("%health", "" + aureliaPlayer.getHealth())
            .getArray();
            

        GuiItem statsItem = new GuiItem(Util.getPlayerHead(player), Main.helper.getMess(player, "&6Your Profile"), statsLore);
        mainMenu.setItem(statsItem, 13);

        return mainMenu;
    }
    
}
