package Kyu.ServerCore.Bans;

import java.sql.Date;

public class BanTime {

    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final boolean permanent;

    public BanTime(int months, int days, int hours, int minutes, int seconds, boolean permanent) {
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.permanent = permanent;
    }

    public Date getUnbanDate() {
        int tmpDays = this.days + (this.months * 30);
        long tmpHours = (long) (this.hours + (tmpDays * 24L));
        long tmpMinutes = (long) (this.minutes + (tmpHours * 60L));
        long tmpSeconds = (long) (this.seconds + (tmpMinutes * 60L));
        long millis = tmpSeconds * 1000;
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
}
