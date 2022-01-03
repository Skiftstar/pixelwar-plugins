package Kyu.LobbyCore.Commands;

import Kyu.LangSupport.LanguageHelper;
import Kyu.LobbyCore.Listeners.VoidListener;
import Kyu.LobbyCore.Main;
import Kyu.SCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Commands {

    public static List<SCommand> commands = new ArrayList<>();

    public static void initCommands(Main plugin) {
        SCommand spawnCmd = new SCommand(plugin, "spawn", Main.helper);
        spawnCmd.execPerm("lobbycore.spawn");
        spawnCmd.playerOnly(true);
        spawnCmd.exec(e -> {
            Player p = e.player();
            if (Main.spawnLoc == null) {
                p.sendMessage(Main.helper.getMess(p, "NoSpawn", true));
                return;
            }
            p.teleport(Main.spawnLoc);
            p.sendMessage(Main.helper.getMess(p, "TeleportToSpawn", true));

        });

        SCommand spawnPosCmd = new SCommand(plugin, "setSpawn", Main.helper);
        spawnPosCmd.playerOnly(true);
        spawnPosCmd.execPerm("lobbycore.setSpawn");
        spawnPosCmd.exec(e -> {
            Player p = e.player();
            Location loc = p.getLocation();
            Main.spawnLoc = loc;

            plugin.getConfig().set("spawnLoc.X", loc.getX());
            plugin.getConfig().set("spawnLoc.Y", loc.getY());
            plugin.getConfig().set("spawnLoc.Z", loc.getZ());
            plugin.getConfig().set("spawnLoc.Yaw", loc.getYaw());
            plugin.getConfig().set("spawnLoc.Pitch", loc.getPitch());
            plugin.getConfig().set("spawnLoc.World", loc.getWorld().getName());
            plugin.saveConfig();

            p.sendMessage(Main.helper.getMess(p, "LobbySpawnSet", true));
        });


        SCommand voidCmd = new SCommand(plugin, "toggleReset", Main.helper);
        voidCmd.execPerm("lobbycore.togglereset");
        voidCmd.exec(e -> {
            String message;
            if (e.isPlayer()) {
                Player p = e.player();
                String status = !VoidListener.voidReset ? Main.helper.getMess(p, "active") : Main.helper.getMess(p, "inactive");
                message = Main.helper.getMess(p, "VoidResetStatus", true)
                        .replace("%status", status);
            } else {
                String status = !VoidListener.voidReset ? Main.helper.getMess("active") : Main.helper.getMess("inactive");
                message = Main.helper.getMess("VoidResetStatus")
                        .replace("%status", status);
            }
            VoidListener.voidReset = !VoidListener.voidReset;
            plugin.getConfig().set("resetOnVoid", VoidListener.voidReset);
            plugin.saveConfig();
            e.sender().sendMessage(message);
        });

        SCommand reloadCmd = new SCommand(plugin, "lReload", Main.helper);
        reloadCmd.execPerm("lobbycore.reload");
        reloadCmd.exec(e -> {
            String message;
            plugin.loadConfigValues();
            if (e.isPlayer()) {
                message = Main.helper.getMess(e.player(), "ReloadDone", true);
            } else {
                message = Main.helper.getMess("ReloadDone");
            }
            e.sender().sendMessage(message);
        });

        SCommand serversCmd = new SCommand(plugin, "servers", Main.helper);
        serversCmd.execPerm("lobbycore.servers");
        serversCmd.playerOnly(true);
        serversCmd.exec(e -> {
            Main.itemListener.openServerGUI(e.player());
        });


        commands.add(spawnCmd);
        commands.add(spawnPosCmd);
        commands.add(voidCmd);
        commands.add(reloadCmd);
        commands.add(serversCmd);
    }

    public static void replaceLanguageHelper(LanguageHelper helper) {
        for (SCommand command : commands) {
            command.setLangHelper(helper);
        }
    }

}
