package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import java.sql.Date;

public class BanTime {

    private boolean permanent;
    private long banLength;
    private BanType banType;

    public BanTime(int months, int days, int hours, int minutes, int seconds, boolean permanent, BanType banType) {
        this.permanent = permanent;
        this.banType = banType;

        int tmpDays = days + (months * 30);
        long tmpHours = (long) (hours + (tmpDays * 24L));
        long tmpMinutes = (long) (minutes + (tmpHours * 60L));
        long tmpSeconds = (long) (seconds + (tmpMinutes * 60L));
        banLength = tmpSeconds * 1000;
    }

    public Date getUnbanDate() {
        return new Date(banLength + System.currentTimeMillis());
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public void setBanLength(long unbanOn) {
        this.banLength = unbanOn;
    }

    public long getBanLength() {
        return banLength;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public BanType getBanType() {
        return banType;
    }
}
