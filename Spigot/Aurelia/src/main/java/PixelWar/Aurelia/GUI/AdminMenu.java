package PixelWar.Aurelia.GUI;

import org.bukkit.entity.Player;

import PixelWar.Aurelia.Main;
import PixelWar.Aurelia.API.GuiAPI.GUI;
import PixelWar.Aurelia.API.GuiAPI.Window.Window;
import PixelWar.Aurelia.Player.AureliaPlayer;

public class AdminMenu {
    
    public static Window build(GUI gui, AureliaPlayer aureliaPlayer) {
        Player player = aureliaPlayer.getPlayer();

        Window adminWindow = new Window(gui, 6, Main.helper.getMess(player, "AdminMenuTitle"));
        adminWindow.setOnClose(e -> {
            gui.openWindow(MainMenu.build(gui, aureliaPlayer));
        });

        return adminWindow;
    }
}
