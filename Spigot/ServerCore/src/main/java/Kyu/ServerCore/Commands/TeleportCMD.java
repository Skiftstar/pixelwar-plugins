package Kyu.ServerCore.Commands;

import Kyu.SCommand;
import Kyu.ServerCore.Main;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportCMD {


    public static void setup(Main plugin) {
        SCommand teleportCmd = new SCommand(plugin, "teleport", Main.helper);
        Main.commands.add(teleportCmd);

        teleportCmd.execPerm("core.teleport");
        teleportCmd.minArgs(1);
        teleportCmd.playerOnly(true);
        teleportCmd.exec(e -> {
            Player p = e.player();

            if (e.args().length == 1) {
                Player p2 = Bukkit.getPlayer(e.args()[0]);
                if (p2 == null) {
                    p.sendMessage(Main.helper.getMess(p, "PlayerNotFound", true)
                            .replace("%player", e.args()[0]));
                    return;
                }
                p.teleport(p2);
                p.sendMessage(Main.helper.getMess(p, "Teleported", true)
                        .replace("%location", ((TextComponent) p2.displayName()).content()));
                p2.sendMessage(Main.helper.getMess(p2, "TeleportedToYou", true)
                        .replace("%player", ((TextComponent) p.displayName()).content()));
                return;
            }
            if (e.args().length == 2) {
                if (!p.hasPermission("core.teleport.others")) {
                    p.sendMessage(Main.helper.getMess(p, "NEPerms", true));
                    return;
                }

                Player sourcePlayer = Bukkit.getPlayer(e.args()[0]);
                if (sourcePlayer == null) {
                    p.sendMessage(Main.helper.getMess(p, "PlayerNotFound", true)
                            .replace("%player", e.args()[0]));
                    return;
                }
                Player targetPlayer = Bukkit.getPlayer(e.args()[1]);
                if (targetPlayer == null) {
                    p.sendMessage(Main.helper.getMess(p, "PlayerNotFound", true)
                            .replace("%player", e.args()[1]));
                    return;
                }
                sourcePlayer.teleport(targetPlayer);
                sourcePlayer.sendMessage(Main.helper.getMess(sourcePlayer, "SbTeleportedYou", true)
                        .replace("%player", ((TextComponent) p.displayName()).content())
                        .replace("%location", ((TextComponent) targetPlayer.displayName()).content()));
                targetPlayer.sendMessage(Main.helper.getMess(targetPlayer, "SbTeleportedToYou", true)
                        .replace("%player1", ((TextComponent) p.displayName()).content())
                        .replace("%player2", ((TextComponent) sourcePlayer.displayName()).content()));
                p.sendMessage(Main.helper.getMess(p, "YouTeleportedSb", true)
                        .replace("%player", ((TextComponent) sourcePlayer.displayName()).content())
                        .replace("%location", ((TextComponent) targetPlayer.displayName()).content()));
                return;
            }
            if (e.args().length == 3) {
                try {
                    int x = Integer.parseInt(e.args()[0]);
                    int y = Integer.parseInt(e.args()[1]);
                    int z = Integer.parseInt(e.args()[2]);
                    p.teleport(new Location(p.getWorld(), x, y, z));
                    p.sendMessage(Main.helper.getMess(p, "Teleported", true)
                            .replace("%location", x + " " + y + " " + z));
                    return;
                } catch (NumberFormatException ex) {
                    p.sendMessage(Main.helper.getMess(p, "InvalidCoordinates", true)
                            .replace("%cords", e.args()[0] + " " + e.args()[1] + " " + e.args()[2]));
                    return;
                }
            }

            if (e.args().length >= 4) {
                if (!p.hasPermission("core.teleport.others")) {
                    p.sendMessage(Main.helper.getMess(p, "NEPerms", true));
                    return;
                }

                Player p2 = Bukkit.getPlayer(e.args()[0]);
                if (p2 == null) {
                    p.sendMessage(Main.helper.getMess(p, "PlayerNotFound", true)
                            .replace("%player", e.args()[0]));
                    return;
                }
                try {
                    int x = Integer.parseInt(e.args()[1]);
                    int y = Integer.parseInt(e.args()[2]);
                    int z = Integer.parseInt(e.args()[3]);
                    p2.teleport(new Location(p.getWorld(), x, y, z));
                    p.sendMessage(Main.helper.getMess(p, "YouTeleportedSb", true)
                            .replace("%player", ((TextComponent) p2.displayName()).content())
                            .replace("%location", x + " " + y + " " + z));
                    p2.sendMessage(Main.helper.getMess(p2, "SbTeleportedYou", true)
                            .replace("%player", ((TextComponent) p.displayName()).content())
                            .replace("%location", x + " " + y + " " + z));
                } catch (NumberFormatException ex) {
                    p.sendMessage(Main.helper.getMess(p, "InvalidCoordinates", true)
                            .replace("%cords", e.args()[1] + " " + e.args()[2] + " " + e.args()[3]));
                }
            }
        });
    }

}
