package Kyu.ServerCore.TabAndScoreboard;

import Kyu.ServerCore.Main;
import Kyu.ServerCore.TabAndScoreboard.Listeners.JoinLeaveListener;
import Kyu.ServerCore.TabAndScoreboard.Listeners.LuckpermsListener;
import Kyu.ServerCore.Util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class ScoreboardManager {

    public static Scoreboard board;
    public static Map<Player, Scoreboard> boards = new HashMap<>();
    public static Map<Player, Tablist> tablists = new HashMap<>();
    public static Map<Player, Sidebar> sidebars = new HashMap<>();
    
    public static void setup(Main plugin) {
        new LuckpermsListener(plugin);
        new JoinLeaveListener(plugin);
        reloadSetup();
    }

    public static void reloadSetup() {
        board = Bukkit.getScoreboardManager().getNewScoreboard();

        boards.clear();
        tablists.clear();
        sidebars.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            setupPlayer(p);
        }
    }

    public static void setupPlayer(Player p) {
        if (boards.containsKey(p)) p.setScoreboard(boards.get(p));
        else {
            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            boards.put(p, sb);
            p.setScoreboard(sb);
        }
        if (tablists.containsKey(p)) tablists.get(p).createTeams();
        else {
            Tablist tablist = new Tablist(boards.get(p));
            tablists.put(p, tablist);
            tablist.createTeams();
            tablist.sortPlayers();
        }
        if (sidebars.containsKey(p)) sidebars.get(p).init();
        else {
            Sidebar sidebar = new Sidebar(boards.get(p), p);
            sidebars.put(p, sidebar);
            sidebar.init();
        }
        setHeaderFooter(p);

        //Update other players Tablists
        for (Tablist list : tablists.values()) {
            list.sortPlayer(p);
        }
    }

    public static void setHeaderFooter(Player p) {
        TextComponent head;
        String text = Main.helper.getMess(p, "TabHeader");
        head = Component.text(Util.color(text));
        TextComponent foot;
        text = Main.helper.getMess(p, "TabFooter");
        foot = Component.text(Util.color(text));
        p.sendPlayerListHeaderAndFooter(head, foot);
    }

    public static Scoreboard getBoard(Player p) {
        return boards.get(p);
    }

    public static void removePlayer(Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        sidebars.get(p).kill();
        sidebars.remove(p);
        boards.remove(p);
        for (Tablist list : tablists.values()) {
            list.removePlayer(p);
        }
    }

    public static Collection<Tablist> getTablists() {
        return tablists.values();
    }
}
