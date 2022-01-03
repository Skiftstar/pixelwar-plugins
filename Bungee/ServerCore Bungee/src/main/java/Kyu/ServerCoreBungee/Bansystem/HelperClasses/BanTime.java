package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import java.sql.Date;

public class BanTime {

    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final boolean permanent;
    private BanType banType;

    public BanTime(int months, int days, int hours, int minutes, int seconds, boolean permanent, BanType banType) {
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.permanent = permanent;
        this.banType = banType;
    }

    public Date getUnbanDate() {
        int tmpDays = this.days + (this.months * 30);
        long tmpHours = (long) (this.hours + (tmpDays * 24L));
        long tmpMinutes = (long) (this.minutes + (tmpHours * 60L));
        long tmpSeconds = (long) (this.seconds + (tmpMinutes * 60L));
        long millis = tmpSeconds * 1000;
        System.out.println(millis);
        return new Date(System.currentTimeMillis() + millis);
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public BanType getBanType() {
        return banType;
    }
}
