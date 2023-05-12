package PixelWar.Aurelia.Commands.GUICommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import PixelWar.Aurelia.Main;
import PixelWar.Aurelia.API.GuiAPI.GUI;
import PixelWar.Aurelia.GUI.MainMneu.MainMenu;
import PixelWar.Aurelia.Player.AureliaPlayer;

public class MainMenuCommand implements CommandExecutor {

    public MainMenuCommand(Main plugin) {
        plugin.getCommand("menu").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String string,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.helper.getMess("PlayerOnly", true));
            return false;
        }

        Player player = (Player) sender;
        AureliaPlayer aureliaPlayer = AureliaPlayer.getPlayer(player);
        GUI gui = new GUI(player, Main.getInstance());
        gui.openWindow(MainMenu.build(gui, aureliaPlayer));

        return true;
    }
    
}
