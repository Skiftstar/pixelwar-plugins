package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import java.sql.Date;

public class Ban {

    private final String reason;
    private final Date unbanDate;
    private final BanType type;
    private final boolean permanent;
    private String language = null; //Only intended for bans
    private String banUUID;

    public Ban(String reason, Date unbanDate, BanType type, boolean permanent, String banUUID) {
        this.reason = reason;
        this.unbanDate = unbanDate;
        this.type = type;
        this.permanent = permanent;
        this.banUUID = banUUID;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public BanType getType() {
        return type;
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
