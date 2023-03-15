package Kyu.ServerCore.TabAndScoreboard;

import Kyu.ServerCore.Main;
import Kyu.ServerCore.Util.Util;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Tablist {

    private Scoreboard sb;
    private Map<String, Team> teams = new HashMap<>();

    public Tablist(Scoreboard sb) {
        this.sb = sb;
    }

    @SuppressWarnings("deprecation")
    public void createTeams() {
        List<Group> groups = new ArrayList<>(Main.lp.getGroupManager().getLoadedGroups());

        Collections.sort(groups, new Comparator<Group>() {
            public int compare(Group o1, Group o2) {
                // compare two instance of `Score` and return `int` as result.
                int o2Weight = o2.getWeight().isPresent() ? o2.getWeight().getAsInt() : 0;
                int o1Weight = o1.getWeight().isPresent() ? o1.getWeight().getAsInt() : 0;
                return Integer.compare(o2Weight, o1Weight);
            }
        });

        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);

            char c = (char) (i + 65);
            Team team = sb.getTeam(Character.toString(c)) != null ? sb.getTeam(Character.toString(c)) : sb.registerNewTeam(Character.toString(c));
            String prefix = group.getCachedData().getMetaData().getPrefix();
            if (prefix != null) {
                team.prefix(Component.text(Util.color(prefix)));
                team.setColor(ChatColor.getByChar(Util.getColors(prefix)));
            }
            String suffix = group.getCachedData().getMetaData().getSuffix();
            if (suffix != null) {
                team.suffix(Component.text(Util.color(suffix)));
            }
            teams.put(group.getName(), team);
        }
    }

    public void sortPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sortPlayer(p);
        }
    }

    public void sortPlayer(Player p) {
        User user = Main.lp.getUserManager().getUser(p.getUniqueId());
        if (user == null) return;
        String groupName = user.getPrimaryGroup();
        teams.get(groupName).addPlayer(p);
    }

    public void removePlayer(Player p) {
        User user = Main.lp.getUserManager().getUser(p.getUniqueId());
        if (user == null) return;
        String groupName = user.getPrimaryGroup();
        teams.get(groupName).removePlayer(p);
    }

}
