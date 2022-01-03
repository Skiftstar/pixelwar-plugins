package Kyu.ServerCore.Util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

public class LuckPermsAPI {

    private static LuckPerms luckAPI;

    public static void setLuckAPI(LuckPerms luckAPI) {
        LuckPermsAPI.luckAPI = luckAPI;
    }

    public static String getGroupPrefix(Player p) {
        User user = luckAPI.getUserManager().getUser(p.getUniqueId());
        if (user == null) {
            return "";
        }
        String groupName = user.getPrimaryGroup();
        Group group = luckAPI.getGroupManager().getGroup(groupName);
        if (group == null) {
            return "";
        }
        return group.getCachedData().getMetaData().getPrefix() == null ? "" : group.getCachedData().getMetaData().getPrefix();
    }

    public static Group getGroup(Player p) {
        User user = luckAPI.getUserManager().getUser(p.getUniqueId());
        if (user == null) {
            return null;
        }
        String groupName = user.getPrimaryGroup();
        Group group = luckAPI.getGroupManager().getGroup(groupName);
        if (group == null) {
            return null;
        }
        return group;
    }

    public static Group getGroup(String name) {
        Group group = luckAPI.getGroupManager().getGroup(name);
        if (group == null) {
            return null;
        }
        return group;
    }

    public static Group getGroupFromPlayerName(String name) {
        User user = luckAPI.getUserManager().getUser(name);
        if (user == null) {
            return null;
        }
        String groupName = user.getPrimaryGroup();
        Group group = luckAPI.getGroupManager().getGroup(groupName);
        if (group == null) {
            return null;
        }
        return group;
    }

    public static LuckPerms getLuckAPI() {
        return luckAPI;
    }
}