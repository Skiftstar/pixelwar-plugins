package kyu.pixesssentials.Commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import Kyu.SCommand;
import kyu.pixesssentials.Main;
import kyu.pixesssentials.Util.Pair;
import kyu.pixesssentials.Util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class SmallCommands {

    public static Map<Player, Pair<Integer, Player>> tpaRequests = new HashMap<>();

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
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "SpawnSetMess", true)));
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

        SCommand setHome = new SCommand(plugin, "sethome", Main.helper);
        setHome.execPerm("core.essentials.sethome");
        setHome.playerOnly(true);
        setHome.exec(e -> {
            YamlConfiguration config = Main.getInstance().getPlayerHomeConfig();
            Location loc = e.player().getLocation();
            config.set(e.player().getUniqueId().toString() + ".X", loc.getX());
            config.set(e.player().getUniqueId().toString() + ".Y", loc.getY());
            config.set(e.player().getUniqueId().toString() + ".Z", loc.getZ());
            config.set(e.player().getUniqueId().toString() + ".Pitch", loc.getPitch());
            config.set(e.player().getUniqueId().toString() + ".Yaw", loc.getYaw());
            config.set(e.player().getUniqueId().toString() + ".World", loc.getWorld().getUID().toString());
            Main.getInstance().save(config);
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "HomeSetSuccess", true)));
        });

        SCommand home = new SCommand(plugin, "home", Main.helper);
        home.execPerm("core.essentials.home");
        home.playerOnly(true);
        home.exec(e -> {
            YamlConfiguration config = Main.getInstance().getPlayerHomeConfig();
            if (config.get(e.player().getUniqueId().toString()) == null) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "NoHomeSet", true)));
                return;
            }
            double x = config.getDouble(e.player().getUniqueId().toString() + ".X");
            double y = config.getDouble(e.player().getUniqueId().toString() + ".Y");
            double z = config.getDouble(e.player().getUniqueId().toString() + ".Z");
            float pitch = (float) config.getDouble(e.player().getUniqueId().toString() + ".Pitch");
            float yaw = (float) config.getDouble(e.player().getUniqueId().toString() + ".Yaw");
            UUID world = UUID.fromString(config.getString(e.player().getUniqueId().toString() + ".World"));
            Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
            e.player().teleport(loc);
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "HomeTeleportSuccess", true)));
        });

        SCommand tpa = new SCommand(plugin, "tpa", Main.helper);
        tpa.execPerm("core.essentials.tpa");
        tpa.playerOnly(true);
        tpa.minArgs(1);
        tpa.exec(e -> {
            String playerName = e.args()[0];
            Player recipent = Bukkit.getPlayer(playerName);
            if (recipent == null) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "PlayerNotOnline", true)));
                return;
            }
            recipent.sendMessage(Component.text(Main.helper.getMess(recipent, "TPARequestReceived", true)
                    .replace("%player", ((TextComponent) e.player().displayName()).content())
                    .replace("%time", Main.tpaTimeout + "")));
            e.player().sendMessage(Component
                    .text(Main.helper.getMess(e.player(), "TPARequestSent", true).replace("%player", playerName)));

            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                tpaRequests.remove(recipent);
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "TPATimeout", true)));
            }, Main.tpaTimeout * 20);
            tpaRequests.remove(recipent);
            tpaRequests.put(recipent, new Pair<Integer, Player>(taskId, e.player()));
        });

        SCommand tpaccept = new SCommand(plugin, "tpaccept", Main.helper);
        tpaccept.execPerm("core.essentials.tpaccept");
        tpaccept.playerOnly(true);
        tpaccept.exec(e -> {
            if (!tpaRequests.containsKey(e.player())) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "NoOpenTpaRequests", true)));
                return;
            }
            Pair<Integer, Player> pair = tpaRequests.get(e.player());
            tpaRequests.remove(e.player());
            Bukkit.getScheduler().cancelTask(pair.first);
            pair.second.teleport(e.player());
        });

        SCommand tps = new SCommand(plugin, "tps", Main.helper);
        tps.execPerm("core.essentials.tps");
        tps.exec(e -> {
            double[] tpsArr = Bukkit.getServer().getTPS();

            if (!e.isPlayer()) {
                String message = Main.helper.getMess("tpsCommand")
                        .replace("%tps1", tpsArr[0] + "")
                        .replace("%tps2", tpsArr[1] + "")
                        .replace("%tps3", tpsArr[2] + "");
                e.sender().sendMessage(message);
            } else {
                String message = Main.helper.getMess(e.player(), "tpsCommand", true)
                        .replace("%tps1", round(tpsArr[0], 2) + "")
                        .replace("%tps2", round(tpsArr[1], 2) + "")
                        .replace("%tps3", round(tpsArr[2], 2) + "");
                e.player().sendMessage(Component.text(message));
            }
        });

        SCommand vanish = new SCommand(plugin, "vanish", Main.helper);
        vanish.execPerm("core.essentials.vanish");
        vanish.playerOnly(true);
        vanish.exec(e -> {
            e.player().setInvisible(!e.player().isInvisible());
            if (e.player().isInvisible()) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "VanishNowActive", true)));
            } else {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "VanishNowDeactivated", true)));
            }
        });

        SCommand invsee = new SCommand(plugin, "invsee", Main.helper);
        invsee.execPerm("core.essentials.invsee");
        invsee.playerOnly(true);
        invsee.minArgs(1);
        invsee.exec(e -> {
            String playerName = e.args()[0];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "PlayerNotOnline", true)));
                return;
            }
            e.player().openInventory(target.getInventory());
        });

        SCommand ping = new SCommand(plugin, "ping", Main.helper);
        ping.execPerm("core.essentials.ping");
        ping.playerOnly(true);
        ping.exec(e -> {
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "pingMessage", true)
            .replace("%ping", e.player().getPing() + "")));
        });

        SCommand smite = new SCommand(plugin, "smite", Main.helper);
        smite.execPerm("core.essentials.smite");
        smite.playerOnly(true);
        smite.minArgs(1);
        smite.exec(e -> {
            String playerName = e.args()[0];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "PlayerNotOnline", true)));
                return;
            }
            target.getLocation().getWorld().strikeLightning(target.getLocation());
            e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "SmiteMessage", true)));
        });

        Main.commands.add(setSpawnCMD);
        Main.commands.add(spawnCMD);
        Main.commands.add(cbCMD);
        Main.commands.add(setHome);
        Main.commands.add(home);
        Main.commands.add(tpa);
        Main.commands.add(tpaccept);
        Main.commands.add(tps);
        Main.commands.add(vanish);
        Main.commands.add(invsee);
        Main.commands.add(ping);
        Main.commands.add(smite);
        Main.commands.add(eReload);
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
