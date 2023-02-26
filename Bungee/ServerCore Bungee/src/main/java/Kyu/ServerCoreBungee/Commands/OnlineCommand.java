package Kyu.ServerCoreBungee.Commands;

import Kyu.ServerCoreBungee.Main;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
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

            User lpUser = Main.lp.getUserManager().getUser(p.getUniqueId());
            String prefix;
            if (lpUser == null) {
                prefix = "User not found";
            } else {
                Group lpGroup = Main.lp.getGroupManager().getGroup(lpUser.getPrimaryGroup());
                if (lpGroup == null) {
                    prefix = "Group not found";
                } else {
                    prefix = lpGroup.getCachedData().getMetaData().getPrefix();
                    if (prefix == null) prefix = "";
                }
            }
            prefix = ChatColor.translateAlternateColorCodes('&', prefix);

            players.append(prefix + p.getDisplayName());
        }
        sender.sendMessage(new TextComponent(Main.helper.getMess(sender, "OnlinePlayers", true)
            .replace("%players", players.toString())));
    }

}
