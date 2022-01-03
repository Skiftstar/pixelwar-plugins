package Kyu.ServerCore.Commands;

import Kyu.SCommand;
import Kyu.ServerCore.Main;
import Kyu.ServerCore.TabAndScoreboard.ScoreboardManager;

public class SmallCommands {

    public static void init(Main plugin) {
        SCommand discordCmd = new SCommand(plugin, "discord", Main.helper);
        discordCmd.playerOnly(true);
        discordCmd.exec(e -> {
            e.player().sendMessage(Main.helper.getMess(e.player(), "DiscordMessage")
                    .replace("%link", Main.discordLink));
        });

        SCommand reloadCmd = new SCommand(plugin, "sReload", Main.helper);
        reloadCmd.execPerm("core.reload");
        reloadCmd.exec(e -> {
            String message;
            plugin.loadConfigValues();
            ScoreboardManager.reloadSetup();
            if (e.isPlayer()) {
                message = Main.helper.getMess(e.player(), "ReloadDone", true);
            } else {
                message = Main.helper.getMess("ReloadDone");
            }
            e.sender().sendMessage(message);
        });

        Main.commands.add(discordCmd);
        Main.commands.add(reloadCmd);
    }

}
