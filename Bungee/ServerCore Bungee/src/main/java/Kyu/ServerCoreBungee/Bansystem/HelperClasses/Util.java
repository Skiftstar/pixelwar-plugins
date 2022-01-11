package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.BansHandler;
import Kyu.ServerCoreBungee.Bansystem.UnbanCMD;
import Kyu.WaterFallLanguageHelper.LanguageHelper;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        long[] array = new long[] { months, days, hours, minutes, seconds };
        String[] nameArray = new String[] { "Months", "Days", "Hours", "Minutes", "Seconds" };
        int used = 0;
        StringBuilder duration = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0)
                continue;
            duration.append(array[i]).append(" ").append(LanguageHelper.getMess(language, nameArray[i]));
            if (used == 0)
                duration.append(" ");
            else
                break;
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

        long[] array = new long[] { months, days, hours, minutes, seconds };
        String[] nameArray = new String[] { "Months", "Days", "Hours", "Minutes", "Seconds" };
        int used = 0;
        StringBuilder duration = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0)
                continue;
            duration.append(array[i]).append(" ").append(LanguageHelper.getMess(language, nameArray[i]));
            if (used == 0)
                duration.append(" ");
            else
                break;
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
                    System.out.println(
                            "Couldn't send PluginChannel Message while clearing bans bc no player was online! Yikes.");
                } else {
                    UnbanCMD.sendCustomData(Main.instance().getProxy().getPlayers().iterator().next(), pUUID.toString(),
                            banUUID);
                }
                BansHandler.gMuteds.remove(pUUID);
                BansHandler.gMutedsCache.remove(pUUID);
                break;
            case KICK:
                break;
        }
    }

    public static boolean exists(String uuid) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM banlogs WHERE banUUID = ?;")) {
            stmt.setString(1, uuid);
            ResultSet resultSet = stmt.executeQuery();

            conn.close();
            if (resultSet.next())
                return true;
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void putInDB(UUID uuid, String banner, String reason, BanTime bantime, long unbanOn, String banUUID,
            long banTime) {
        Connection conn = Main.getDb().getConnection();
        if (!bantime.getBanType().equals(BanType.KICK)) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO bans(uuid, banType, banReasonKey, bannedBy, bannedOn, unbanOn, banUUID) VALUES(?, ?, ?, ?, ?, ?, ?);")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, bantime.getBanType().toString());
                stmt.setString(3, reason);
                stmt.setString(4, banner);
                stmt.setLong(5, banTime);
                stmt.setLong(6, unbanOn);
                stmt.setString(7, banUUID);
                stmt.execute();
            } catch (SQLException e) {
                Main.logger().warning("Something went wrong.");
                e.printStackTrace();
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO banlogs(uuid, banType, banReasonKey, bannedBy, bannedOn, unbanOn, banUUID, earlyUnban) VALUES(?, ?, ?, ?, ?, ?, ?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, bantime.getBanType().toString());
            stmt.setString(3, reason);
            stmt.setString(4, banner);
            stmt.setLong(5, banTime);
            stmt.setLong(6, unbanOn);
            stmt.setString(7, banUUID);
            stmt.setBoolean(8, false);
            stmt.execute();
        } catch (SQLException e) {
            Main.logger().warning("Something went wrong.");
            e.printStackTrace();
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static long stringToUnbanTime(String durationSt) {
        long unbanOn = 0;
        durationSt = durationSt.toLowerCase();
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        if (durationSt.contains("mo")) {
            months = convert(durationSt.split("mo")[0]);
        }
        if (durationSt.contains("d")) {
            days = convert(durationSt.split("d")[0]);
        }
        if (durationSt.contains("h")) {
            hours = convert(durationSt.split("h")[0]);
        }
        if (durationSt.contains("mi")) {
            minutes = convert(durationSt.split("mi")[0]);
        }
        if (durationSt.contains("s")) {
            seconds = convert(durationSt.split("s")[0]);
        }
        unbanOn = (seconds * 1000) + (minutes * 60 * 1000) + (hours * 60 * 60 * 1000) + (days * 24 * 60 * 60 * 1000) + (months * 30 * 24 * 60 * 60 * 1000) + System.currentTimeMillis();
        return unbanOn;
    }

    private static int convert(String s) {
        int index = s.length() - 1;
        for (int i = index; i >= 0; i--) {
            try {
                Integer.parseInt(Character.toString(s.charAt(i)));
                index = i;
            } catch (NumberFormatException e) {
                if (index == s.length() - 1) return 0;
            }
        }
        return Integer.parseInt(s.substring(index));
    }

    public static void main(String[] args) {
        String test = "1mo1";
        System.out.println("String: " + test.split("mo")[0]);
    }

}
