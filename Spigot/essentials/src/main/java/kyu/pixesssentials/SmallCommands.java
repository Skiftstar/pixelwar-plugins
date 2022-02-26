package kyu.pixesssentials;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import Kyu.SCommand;
import kyu.pixesssentials.Util.Util;
import net.kyori.adventure.text.Component;

public class SmallCommands {
    public static void init(Main plugin) {
        SCommand setSpawnCMD = new SCommand(plugin, "setSpawn", Main.helper);
        setSpawnCMD.playerOnly(true);
        setSpawnCMD.execPerm("core.essentials.setSpawn");
        setSpawnCMD.exec(e -> {
            Location loc = e.player().getLocation();
            Main.spawnPos = loc;
            YamlConfiguration config = Main.getInstance().getConfig();
            config.set("Essentials.Spawn.X", loc.getX());
            config.set("Essentials.Spawn.Y", loc.getY());
            config.set("Essentials.Spawn.Z", loc.getZ());
            config.set("Essentials.Spawn.Pitch", loc.getPitch());
            config.set("Essentials.Spawn.Yaw", loc.getYaw());
            config.set("Essentials.Spawn.World", loc.getWorld().getUID().toString());
            Main.getInstance().saveConfig();
        });

        SCommand spawnCMD = new SCommand(plugin, "spawn", Main.helper);
        spawnCMD.playerOnly(true);
        spawnCMD.execPerm("core.essentials.spawn");
        spawnCMD.exec(e -> {
            if (Main.spawnPos != null) {
                e.player().teleport(Main.spawnPos);
            }
        });

        SCommand cbCMD = new SCommand(plugin, "cb", Main.helper);
        cbCMD.playerOnly(true);
        cbCMD.exec(e -> {
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "CBInfoMessage")));
        });

        SCommand eReload = new SCommand(plugin, "eReload", Main.helper);
        eReload.execPerm("core.essentials.reload");
        eReload.exec(e -> {
            Main.getInstance().loadConfigValues();
            e.sender().sendMessage(Util.color("&aReload done"));
        });

        Main.commands.add(setSpawnCMD);
        Main.commands.add(spawnCMD);
        Main.commands.add(cbCMD);
        Main.commands.add(eReload);
    }
}
