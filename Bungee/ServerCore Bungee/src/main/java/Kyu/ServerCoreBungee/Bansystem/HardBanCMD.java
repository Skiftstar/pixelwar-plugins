package Kyu.ServerCoreBungee.Bansystem;

import Kyu.ServerCoreBungee.Main;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class HardBanCMD extends Command {
 
    public HardBanCMD(Main plugin) {
        super("hardban", "core.hardban");
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }
    

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs", true)));
            return;
        }
        String player = args[0];
        String BanType = args[1];
        
    }

}
