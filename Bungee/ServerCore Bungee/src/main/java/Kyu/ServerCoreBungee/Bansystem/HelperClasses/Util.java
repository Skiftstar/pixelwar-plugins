package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.BanCMD;
import Kyu.ServerCoreBungee.Bansystem.BansHandler;
import Kyu.ServerCoreBungee.Bansystem.UnbanCMD;
import net.md_5.bungee.api.CommandSender;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            duration.append(array[i]).append(" ").append(Main.helper.getMess(language, nameArray[i]));
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
            duration.append(array[i]).append(" ").append(Main.helper.getMess(language, nameArray[i]));
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
            long banOn) {
        Connection conn = Main.getDb().getConnection();
        if (!bantime.getBanType().equals(BanType.KICK)) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO bans(uuid, banType, banReasonKey, bannedBy, bannedOn, unbanOn, banUUID) VALUES(?, ?, ?, ?, ?, ?, ?);")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, bantime.getBanType().toString());
                stmt.setString(3, reason);
                stmt.setString(4, banner);
                stmt.setLong(5, banOn);
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
            stmt.setLong(5, banOn);
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

    public static String getReason(String reason, CommandSender sender) {
        String reasonMess;
        if (reason.startsWith("CUSTOM_")) {
            reasonMess = reason.split("CUSTOM_")[1];
        } else if (reason.startsWith("CMB_")) {
            String reasons = "";
            for (String string : reason.split("CMB_")[1].split("\\+")) {
                if (string.startsWith("CUSTOM_"))
                    reasons += " + " + string.split("CUSTOM_")[1];
                else
                    reasons += " + " + Main.helper.getMess(sender, string);
            }
            reasons = reasons.replaceFirst(" \\+ ", "");
            reasonMess = reasons;
        } else {
            reasonMess = Main.helper.getMess(sender, reason);
        }
        return reasonMess;
    }

    public static String getReason(String reason, String language) {
        String reasonMess;
        if (reason.startsWith("CUSTOM_")) {
            reasonMess = reason.split("CUSTOM_")[1];
        } else if (reason.startsWith("CMB_")) {
            String reasons = "";
            for (String string : reason.split("CMB_")[1].split("\\+")) {
                if (string.startsWith("CUSTOM_"))
                    reasons += " + " + string.split("CUSTOM_")[1];
                else
                    reasons += " + " + Main.helper.getMess(language, string);
            }
            reasons = reasons.replaceFirst(" \\+ ", "");
            reasonMess = reasons;
        } else {
            reasonMess = Main.helper.getMess(language, reason);
        }
        return reasonMess;
    }

    public static Map<String, List<BanInfo>> fetchAllBans(String uuid) {
        Map<String, List<BanInfo>> bans = new HashMap<>();
        for (BanReason banreason : BanCMD.banReasons) {
            bans.put(banreason.getReason(), new ArrayList<>());
        }

        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM banlogs WHERE uuid = ? ORDER BY bannedOn ASC;")) {
            stmt.setString(1, uuid);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                BanInfo info;

                BanType banType = BanType.valueOf(resultSet.getString("banType"));
                String reasonKey = resultSet.getString("banReasonKey");
                String banUUID = resultSet.getString("banUUID");
                String bannedBy = uuidToName(resultSet.getString("bannedBy"));
                Date bannedOn = new Date(resultSet.getLong("bannedOn"));
                String playerUUID = resultSet.getString("uuid");
                String combinedInto = resultSet.getString("combinedIntoNew");

                long unbanLong = resultSet.getLong("unbanOn");
                boolean permanent = unbanLong == -1;
                unbanLong = permanent ? 0 : unbanLong;
                if (permanent) {
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn);
                } else {
                    Date unbanOn = new Date(unbanLong);
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn, unbanOn);
                }

                boolean earlyUnban = resultSet.getBoolean("earlyUnban");
                if (earlyUnban) {
                    String unbanBy = uuidToName(resultSet.getString("earlyUnbanByUUID"));
                    Date earlyUnbanOn = new Date(resultSet.getLong("earlyUnbanOn"));
                    info.setEarlyUnban(unbanBy, earlyUnbanOn);
                }

                if (combinedInto != null) {
                    info.setCombinedInto(combinedInto);
                }

                if (!bans.containsKey(reasonKey)) {
                    List<BanInfo> list = new ArrayList<>(Arrays.asList(info));
                    bans.put(reasonKey, list);
                } else {
                    bans.get(reasonKey).add(info);
                }
            }
            conn.close();
            return bans;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, List<BanInfo>> fetchActiveBans(String uuid) {
        Map<String, List<BanInfo>> bans = new HashMap<>();
        for (BanReason banreason : BanCMD.banReasons) {
            bans.put(banreason.getReason(), new ArrayList<>());
        }

        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM bans WHERE uuid = ? ORDER BY bannedOn ASC;")) {
            stmt.setString(1, uuid);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                BanInfo info;

                BanType banType = BanType.valueOf(resultSet.getString("banType"));
                String reasonKey = resultSet.getString("banReasonKey");
                String banUUID = resultSet.getString("banUUID");
                String bannedBy = uuidToName(resultSet.getString("bannedBy"));
                Date bannedOn = new Date(resultSet.getLong("bannedOn"));
                String playerUUID = resultSet.getString("uuid");

                long unbanLong = resultSet.getLong("unbanOn");
                boolean permanent = unbanLong == -1;
                unbanLong = permanent ? 0 : unbanLong;
                if (!permanent && unbanLong < System.currentTimeMillis())
                    continue;
                if (permanent) {
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn);
                } else {
                    Date unbanOn = new Date(unbanLong);
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn, unbanOn);
                }

                if (!bans.containsKey(reasonKey)) {
                    List<BanInfo> list = new ArrayList<>(Arrays.asList(info));
                    bans.put(reasonKey, list);
                } else {
                    bans.get(reasonKey).add(info);
                }
            }
            conn.close();
            return bans;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String uuidToName(String uuid) {
        if (Main.getUuidStorage().get(uuid) == null) {
            return "UUID NOT FOUND!";
        } else {
            return Main.getUuidStorage().getString(uuid);
        }
    }


    public static int[] stringToUnbanTime(String durationSt) {
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
        return new int[] { months, days, hours, minutes, seconds };
    }

    private static int convert(String s) {
        int index = s.length() - 1;
        for (int i = index; i >= 0; i--) {
            try {
                Integer.parseInt(Character.toString(s.charAt(i)));
                index = i;
            } catch (NumberFormatException e) {
                if (index == s.length() - 1)
                    return 0;
                break;
            }
        }
        return Integer.parseInt(s.substring(index));
    }

    public static Pair<Long, String> checkForActive(UUID uuid, String banType, BanTime bantime, String newBanUUID) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM bans WHERE uuid = ? AND banType = ?;")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, banType);
            ResultSet resultSet = stmt.executeQuery();

            long banLong = bantime.isPermanent() ? -1 : bantime.getUnbanDate().getTime();

            String newReasons = "";
            List<String> oldBans = new ArrayList<>();
            while (resultSet.next()) {
                System.out.println("Found ban");
                oldBans.add(resultSet.getString("banUUID"));
                String reason = resultSet.getString("banReasonKey");
                long unbanTime = resultSet.getLong("unbanOn");
                System.out.println(unbanTime);
                System.out.println(banLong);
                if (unbanTime < System.currentTimeMillis())
                    continue;
                if (unbanTime == -1 || bantime.isPermanent()) {
                    banLong = -1;
                } else {
                    banLong = banLong + (unbanTime - System.currentTimeMillis());
                }
                System.out.println(banLong);
                newReasons += "+" + reason.replace("CMB_", "");
            }
            stmt.close();

            if (oldBans.size() > 0) {
                BanType bantype = BanType.valueOf(banType);
                Util.clearBans(uuid, bantype, newBanUUID);
            }

            PreparedStatement stamt = null;
            for (String oldBanUUID : oldBans) {
                stamt = conn.prepareStatement("DELETE FROM bans WHERE banUUID = ?;");
                stamt.setString(1, oldBanUUID);
                stamt.execute();
                stamt.close();
                stamt = conn.prepareStatement("UPDATE banlogs SET combinedIntoNew = ? WHERE banUUID = ?");
                stamt.setString(1, newBanUUID);
                stamt.setString(2, oldBanUUID);
                stamt.executeUpdate();
                stamt.close();
            }
            conn.close();
            System.out.println(newReasons);
            return new Pair<>(banLong, newReasons);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Pair<>(bantime.getUnbanDate().getTime(), "");
        }
    }

    public static void main(String[] args) {
        String test = "1mo1";
        System.out.println("String: " + test.split("mo")[0]);
    }

}
