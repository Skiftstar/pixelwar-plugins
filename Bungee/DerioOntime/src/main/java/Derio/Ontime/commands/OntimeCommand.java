package Derio.Ontime.commands;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import Derio.Ontime.Main;
import Derio.Ontime.utils.Cache;
import Derio.Ontime.utils.LangFiles;
import Derio.Ontime.utils.PlayerData;
import Derio.Ontime.utils.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class OntimeCommand extends Command implements TabExecutor {
    static String locale = "en";

    public OntimeCommand(Main main) {
        super("ontime", "ontime.show.self");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    public void execute(CommandSender cs, String[] args)  {


        if (args.length == 0) {
            if (!(cs instanceof ProxiedPlayer)) {
                return;
            }
            ProxiedPlayer sender = (ProxiedPlayer)cs;



            String mess = buildResponse(cs,sender.getUniqueId().toString());
            sender.sendMessage(new TextComponent(mess));

        } else {
            args[0] = args[0].toLowerCase();

            PlayerData data;
            data = Cache.data;
            LangFiles lang = Cache.lang;

            String otherUUID = data.getUUID(args[0].toLowerCase());
            if (otherUUID == null) {
                cs.sendMessage(new TextComponent(lang.getMessage(locale, "Ontime.WrongPlayer").replace("&","§")));
                return;
            }

            if (!cs.hasPermission("ontime.show.other")) {
                cs.sendMessage(new TextComponent(lang.getMessage(locale, "Ontime.NoPermission").replace("&","§")));
                return;
            }

            String mess = buildResponse(cs, otherUUID, args[0]);
            cs.sendMessage(new TextComponent(mess));
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        PlayerData data = Cache.data;

        for (String s : data.getListKeys()) {

            if (!s.equalsIgnoreCase("console")){
                if (s.length()<=16){
                    list.add(capitalize(s.toLowerCase()));
                }
            }
        }

        return list;
    }

    private static String buildResponse(CommandSender cs,String uuid, String... playerName) {
         locale = "en";
        LangFiles lang = Cache.lang;

        if (cs instanceof ProxiedPlayer){
            locale = Util.getLocale(((ProxiedPlayer) cs).getUniqueId().toString());
            if (locale == null){
                locale = "en";
            }
        }
        if (!cs.hasPermission("ontime.show.self")) {
            return lang.getMessage(locale, "Ontime.NoPermission").replace("&","§");
        }

        String header = playerName.length > 0 ? lang.getMessage(locale,"Ontime.Header").replace("&","§").replace("(name)",  capitalize(playerName[0]) ): lang.getMessage(locale, "Ontime.HeaderAlt").replace("&","§");

        long[] playtimes = getPlaytime(uuid);
        System.out.println(uuid);
        long dayTime = playtimes[0];
        long weekTime = playtimes[1];
        long monthTime = playtimes[2];
        long totalTime = playtimes[3];


        String dayString = convertMillisToDHMS(dayTime);
        String weekString = convertMillisToDHMS(weekTime);
        String monthString = convertMillisToDHMS(monthTime);
        String totalString = convertMillisToDHMS(totalTime);
        String mess = header + "\n" +
            lang.getMessage(locale, "Ontime.Format")
                    .replace("(day)", dayString.trim())
                    .replace("(week)", weekString.trim())
                    .replace("(month)", weekString.trim())
                    .replace("(total)", totalString.trim())
                    .replace("&","§");


        return mess;
    }

    public static String convertMillisToDHMS(long milliseconds) {
        LangFiles lang = Cache.lang;

        long seconds = milliseconds / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        String[] timeArray = new String[3];
        if (days > 0L) {
            timeArray[0] = "" + days + " "+ lang.getMessage(locale, "Ontime.words.days");
            hours %= 24L;
        }

        if (hours > 0L || days > 0L) {
            timeArray[1] = "" + hours + " "+lang.getMessage(locale, "Ontime.words.hours");
            minutes %= 60L;
        }

        if (minutes == 0L && hours == 0L && days == 0L) {
            timeArray[0] = "0 "+lang.getMessage(locale, "Ontime.words.minutes");
        } else if (minutes > 0L) {
            timeArray[0] = "" + minutes + " "+lang.getMessage(locale, "Ontime.words.minutes");
        }

        String result = "";

        for (int i = 0; i < 3; i++) {
            if (timeArray[i] != null) {
                result += timeArray[i] + " ";
            }
        }

        return result;
    }
    private static long[] getPlaytime(String uuid) {


            long[] result = new long[]{0, 0, 0, 0};
    try {



        result[0] = System.currentTimeMillis()-Cache.lastLogin.get(uuid) + Cache.playtimeDay.get(uuid);
        result[1] = System.currentTimeMillis()-Cache.lastLogin.get(uuid) + Cache.playtimeWeek.get(uuid);
        result[2] = System.currentTimeMillis()-Cache.lastLogin.get(uuid) + Cache.playtimeMonth.get(uuid);
        result[3] = System.currentTimeMillis()-Cache.lastLogin.get(uuid) + Cache.playtimeTotal.get(uuid);

    }catch (NullPointerException ex){
        long[] cache = Util.getPlaytime(uuid);
        long lastLogin = Util.getLastUpdate(uuid);

        Cache.playtimeDay.put(uuid, cache[0]);
        Cache.playtimeWeek.put(uuid,cache[1]);
        Cache.playtimeMonth.put(uuid,cache[2]);
        Cache.playtimeTotal.put(uuid,cache[3]);
        Cache.lastLogin.put(uuid, lastLogin);

        result[0] = Cache.playtimeDay.get(uuid);
        result[1] = Cache.playtimeWeek.get(uuid);
        result[2] = Cache.playtimeMonth.get(uuid);
        result[3] = Cache.playtimeTotal.get(uuid);

    }


            return result;
    }
    private static String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}