package Kyu.ServerCore.LuckPermsDenial;

import Kyu.ServerCore.Main;
import Kyu.ServerCore.Util.LuckPermsAPI;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;

public class LuckPermsDenial implements Listener {

    /*
    This class prevents staff from editing / assigning / revoking groups that are higher in weight than their own group
     */

    public LuckPermsDenial(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onLuckCmd(PlayerCommandPreprocessEvent e) {
        String[] options = new String[]{"luckperms", "lp", "perm", "permission", "permissions", "perms"};
        if (!checkStartsWith(e.getMessage().toLowerCase(), options)) {
            return;
        }
        String[] args = e.getMessage().split(" ");
        args = Arrays.copyOfRange(args, 1, args.length);
        if (!(args[0].equalsIgnoreCase("user") || args[0].equalsIgnoreCase("group"))) {
            return;
        }
        System.out.println("fired");
        Player p = e.getPlayer();
        if (p.hasPermission("core.ignoreLuckDenial")) {
            return;
        }
        Group group = LuckPermsAPI.getGroup(p);
        int playerWeight;
        if (group == null) {
            playerWeight = 0;
        } else {
            playerWeight = group.getWeight().orElse(0);
        }
        int toChangeWeight;
        if (args[0].equalsIgnoreCase("group")) {
            String groupName = args[1];
            Group toChangeGroup = LuckPermsAPI.getGroup(groupName);
            if (toChangeGroup == null) {
                return;
            }
            toChangeWeight = toChangeGroup.getWeight().orElse(0);
        } else {
            String user = args[1];
            Group mainGroup = LuckPermsAPI.getGroupFromPlayerName(user);
            if (mainGroup == null) {
                toChangeWeight = 0;
            } else {
                toChangeWeight = mainGroup.getWeight().orElse(0);
            }
        }
//        System.out.println("am here");
//        System.out.println(toChangeWeight);
//        System.out.println(playerWeight);
        if (toChangeWeight > playerWeight) {
            e.setCancelled(true);
            p.sendMessage(Main.helper.getMess(p, "LuckPermsCantChangeThis", true)
                    .replace("%target", args[1]));
        }
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