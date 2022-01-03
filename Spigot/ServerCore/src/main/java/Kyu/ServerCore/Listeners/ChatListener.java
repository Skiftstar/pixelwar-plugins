package Kyu.ServerCore.Listeners;

import Kyu.ServerCore.Bans.Mute;
import Kyu.ServerCore.Bans.MuteHandler;
import Kyu.ServerCore.Main;
import Kyu.ServerCore.Util.Util;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Date;
import java.util.Locale;

public class ChatListener  implements Listener {

    public ChatListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        e.setCancelled(true);
        Player player = e.getPlayer();

        System.out.println(MuteHandler.mutedPlayers.getOrDefault(player.getUniqueId(), null));
        if (MuteHandler.mutedPlayers.containsKey(player.getUniqueId())) {
            Mute ban = MuteHandler.mutedPlayers.get(player.getUniqueId());
            if (!ban.getUnbanDate().before(new Date(System.currentTimeMillis()))) {
                if (ban.isPermanent()) {
                    player.sendMessage(Component.text(Main.helper.getMess(player, "PermaMuteMessage")
                            .replace("%reason", Main.helper.getMess(player, ban.getReason()))));
                } else {
                    player.sendMessage(Component.text(Main.helper.getMess(player, "MuteMessage")
                            .replace("%reason", Main.helper.getMess(player, ban.getReason()))
                            .replace("%duration", Util.getRemainingTime(ban.getUnbanDate(), player))));
                }
                return;
            } else {
                MuteHandler.mutedPlayers.remove(player.getUniqueId());
            }
        }

        User lpUser = Main.lp.getUserManager().getUser(e.getPlayer().getUniqueId());
        String prefix;
        if (lpUser == null) {
            prefix = "User not found";
        } else {
            Group lpGroup = Main.lp.getGroupManager().getGroup(lpUser.getPrimaryGroup());
            if (lpGroup == null) {
                prefix = "Group not found";
            } else {
                prefix = lpGroup.getCachedData().getMetaData().getPrefix();
            }
        }
        String unfilteredMessage = ((TextComponent) e.message()).content();
        String message = "";
        for (String word : unfilteredMessage.split(" ")) {
            if (Main.badWords.contains(word.toLowerCase(Locale.ROOT))) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("core.mod")) {
                        p.sendMessage(Main.helper.getMess(p, "BadWordSent")
                                .replace("%player", ((TextComponent) e.getPlayer().displayName()).content())
                                .replace("%word", word));
                    }
                }
                word = word.replace("[.]", "*");
            }
            message += word + " ";
        }
        if (e.getPlayer().hasPermission("core.colorchat")) {
            message = Util.color(message);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Main.helper.getMess(p, "ChatTemplate")
                    .replace("%prefix", Util.color(prefix))
                    .replace("%player", ((TextComponent) e.getPlayer().displayName()).content())
                    .replace("%message", message));
        }
    }
}
