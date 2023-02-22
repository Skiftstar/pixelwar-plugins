package Kyu.ServerCoreBungee.Commands;

import Kyu.ServerCoreBungee.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class OnlineCommand extends Command {
    public OnlineCommand(Main main) {
        super("online", "bcore.online");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        StringBuilder players = new StringBuilder();
        for (ProxiedPlayer p : Main.instance().getProxy().getPlayers()) {
            if (players.length() > 0) {
                players.append(", ");
            }
            players.append(p.getDisplayName());
        }
        sender.sendMessage(new TextComponent(Main.helper.getMess(sender, "OnlinePlayers", true)
            .replace("%players", players.toString())));
    }

}
