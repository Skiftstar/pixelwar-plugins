package Kyu.ServerCore.Commands;

import Kyu.SCommand;
import Kyu.ServerCore.Main;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeCMD {


    public static void setup(Main plugin) {
        SCommand gamemodeCmd = new SCommand(plugin, "gamemode", Main.helper);
        Main.commands.add(gamemodeCmd);

        gamemodeCmd.execPerm("core.gamemode");
        gamemodeCmd.minArgs(1);
        gamemodeCmd.playerOnly(true);
        gamemodeCmd.exec(e -> {
            Player p = e.player();
            Player p2 = null;
            boolean setOtherPlayer = false;

            if (e.args().length >= 2) {
                p2 = Bukkit.getPlayer(e.args()[1]);
                if (p2 == null) {
                    p.sendMessage(Main.helper.getMess(p, "PlayerNotFound", true)
                            .replace("%player", e.args()[1]));
                    return;
                }
                if (!p.hasPermission("core.gamemode.others")) {
                    p.sendMessage(Main.helper.getMess(p, "NEPerms", true));
                    return;
                }
                setOtherPlayer = true;
            }

            GameMode mode;
            try {
                int i = Integer.parseInt(e.args()[0]);
                GameMode[] modes = new GameMode[]{GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR};
                if (i > modes.length - 1 || i < 0) {
                    p.sendMessage(Main.helper.getMess(p, "InvalidGamemode", true)
                            .replace("%mode", e.args()[0]));
                    return;
                }
                mode = modes[i];
            } catch (NumberFormatException ex) {
                try {
                    mode = GameMode.valueOf(e.args()[0].toUpperCase());
                } catch (Exception ex1) {
                    p.sendMessage(Main.helper.getMess(p, "InvalidGamemode", true)
                            .replace("%mode", e.args()[0]));
                    return;
                }
            }
            if (setOtherPlayer) {
                p2.setGameMode(mode);
                p2.sendMessage(Main.helper.getMess(p, "GameModeChanged", true).replace("%mode", mode.name()));
                p.sendMessage(Main.helper.getMess(p, "OtherGameModeChanged", true)
                        .replace("%mode", mode.name())
                        .replace("%player", ( (TextComponent) p2.displayName()).content()));
            } else {
                p.setGameMode(mode);
                p.sendMessage(Main.helper.getMess(p, "GameModeChanged", true).replace("%mode", mode.name()));
            }
        });
    }

}
