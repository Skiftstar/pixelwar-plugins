package Kyu.ServerCore.Bans;

import java.sql.Date;

public class Mute {

    private final String reason;
    private final Date unbanDate;
    private final boolean permanent;
    private String language = null; //Only intended for ban
    private String banUUID;

    public Mute(String reason, Date unbanDate, boolean permanent, String banUUID) {
        this.reason = reason;
        this.unbanDate = unbanDate;
        this.permanent = permanent;
        this.banUUID = banUUID;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public Date getUnbanDate() {
        return unbanDate;
    }

    public String getReason() {
        return reason;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public String getBanUUID() {
        return banUUID;
    }
}
