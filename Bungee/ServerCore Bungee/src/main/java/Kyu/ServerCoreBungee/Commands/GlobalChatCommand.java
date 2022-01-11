package Kyu.ServerCoreBungee.Commands;

import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Ban;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Util;
import Kyu.ServerCoreBungee.Bansystem.BansHandler;
import Kyu.ServerCoreBungee.Main;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Date;

public class GlobalChatCommand extends Command {

    public GlobalChatCommand(Main main) {
        super("global", "bcore.global", "g");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess("PlayerOnly")));
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (args.length == 0) {
            boolean chatEnabled = p.hasPermission("bcore.globalenabled");

            User lpUser = Main.lp.getUserManager().getUser(p.getUniqueId());
            if (chatEnabled) lpUser.data().remove(Node.builder("bcore.globalenabled").build());
            else lpUser.data().add(Node.builder("bcore.globalenabled").build());
            Main.lp.getUserManager().saveUser(lpUser);

            String stateSt = chatEnabled ? LanguageHelper.getMess(p, "deactivated") : LanguageHelper.getMess(p, "activated");
            p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GlobalChatStatus")
                    .replace("%status", stateSt)));
            return;
        }

        if (!p.hasPermission("bcore.globalenabled")) {
            p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GlobalChatNotActive")));
            return;
        }

        if (BansHandler.gMuteds.containsKey(p.getUniqueId())) {
            Ban ban = BansHandler.gMuteds.get(p.getUniqueId());
            if (!ban.getUnbanDate().before(new Date(System.currentTimeMillis()))) {
                if (ban.isPermanent()) {
                    p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatPermaMuteMessage")
                    //TODO: fix reason
                            .replace("%reason", LanguageHelper.getMess(p, ban.getReason()))));
                } else {
                    p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatMuteMessage")
                            .replace("%reason", LanguageHelper.getMess(p, ban.getReason()))
                            .replace("%duration", Util.getRemainingTime(ban.getUnbanDate(), LanguageHelper.getLanguage(p)))));
                }
                return;
            } else {
                BansHandler.remove(p.getUniqueId(), BansHandler.gMuteds);
            }
        }

        StringBuilder message = new StringBuilder();
        message.append(ChatColor.DARK_GRAY)
                .append("[")
                .append(p.getServer().getInfo().getName())
                .append("] ")
                .append(ChatColor.GRAY).append(p.getDisplayName())
                .append(ChatColor.DARK_GRAY)
                .append(" >>")
                .append(ChatColor.GRAY);
        for (String word : args) {
            message.append(" ").append(word);
        }

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.hasPermission("bcore.globalenabled")) {
                player.sendMessage(new TextComponent(message.toString()));
            }
        }

    }
}
