package Kyu.ServerCoreBungee.Commands;

import Kyu.ServerCoreBungee.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCMD extends Command {

    public ReloadCMD(Main main) {
        super("breload", "bcore.reload");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        Main.instance().loadConfigValues();
        sender.sendMessage(new TextComponent(Main.helper.getMess(sender, "ReloadDone", true)));
    }
}
