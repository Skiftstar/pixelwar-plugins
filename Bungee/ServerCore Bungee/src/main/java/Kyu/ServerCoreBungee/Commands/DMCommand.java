package Kyu.ServerCoreBungee.Commands;

import Kyu.ServerCoreBungee.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class DMCommand extends Command implements TabExecutor {
    public DMCommand(Main main) {
        super("dm", "bcore.dm", "msg", "tell");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(Main.helper.getMess("PlayerOnly")));
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (args.length < 2) {
            p.sendMessage(new TextComponent(Main.helper.getMess(p, "NEArguments")));
            return;
        }
        String p2Name = args[0];
        ProxiedPlayer receiver = ProxyServer.getInstance().getPlayer(p2Name);
        if (receiver == null) {
            p.sendMessage(new TextComponent(Main.helper.getMess(p, "PlayerNotFound")));
            return;
        }
        StringBuilder content = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            content.append(" ").append(args[i]);
        }
        p.sendMessage(new TextComponent(Main.helper.getMess(p, "DMTemplate")
                        .replace("%p1", Main.helper.getMess(p, "you"))
                        .replace("%p2", receiver.getDisplayName())
                        .replace("%mess", content.toString())));

        receiver.sendMessage(new TextComponent(Main.helper.getMess(receiver, "DMTemplate")
                .replace("%p2", Main.helper.getMess(receiver, "you"))
                .replace("%p1", p.getDisplayName())
                .replace("%mess", content.toString())));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if (p.getDisplayName().startsWith(args[0])) names.add(p.getDisplayName());
        }
        return names;
    }
}
