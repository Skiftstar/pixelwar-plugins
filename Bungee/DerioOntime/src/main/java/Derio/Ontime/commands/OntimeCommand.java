package Derio.Ontime.commands;

import java.util.*;
import Derio.Ontime.Main;
import Derio.Ontime.utils.Cache;
import Derio.Ontime.utils.LangFiles;
import Derio.Ontime.utils.PlayerData;
import Derio.Ontime.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
            if (otherUUID.isBlank()) {
                cs.sendMessage(new TextComponent(lang.getMessage(locale, "Ontime.WrongPlayer").replace("&","§")));
                return;
            }

            if (!cs.hasPermission("ontime.show.other")&&!cs.hasPermission("ontime.show.*")) {
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
        if (!sender.hasPermission("ontime.show.other")&&!sender.hasPermission("ontime.show.*")){
            return new ArrayList<>();
        }

        for (String s : data.getListKeys()) {

            if (!s.equalsIgnoreCase("console")){
                if (s.length()<=16){
                    list.add(capitalize(s.toLowerCase()));
                }
            }
        }
        List<String> endlist = new ArrayList<>();
        String currentarg = args[args.length-1].toLowerCase();
        for (String s : list) {
            String s1 = s.toLowerCase();
            if (s1.startsWith(currentarg)){
                endlist.add(s);
            }
        }

        return endlist;
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
            return lang.getMessage(locale, "Ontime.NoPermission").replace("&","§ ");
        }

        String header = playerName.length > 0 ? lang.getMessage(locale,"Ontime.Header").replace("&","§").replace("(name)",  capitalize(playerName[0]) ): lang.getMessage(locale, "Ontime.HeaderAlt").replace("&","§");

        long[] playtimes = getPlaytime(uuid);

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
                    .replace("(month)", monthString.trim())
                    .replace("(total)", totalString.trim())
                    .replace("&","§");


        return mess;
    }

    public static String convertMillisToDHMS(long milliseconds) {
        LangFiles lang = Cache.lang;

        int days = (int) (milliseconds / 1000 / 60 / 60 / 24);
        milliseconds -= (long) days * 1000 * 60 * 60 * 24;

        long hours = (milliseconds / 1000 / 60 / 60);
        milliseconds -= hours * 1000 * 60 * 60;

        long minutes = (milliseconds / 1000 / 60);
        milliseconds -= minutes * 1000 * 60;

        // long seconds = milliseconds / 1000;

        String[] timeArray = new String[3];
        if (days > 0L) {
            timeArray[2] = "" + days + " "+ lang.getMessage(locale, "Ontime.words.days");
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
        for (int i = 2; i > -1; i--) {
            if (timeArray[i] != null) {
                result += timeArray[i] + " ";
            }
        }

        return result;
    }

    private static boolean isOnline(UUID uuid) {
        ProxyServer server = ProxyServer.getInstance();
        if (server.getPlayer(uuid) != null && server.getPlayer(uuid).isConnected()) return true;
        return false;
    }

    private static long[] getPlaytime(String uuid) {
        long[] result = new long[]{0, 0, 0, 0};
        long current = System.currentTimeMillis();
        long lastLoginCached = Cache.lastLogin.getOrDefault(uuid, -1L);

        if (lastLoginCached == -1) {
            long[] database = Util.getPlaytime(uuid);
            long lastLogin = Util.getLastUpdate(uuid);

            Cache.playtimeDay.put(uuid, database[0]);
            Cache.playtimeWeek.put(uuid, database[1]);
            Cache.playtimeMonth.put(uuid, database[2]);
            Cache.playtimeTotal.put(uuid, database[3]);
            Cache.lastLogin.put(uuid, lastLogin);
        }

        if (isNewMonth(Cache.lastLogin.get(uuid), current)){
            if (isOnline(UUID.fromString(uuid))) {
                long playtimeM = getMinutesFromCurrentDay(System.currentTimeMillis());
                long playtimeMs = playtimeM*60*1000;
                result[0] = playtimeMs;
                result[1] = playtimeMs;
                result[2] = playtimeMs;
                result[3] = current-Cache.lastLogin.get(uuid) + Cache.playtimeTotal.get(uuid);
                return result;
            }

            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
            result[3] = Cache.playtimeTotal.get(uuid);
            return result;
        } else if (isNewWeek(Cache.lastLogin.get(uuid), current)) {
            if (isOnline(UUID.fromString(uuid))) {
                long playtimeM =  getMinutesFromCurrentDay(System.currentTimeMillis());
                long playtimeMs = playtimeM*60*1000;
                result[0] = playtimeMs;
                result[1] = playtimeMs;
                result[2] = Cache.playtimeMonth.get(uuid);
                result[3] = Cache.playtimeTotal.get(uuid);
                return result;
            }

            result[0] =  0;
            result[1] = 0;
            result[2] = current-Cache.lastLogin.get(uuid) + Cache.playtimeMonth.get(uuid);
            result[3] = current-Cache.lastLogin.get(uuid) + Cache.playtimeTotal.get(uuid);
            return result;

        } else if (isNewDay(Cache.lastLogin.get(uuid), current)) {
            if (isOnline(UUID.fromString(uuid))) {
                long playtimeM =  getMinutesFromCurrentDay(System.currentTimeMillis());
                long playtimeMs = playtimeM*60*1000;
                result[0] = playtimeMs;
                result[1] = current-Cache.lastLogin.get(uuid) + Cache.playtimeWeek.get(uuid);
                result[2] = current-Cache.lastLogin.get(uuid) + Cache.playtimeMonth.get(uuid);
                result[3] = current-Cache.lastLogin.get(uuid) + Cache.playtimeTotal.get(uuid);
                return result;
            }
            result[0] =  0;
            result[1] = Cache.playtimeWeek.get(uuid);
            result[2] = Cache.playtimeMonth.get(uuid);
            result[3] = Cache.playtimeTotal.get(uuid);
            return result;

        } else {
            if (isOnline(UUID.fromString(uuid))) {
                result[0] = current - Cache.lastLogin.get(uuid) + Cache.playtimeDay.get(uuid);
                result[1] = current - Cache.lastLogin.get(uuid) + Cache.playtimeWeek.get(uuid);
                result[2] = current - Cache.lastLogin.get(uuid) + Cache.playtimeMonth.get(uuid);
                result[3] = current - Cache.lastLogin.get(uuid) + Cache.playtimeTotal.get(uuid);

                return result;
            }

            result[0] =  Cache.playtimeDay.get(uuid);
            result[1] = Cache.playtimeWeek.get(uuid);
            result[2] = Cache.playtimeMonth.get(uuid);
            result[3] = Cache.playtimeTotal.get(uuid);

            return result;
        }
    }


    private static String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    private static boolean isNewDay(long time1, long time2) {
        if (time1 > time2) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date(time1));

        int year1 = cal.get(Calendar.YEAR);
        int month1 = cal.get(Calendar.MONTH);
        int day1 = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(new java.util.Date(time2));
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);

        return year1 < year2 || month1 < month2 || day1 < day2;
    }

    private static boolean isNewWeek(long time1, long time2) {
        if (time1 > time2) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date(time1));

        int year1 = cal.get(Calendar.YEAR);
        int month1 = cal.get(Calendar.MONTH);
        int week1 = cal.get(Calendar.WEEK_OF_MONTH);
        cal.setTime(new java.util.Date(time2));
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int week2 = cal.get(Calendar.WEEK_OF_MONTH);

        return year1 < year2 || month1 < month2 || week1 < week2;
    }

    private static boolean isNewMonth(long time1, long time2) {
        if (time1 > time2) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date(time1));

        int year1 = cal.get(Calendar.YEAR);
        int month1 = cal.get(Calendar.MONTH);
        cal.setTime(new java.util.Date(time2));
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);

        return year1 < year2 || month1 < month2;
    }
    public static long getMinutesFromCurrentDay(long currentMs) {
        long minutes = (int) (currentMs / (1000 * 60));

        long minutesSinceMidnight = minutes % (24 * 60);

        return minutesSinceMidnight;
    }
}