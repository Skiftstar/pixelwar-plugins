package Kyu.ServerCoreBungee.Ontime.commands;

import java.util.ArrayList;
import java.util.List;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Ontime.utils.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class OntimeCommand extends Command implements TabExecutor {
    public OntimeCommand(Main main) {
        super("ontime", "ontime.show.self");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    public void execute(CommandSender cs, String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer sender = (ProxiedPlayer)cs;

        if (args.length == 0) {
            String mess = buildResponse(sender.getUniqueId().toString());
            sender.sendMessage(new TextComponent(mess));

        } else {
            ProxiedPlayer otherP = ProxyServer.getInstance().getPlayer(args[0]);
            if (otherP == null) {
                sender.sendMessage(new TextComponent("§cDieser Spieler ist offline/existiert nicht"));
                return;
            }

            if (!sender.hasPermission("ontime.show.other")) {
                sender.sendMessage(new TextComponent("§cDu hast dazu keine Berechtigungen"));
                return;
            }

            String mess = buildResponse(otherP.getUniqueId().toString(), otherP.getName());
            sender.sendMessage(new TextComponent(mess));
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if (args.length == 1) {
                list.add(p.getName());
            }
        }
        return list;
    }

    private static String buildResponse(String uuid, String... playerName) {
        String header = playerName.length > 0 ? "  §b=== Ontime === §7(Von: §e" + playerName[0] + "§7)" : "  §b=== Ontime ===";

        long[] playtimes = Util.getPlaytime(uuid);
        long dayTime = playtimes[0];
        long weekTime = playtimes[1];
        long monthTime = playtimes[2];
        long totalTime = playtimes[3];


        String dayString = convertMillisToDHMS(dayTime);
        String weekString = convertMillisToDHMS(weekTime);
        String monthString = convertMillisToDHMS(monthTime);
        String totalString = convertMillisToDHMS(totalTime);

        String mess = header + "\n" 
            + ChatColor.AQUA + "Heute: " + dayString.trim() + "\n"
            + ChatColor.AQUA + "Diese Woche: " + weekString.trim() + "\n"
            + ChatColor.AQUA + "Diesen Monat: " + monthString.trim() + "\n"
            + ChatColor.AQUA + "Insgesamt: " + totalString.trim();


        return mess;
    }

    public static String convertMillisToDHMS(long milliseconds) {
        long seconds = milliseconds / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        String[] timeArray = new String[3];
        if (days > 0L) {
            timeArray[0] = "" + days + " Tage";
            hours %= 24L;
        }

        if (hours > 0L || days > 0L) {
            timeArray[1] = "" + hours + " Stunden";
            minutes %= 60L;
        }

        if (minutes == 0L && hours == 0L && days == 0L) {
            timeArray[0] = "0 Minuten";
        } else if (minutes > 0L) {
            timeArray[0] = "" + minutes + " Minuten";
        }

        String result = "";

        for (int i = 0; i < 3; i++) {
            if (timeArray[i] != null) {
                result += timeArray[i] + " ";
            }
        }

        return result;
    }
}