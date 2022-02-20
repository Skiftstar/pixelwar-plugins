package Kyu.ServerCore.TabAndScoreboard;

import Kyu.ServerCore.Main;
import Kyu.ServerCore.Util.LuckPermsAPI;
import Kyu.ServerCore.Util.Util;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Sidebar {

    private Scoreboard sb;
    private Player p;
    private int task;
    private Objective obj;

    public Sidebar(Scoreboard sb, Player p) {
        this.sb = sb;
        this.p = p;
    }

    public void init() {
        obj = sb.registerNewObjective("ServerName", "dummy", Component.text(Main.helper.getMess(p, "SidebarTitle")
                .replace("%ServerName", Main.serverName)));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);


        Team rank = sb.registerNewTeam("rankDisplay");
        rank.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        Team money = sb.registerNewTeam("moneyCounter");
        money.addEntry(ChatColor.YELLOW + "" + ChatColor.WHITE);
        Team online = sb.registerNewTeam("onlineCounter");
        online.addEntry(ChatColor.BLUE + "" + ChatColor.WHITE);
        updateSidebar();
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::updateSidebar, Main.sidebarDelay * 20L, Main.sidebarDelay * 20L);
    }

    private void updateSidebar() {
        List<String> lines = Main.helper.getLore(p, "Sidebar");
        int index = lines.size() - 1;
//        Collections.reverse(lines);
        for (String line : lines) {
            Team team = null;
            if (line.contains("%rankName")) {
                team = sb.getTeam("rankDisplay");
                line = line.replace("%rankName", Util.color(LuckPermsAPI.getGroupPrefix(p).replace(Main.toIgnore, "")));
                obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(index);
            } else if (line.contains("%players")) {
                team = sb.getTeam("onlineCounter");
                line = line.replace("%players", "" + Bukkit.getOnlinePlayers().size());
                obj.getScore(ChatColor.BLUE + "" + ChatColor.WHITE).setScore(index);
            } else if (line.contains("%money")) {
                team = sb.getTeam("moneyCounter");
                Economy econ = Main.econ;
                double money = econ.getBalance(p);
                line = line.replace("%money", "" + round(money, 2));
                obj.getScore(ChatColor.YELLOW + "" + ChatColor.WHITE).setScore(index);
            }
            if (team != null) {
                team.prefix(Component.text(Util.color(line)));
            } else {
                obj.getScore(line).setScore(index);
            }
//            if (team == null) {
//                Score score = obj.getScore(index + "");
//            } else {
//                team.addEntry(index + "");
//                team.prefix(Component.text(Util.color(line)));
//            }
//
//            Score score = obj.getScore(index + "");
//            score.setScore(index);
            index--;
        }
    }

    public void kill() {
        Bukkit.getScheduler().cancelTask(task);
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
