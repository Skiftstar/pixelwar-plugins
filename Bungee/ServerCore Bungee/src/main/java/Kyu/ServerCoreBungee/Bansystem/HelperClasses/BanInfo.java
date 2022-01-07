package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import java.sql.Date;

public class BanInfo {

    private String bannedBy = null, earlyUnbanBy = null, banUUID = null, playerUUID = null;
    private Date banExpireOn = null, banOn = null, earlyUnbanOn = null;
    private boolean earlyUnban = false, permanent = false;
    private BanType bantype;
    
    public BanInfo(String banUUID, String playerUUID, BanType bantype, String bannedBy, Date banOn) {
        this.banUUID = banUUID;
        this.bantype = bantype;
        this.playerUUID = playerUUID;
        this.bannedBy = bannedBy;
        this.banOn = banOn;
        this.permanent = true;
    }

    public BanInfo(String banUUID, String playerUUID, BanType bantype, String bannedBy, Date banOn, Date banExpireOn) {
        this.bantype = bantype;
        this.banUUID = banUUID;
        this.playerUUID = playerUUID;
        this.bannedBy = bannedBy;
        this.banOn = banOn;
        this.banExpireOn = banExpireOn;
    }

    public void setEarlyUnban(String earlyUnbanBy, Date earlyUnbanOn) {
        this.earlyUnban = true;
        this.earlyUnbanBy = earlyUnbanBy;
        this.earlyUnbanOn = earlyUnbanOn;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public String getEarlyUnbanBy() {
        return earlyUnbanBy;
    }

    public boolean isEarlyUnban() {
        return earlyUnban;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public Date getEarlyUnbanOn() {
        return earlyUnbanOn;
    }

    public Date getBanExpireOn() {
        return banExpireOn;
    }

    public Date getBanOn() {
        return banOn;
    }

    public String getBanUUID() {
        return banUUID;
    }

    public BanType getBantype() {
        return bantype;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }
}
