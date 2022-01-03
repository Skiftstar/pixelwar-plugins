package Kyu.ServerCore.TabAndScoreboard.Listeners;

import Kyu.ServerCore.Main;
import Kyu.ServerCore.TabAndScoreboard.ScoreboardManager;
import Kyu.ServerCore.TabAndScoreboard.Tablist;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class LuckpermsListener implements Listener {

    private Main plugin;

    public LuckpermsListener(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onLuckCmd(PlayerCommandPreprocessEvent e) {
        String[] options = new String[]{"luckperms", "lp", "perm", "permission", "permissions", "perms"};
        if (!checkStartsWith(e.getMessage().toLowerCase(), options)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                for (Tablist list : ScoreboardManager.getTablists()) {
                    list.createTeams();
                    list.sortPlayers();
                }
            }
        }, 40);
    }

    private boolean checkStartsWith(String mess, String[] options) {
        for (String option : options) {
            if (mess.startsWith("/" + option)) {
                return true;
            }
        }
        return false;
    }
}
