package Kyu.ServerCoreBungee.Commands;

import Kyu.ServerCoreBungee.Main;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TeamchatCommand extends Command {

    public TeamchatCommand(Main main) {
        super("tc", "bcore.teamchat");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(Main.helper.getMess("PlayerOnly")));
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (args.length < 1) {
            p.sendMessage(new TextComponent(Main.helper.getMess(p, "NEArgs")));
            return;
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

        StringBuilder message = new StringBuilder();
        message.append(ChatColor.AQUA).append("[TC] ").append(prefix + p.getDisplayName()).append(ChatColor.AQUA + " >>");
        for (String word : args) {
            message.append(" ").append(word);
        }
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.hasPermission("bcore.teamchat")) {
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message.toString())));
            }
        }
    }
}
