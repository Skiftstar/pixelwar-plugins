package Kyu.ServerCoreBungee.Ontime.commands;

import java.util.ArrayList;
import java.util.Iterator;
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
        super("ontime", "ontime.show.self", new String[0]);
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    public void execute(CommandSender cs, String[] args) {
        if (args.length == 0) {
            if (!(cs instanceof ProxiedPlayer)) {
                return;
            }

            ProxiedPlayer sender = (ProxiedPlayer)cs;
            if (!sender.hasPermission("ontime.show.self") && !sender.hasPermission("ontime.show.*")) {
                sender.sendMessage(new TextComponent("§cDu hast dazu keine Berechtigungen"));
            } else {
                long totaltime = Util.getOntimeTotal(sender.getUniqueId().toString());
                long daytime = Util.getDayOnTime(sender.getUniqueId().toString());
                long weektime = Util.getWeekOnTime(sender.getUniqueId().toString());
                long monthtime = Util.getMonthOnTime(sender.getUniqueId().toString());

                String[] resultday = convertMillisToDHMS(daytime);
                sender.sendMessage(new TextComponent("  §b=== Ontime ==="));
                String dayString = "";
                if (resultday[0] != null) {
                    dayString = dayString + resultday[0] + " ";
                }

                if (resultday[1] != null) {
                    dayString = dayString + resultday[1] + " ";
                }

                if (resultday[2] != null) {
                    dayString = dayString + resultday[2];
                }

                sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Heute: " + dayString.trim()));
                String[] resulttotal = convertMillisToDHMS(totaltime);
                String[] restultweek = convertMillisToDHMS(weektime);
                String[] resultmonth = convertMillisToDHMS(monthtime);
                String weekString = "";
                if (restultweek[0] != null) {
                    weekString = weekString + restultweek[0] + " ";
                }

                if (restultweek[1] != null) {
                    weekString = weekString + restultweek[1] + " ";
                }

                if (restultweek[2] != null) {
                    weekString = weekString + restultweek[2];
                }

                try {
                    sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Diese Woche: " + weekString.trim()));
                } catch (IndexOutOfBoundsException var34) {
                    try {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diese Woche: " + weekString));
                    } catch (IndexOutOfBoundsException var33) {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diese Woche: "));
                    }
                }

                String monthString = "";
                if (resultmonth[0] != null) {
                    monthString = monthString + resultmonth[0] + " ";
                }

                if (resultmonth[1] != null) {
                    monthString = monthString + resultmonth[1] + " ";
                }

                if (resultmonth[2] != null) {
                    monthString = monthString + resultmonth[2];
                }

                try {
                    sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Diesen Monat: " + monthString.trim()));
                } catch (IndexOutOfBoundsException var32) {
                    try {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diesen Monat: " + monthString));
                    } catch (IndexOutOfBoundsException var31) {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diesen Monat: "));
                    }
                }

                String totalString = "";
                if (resulttotal[0] != null) {
                    totalString = totalString + resulttotal[0] + " ";
                }

                if (resulttotal[1] != null) {
                    totalString = totalString + resulttotal[1] + " ";
                }

                if (resulttotal[2] != null) {
                    totalString = totalString + resulttotal[2];
                }

                try {
                    sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Insgesamt: " + totalString.trim()));
                } catch (IndexOutOfBoundsException var30) {
                    try {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Insgesamt: " + totalString));
                    } catch (IndexOutOfBoundsException var29) {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Insgesamt: "));
                    }
                }
            }
        } else {
            ProxiedPlayer sender = (ProxiedPlayer)cs;
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
            if (p == null) {
                p.sendMessage(new TextComponent("§cDieser Spieler ist offline/existiert nicht"));
                return;
            }

            if (!sender.hasPermission("ontime.show.other") && !sender.hasPermission("ontime.show.*")) {
                sender.sendMessage(new TextComponent("§cDu hast dazu keine Berechtigungen"));
            } else {
                long totaltime = Util.getOntimeTotal(p.getUniqueId().toString());
                long daytime = Util.getDayOnTime(p.getUniqueId().toString());
                long weektime = Util.getWeekOnTime(p.getUniqueId().toString());
                long monthtime = Util.getMonthOnTime(p.getUniqueId().toString());
                sender.sendMessage(new TextComponent("  §b=== Ontime === §7(Von: §e" + p.getName() + "§7)"));
                String[] resultday = convertMillisToDHMS(daytime);
                String dayString = "";
                if (resultday[0] != null) {
                    dayString = dayString + resultday[0] + " ";
                }

                if (resultday[1] != null) {
                    dayString = dayString + resultday[1] + " ";
                }

                if (resultday[2] != null) {
                    dayString = dayString + resultday[2];
                }

                sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Heute: " + dayString.trim()));
                String[] resulttotal = convertMillisToDHMS(totaltime);
                String[] resultWeek = convertMillisToDHMS(weektime);
                String[] resultmonth = convertMillisToDHMS(monthtime);
                String weekString = "";
                if (resultWeek[0] != null) {
                    weekString = weekString + resultWeek[0] + " ";
                }

                if (resultWeek[1] != null) {
                    weekString = weekString + resultWeek[1] + " ";
                }

                if (resultWeek[2] != null) {
                    weekString = weekString + resultWeek[2];
                }

                try {
                    sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Diese Woche: " + weekString.trim()));
                } catch (IndexOutOfBoundsException var28) {
                    try {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diese Woche: " + weekString));
                    } catch (IndexOutOfBoundsException var27) {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diese Woche: "));
                    }
                }

                String monthString = "";
                if (resultmonth[0] != null) {
                    monthString = monthString + resultmonth[0] + " ";
                }

                if (resultmonth[1] != null) {
                    monthString = monthString + resultmonth[1] + " ";
                }

                if (resultmonth[2] != null) {
                    monthString = monthString + resultmonth[2];
                }

                try {
                    sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Diesen Monat: " + monthString.trim()));
                } catch (IndexOutOfBoundsException var26) {
                    try {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diesen Monat: " + monthString));
                    } catch (IndexOutOfBoundsException var25) {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Diesen Monat: "));
                    }
                }

                String totalString = "";
                if (resulttotal[0] != null) {
                    totalString = totalString + resulttotal[0] + " ";
                }

                if (resulttotal[1] != null) {
                    totalString = totalString + resulttotal[1] + " ";
                }

                if (resulttotal[2] != null) {
                    totalString = totalString + resulttotal[2];
                }

                try {
                    sender.sendMessage(new TextComponent("" + ChatColor.AQUA + "Insgesamt: " + totalString.trim()));
                } catch (IndexOutOfBoundsException var24) {
                    try {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Insgesamt: " + totalString));
                    } catch (IndexOutOfBoundsException var23) {
                        sender.sendMessage(new TextComponent(ChatColor.AQUA + "Insgesamt: "));
                    }
                }
            }
        }

    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList();
        Iterator var4 = Main.instance().getProxy().getPlayers().iterator();

        while(var4.hasNext()) {
            ProxiedPlayer all = (ProxiedPlayer)var4.next();
            if (args.length == 1) {
                list.add(all.getName());
            }
        }

        return list;
    }

    public static String[] convertMillisToDHMS(long milliseconds) {
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

        return timeArray;
    }
}