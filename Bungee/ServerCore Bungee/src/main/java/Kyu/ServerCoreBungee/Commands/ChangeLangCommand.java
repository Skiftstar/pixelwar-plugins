package Kyu.ServerCoreBungee.Commands;

import java.util.ArrayList;
import java.util.List;

import Kyu.ServerCoreBungee.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class ChangeLangCommand extends Command implements TabExecutor {
    public ChangeLangCommand(Main main) {
        super("lang", "bcore.lang");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(Main.helper.getMess("PlayerOnly")));
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;

        if (args.length < 1) {
            p.sendMessage(new TextComponent(Main.helper.getMess(p, "NEArgs")));
            return;
        }

        String newLang = args[0].toLowerCase();

        if (!Main.helper.getLanguages().contains(newLang)) {
            p.sendMessage(new TextComponent(Main.helper.getMess(p, "NoSuchLang")));
            return;
        }

        Main.helper.changeLang(p.getUniqueId(), newLang);
        p.sendMessage(new TextComponent(Main.helper.getMess(p, "ChangeLangSuccess").replace("%lang", newLang)));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        for (String lang : Main.helper.getLanguages()) {
            if (lang.toLowerCase().startsWith(args[0].toLowerCase())) names.add(lang);
        }
        return names;
    }

}
