package Kyu.Ontime.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Kyu.Ontime.Main;
import Kyu.Ontime.Util.Cache;
import Kyu.Ontime.Util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class OntimeCommand extends Command implements TabExecutor {
    
    public OntimeCommand(Main main) {
        super("ontime", "ontime.show.self");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    public void execute(CommandSender sender, String[] args)  {
        //Show own ontime
        if (args.length == 0) {
            if (!(sender instanceof ProxiedPlayer)) {
                return;
            }
            Main.logger().info("SHOWING SELF PLAYTIME");
            ProxiedPlayer player = (ProxiedPlayer) sender;
            String mess = buildResponse(sender, player.getUniqueId());
            sender.sendMessage(new TextComponent(mess));
            return;
        }
        //Show ontime from someone else
        String otherName = args[0].toLowerCase();

        if (Main.getUuidStorage().get(otherName) == null) {
            sender.sendMessage(new TextComponent(Main.helper.getMess(sender, "WrongPlayer")));
            return;
        }
        UUID otherUUID = UUID.fromString(Main.getUuidStorage().getString(otherName));

        if (!sender.hasPermission("ontime.show.other") && !sender.hasPermission("ontime.show.*")) {
            sender.sendMessage(new TextComponent(Main.helper.getMess(sender, "NoPermission")
                .replace("&","§")));
            return;
        }

        String mess = buildResponse(sender, otherUUID, args[0]);
        sender.sendMessage(new TextComponent(mess));
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> playerNames = new ArrayList<>();
        if (!sender.hasPermission("ontime.show.other")&&!sender.hasPermission("ontime.show.*")){
            return new ArrayList<>();
        }

        for (String key : Main.getUuidStorage().getKeys()) {
            if (key.equalsIgnoreCase("console")) continue;
            if (key.length() <= 16) {
                playerNames.add(Util.capitalize(key.toLowerCase()));
            }
        }

        List<String> fittingNames = new ArrayList<>();
        String currentarg = args[args.length-1].toLowerCase();
        for (String playerName : playerNames) {
            if (playerName.toLowerCase().startsWith(currentarg)){
                fittingNames.add(playerName);
            }
        }

        return fittingNames;
    }

    private static String buildResponse(CommandSender sender, UUID uuid, String... playerName) {
        if (!sender.hasPermission("ontime.show.self")) {
            return Main.helper.getMess(sender, "NoPermission")
                .replace("&","§ ");
        }

        String header = playerName.length > 0 ? 
            Main.helper.getMess(sender, "Header")
                .replace("&","§")
                .replace("(name)",  Util.capitalize(playerName[0]) ) : 
            Main.helper.getMess(sender, "HeaderAlt")
                .replace("&","§");

        Main.logger().info("FETCHING PLAYTIME");

        long[] playtimes = Cache.getPlaytimes(uuid);

        long dayTime = playtimes[0];
        long weekTime = playtimes[1];
        long monthTime = playtimes[2];
        long totalTime = playtimes[3];

        String dayString = Util.convertMillisToDHMS(dayTime, sender);
        String weekString = Util.convertMillisToDHMS(weekTime, sender);
        String monthString = Util.convertMillisToDHMS(monthTime, sender);
        String totalString = Util.convertMillisToDHMS(totalTime, sender);

        String mess = header + "\n" +
            Main.helper.getMess(sender, "Format")
                    .replace("(day)", dayString.trim())
                    .replace("(week)", weekString.trim())
                    .replace("(month)", monthString.trim())
                    .replace("(total)", totalString.trim())
                    .replace("&","§");


        return mess;
    }
}
