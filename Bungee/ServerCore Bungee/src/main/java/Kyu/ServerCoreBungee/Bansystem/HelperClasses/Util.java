package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.BansHandler;
import Kyu.ServerCoreBungee.Bansystem.UnbanCMD;
import Kyu.WaterFallLanguageHelper.LanguageHelper;

import java.sql.Date;
import java.util.UUID;

public class Util {

    public static String getRemainingTime(Date unbanDate, String language) {
        long millisDiff = unbanDate.getTime() - System.currentTimeMillis();

        System.out.println(millisDiff);

        int months = (int) (millisDiff / 1000 / 60 / 60 / 24 / 30);
        millisDiff -= (long) months * 1000 * 60 * 60 * 24 * 30;

        int days = (int) (millisDiff / 1000 / 60 / 60 / 24);
        millisDiff -= (long) days * 1000 * 60 * 60 * 24;

        long hours = (millisDiff / 1000 / 60 / 60);
        millisDiff -= hours * 1000 * 60 * 60;

        long minutes = (millisDiff / 1000 / 60);
        millisDiff -= minutes * 1000 * 60;

        long seconds = millisDiff / 1000;

        System.out.println(months + " " + days + " " + hours + " " + minutes + " " + seconds);

        long[] array = new long[]{months, days, hours, minutes, seconds};
        String[] nameArray = new String[]{"Months", "Days", "Hours", "Minutes", "Seconds"};
        int used = 0;
        StringBuilder duration = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) continue;
            duration.append(array[i]).append(" ").append(LanguageHelper.getMess(language, nameArray[i]));
            if (used == 0) duration.append(" ");
            else break;
            used++;
        }
        return duration.toString();
    }

    public static String getDateDiff(Date date1, Date date2, String language) {
        long millisDiff = date2.getTime() - date1.getTime();

        System.out.println(millisDiff);

        int months = (int) (millisDiff / 1000 / 60 / 60 / 24 / 30);
        millisDiff -= (long) months * 1000 * 60 * 60 * 24 * 30;

        int days = (int) (millisDiff / 1000 / 60 / 60 / 24);
        millisDiff -= (long) days * 1000 * 60 * 60 * 24;

        long hours = (millisDiff / 1000 / 60 / 60);
        millisDiff -= hours * 1000 * 60 * 60;

        long minutes = (millisDiff / 1000 / 60);
        millisDiff -= minutes * 1000 * 60;

        long seconds = millisDiff / 1000;

        System.out.println(months + " " + days + " " + hours + " " + minutes + " " + seconds);

        long[] array = new long[]{months, days, hours, minutes, seconds};
        String[] nameArray = new String[]{"Months", "Days", "Hours", "Minutes", "Seconds"};
        int used = 0;
        StringBuilder duration = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) continue;
            duration.append(array[i]).append(" ").append(LanguageHelper.getMess(language, nameArray[i]));
            if (used == 0) duration.append(" ");
            else break;
            used++;
        }
        return duration.toString();
    }

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static void clearBans(UUID pUUID, BanType banType, String banUUID) {
        switch (banType) {
            case BAN:
                BansHandler.bans.remove(pUUID);
                BansHandler.bansCache.remove(pUUID);
                break;
            case GCHAT_MUTE:
                BansHandler.gMuteds.remove(pUUID);
                BansHandler.gMutedsCache.remove(pUUID);
                break;
            case MUTE:
                if (Main.instance().getProxy().getPlayers().size() == 0) {
                    System.out.println("Couldn't send PluginChannel Message while clearing bans bc no player was online! Yikes.");
                } else {
                    UnbanCMD.sendCustomData(Main.instance().getProxy().getPlayers().iterator().next(), pUUID.toString(), banUUID);
                }
                BansHandler.gMuteds.remove(pUUID);
                BansHandler.gMutedsCache.remove(pUUID);
                break;
            case KICK:
                break;
        }
    }

}
