package Kyu.Ontime.Util;

import java.util.Calendar;
import java.util.Date;

import Kyu.Ontime.Main;
import net.md_5.bungee.api.CommandSender;

public class Util {
    
    /**
     * Returns whether time1 is on a new day/week/month compared to time2
     * @param time1 older time
     * @param time2 newer time
     * @return a boolean array [isNewDay, isNewWeek, isNewMonth]
     */
    public static boolean[] dateComparison(long time1, long time2) {
        boolean isNewDay = false;
        boolean isNewWeek = false;
        boolean isNewMonth = false;
        if (time1 > time2) {
            return new boolean[]{isNewDay, isNewWeek, isNewMonth};
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(time1));
            int year1 = cal.get(Calendar.YEAR);
            int month1 = cal.get(Calendar.MONTH);
            int week1 = cal.get(Calendar.WEEK_OF_MONTH);
            int day1 = cal.get(Calendar.DAY_OF_MONTH);

            cal.setTime(new Date(time2));
            int year2 = cal.get(Calendar.YEAR);
            int month2 = cal.get(Calendar.MONTH);
            int week2 = cal.get(Calendar.WEEK_OF_MONTH);
            int day2 = cal.get(Calendar.DAY_OF_MONTH);
            
            isNewDay = year1 < year2 || month1 < month2 || day1 < day2;
            isNewWeek = year1 < year2 || month1 < month2 || week1 < week2;
            isNewMonth = year1 < year2 || month1 < month2;
            return new boolean[]{isNewDay, isNewWeek, isNewMonth};
        }
    }

    public static long getMillisFromCurrentDay(long currentMs) {
        long minutes = (int) (currentMs / (1000 * 60));

        long minutesSinceMidnight = minutes % (24 * 60);

        return minutesSinceMidnight * 60 * 1000;
    }

    public static String convertMillisToDHMS(long milliseconds, CommandSender sender) {
        int days = (int) (milliseconds / 1000 / 60 / 60 / 24);
        milliseconds -= (long) days * 1000 * 60 * 60 * 24;

        long hours = (milliseconds / 1000 / 60 / 60);
        milliseconds -= hours * 1000 * 60 * 60;

        long minutes = (milliseconds / 1000 / 60);

        String[] timeArray = new String[3];
        if (days > 0L) {
            timeArray[2] = days + " " + (days == 1L ? Main.helper.getMess(sender, "daysSing") : Main.helper.getMess(sender, "days"));
            hours %= 24L;
        }

        if (hours > 0L || days > 0L) {
            timeArray[1] = hours + " " + (hours == 1L ? Main.helper.getMess(sender, "hoursSing") : Main.helper.getMess(sender, "hours"));
            minutes %= 60L;
        }

        if (minutes == 0L && hours == 0L && days == 0L) {
            timeArray[0] = "0 " + Main.helper.getMess(sender, "minutes");
        } else if (minutes > 0L) {
            timeArray[0] = minutes + " " + (minutes == 1L ? Main.helper.getMess(sender, "minutesSing") : Main.helper.getMess(sender, "minutes"));
        }

        String result = "";
        for (int i = 2; i > -1; i--) {
            if (timeArray[i] != null) {
                result += timeArray[i] + " ";
            }
        }

        return result;
    }

    public static String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
