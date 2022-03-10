package Kyu.ServerCore.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Kyu.SCommand;
import Kyu.ServerCore.Main;
import Kyu.ServerCore.TabAndScoreboard.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class SmallCommands {

    public static List<Player> vanishedPlayers = new ArrayList<>();


    public static void init(Main plugin) {
        SCommand discordCmd = new SCommand(plugin, "discord", Main.helper);
        discordCmd.playerOnly(true);
        discordCmd.exec(e -> {
            e.player().sendMessage(Main.helper.getMess(e.player(), "DiscordMessage")
                    .replace("%link", Main.discordLink));
        });

        SCommand reloadCmd = new SCommand(plugin, "sReload", Main.helper);
        reloadCmd.execPerm("core.reload");
        reloadCmd.exec(e -> {
            String message;
            plugin.loadConfigValues();
            ScoreboardManager.reloadSetup();
            if (e.isPlayer()) {
                message = Main.helper.getMess(e.player(), "ReloadDone", true);
            } else {
                message = Main.helper.getMess("ReloadDone");
            }
            e.sender().sendMessage(message);
        });

        SCommand vanish = new SCommand(plugin, "vanish", Main.helper);
        vanish.execPerm("core.essentials.vanish");
        vanish.playerOnly(true);
        vanish.exec(e -> {
            if (!vanishedPlayers.contains(e.player())) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hidePlayer(plugin, e.player());
                    p.sendMessage(Component.text(Main.helper.getMess(p, "FakeLeaveMessage", false)
                        .replace("%player", ((TextComponent) e.player().name()).content())));
                }
                vanishedPlayers.add(e.player());
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "VanishNowActive", true)));
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showPlayer(plugin, e.player());
                    p.sendMessage(Component.text(Main.helper.getMess(p, "FakeJoinMessage", false)
                        .replace("%player", ((TextComponent) e.player().name()).content())));
                }
                vanishedPlayers.remove(e.player());
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "VanishNowDeactivated", true)));
            }
        });

        SCommand invsee = new SCommand(plugin, "invsee", Main.helper);
        invsee.execPerm("core.essentials.invsee");
        invsee.playerOnly(true);
        invsee.minArgs(1);
        invsee.exec(e -> {
            String playerName = e.args()[0];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "PlayerNotOnline", true)));
                return;
            }
            e.player().openInventory(target.getInventory());
        });

        SCommand ping = new SCommand(plugin, "ping", Main.helper);
        ping.execPerm("core.essentials.ping");
        ping.playerOnly(true);
        ping.exec(e -> {
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "pingMessage", true)
                    .replace("%ping", e.player().getPing() + "")));
        });

        Main.commands.add(ping);
        Main.commands.add(invsee);
        Main.commands.add(vanish);
        Main.commands.add(discordCmd);
        Main.commands.add(reloadCmd);
    }

}
