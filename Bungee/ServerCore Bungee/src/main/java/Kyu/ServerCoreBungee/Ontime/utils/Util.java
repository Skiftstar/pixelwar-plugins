package Kyu.ServerCoreBungee.Ontime.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import Kyu.ServerCoreBungee.Main;

public class Util {
    public static long getOntimeTotal(String uuid) {
        try {
            Connection conn = Main.getDb().getConnection();

            long var4;
            label79: {
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT playtimeTotal FROM player_playtime WHERE uuid = ?;");

                    label81: {
                        try {
                            stmt.setString(1, uuid);
                            ResultSet resultSet = stmt.executeQuery();
                            if (!resultSet.next()) {
                                var4 = -1L;
                                break label81;
                            }

                            var4 = resultSet.getLong("playtimeTotal");
                        } catch (Throwable var8) {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                }
                            }

                            throw var8;
                        }

                        if (stmt != null) {
                            stmt.close();
                        }
                        break label79;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var9) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (conn != null) {
                    conn.close();
                }

                return var4;
            }

            if (conn != null) {
                conn.close();
            }

            return var4;
        } catch (SQLException var10) {
            var10.printStackTrace();
            return -1L;
        }
    }

    public static long getDayOnTime(String uuid) {
        try {
            Connection conn = Main.getDb().getConnection();

            long var4;
            label79: {
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT playtimeDay FROM player_playtime WHERE uuid = ?;");

                    label81: {
                        try {
                            stmt.setString(1, uuid);
                            ResultSet resultSet = stmt.executeQuery();
                            if (!resultSet.next()) {
                                var4 = -1L;
                                break label81;
                            }

                            var4 = resultSet.getLong("playtimeDay");
                        } catch (Throwable var8) {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                }
                            }

                            throw var8;
                        }

                        if (stmt != null) {
                            stmt.close();
                        }
                        break label79;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var9) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (conn != null) {
                    conn.close();
                }

                return var4;
            }

            if (conn != null) {
                conn.close();
            }

            return var4;
        } catch (SQLException var10) {
            var10.printStackTrace();
            return -1L;
        }
    }

    public static long getWeekOnTime(String uuid) {
        try {
            Connection conn = Main.getDb().getConnection();

            long var4;
            label79: {
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT playtimeWeek FROM player_playtime WHERE uuid = ?;");

                    label81: {
                        try {
                            stmt.setString(1, uuid);
                            ResultSet resultSet = stmt.executeQuery();
                            if (!resultSet.next()) {
                                var4 = -1L;
                                break label81;
                            }

                            var4 = resultSet.getLong("playtimeWeek");
                        } catch (Throwable var8) {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                }
                            }

                            throw var8;
                        }

                        if (stmt != null) {
                            stmt.close();
                        }
                        break label79;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var9) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (conn != null) {
                    conn.close();
                }

                return var4;
            }

            if (conn != null) {
                conn.close();
            }

            return var4;
        } catch (SQLException var10) {
            var10.printStackTrace();
            return -1L;
        }
    }

    public static long getMonthOnTime(String uuid) {
        try {
            Connection conn = Main.getDb().getConnection();

            long var4;
            label79: {
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT playtimeMonth FROM player_playtime WHERE uuid = ?;");

                    label81: {
                        try {
                            stmt.setString(1, uuid);
                            ResultSet resultSet = stmt.executeQuery();
                            if (!resultSet.next()) {
                                var4 = -1L;
                                break label81;
                            }

                            var4 = resultSet.getLong("playtimeMonth");
                        } catch (Throwable var8) {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                }
                            }

                            throw var8;
                        }

                        if (stmt != null) {
                            stmt.close();
                        }
                        break label79;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var9) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (conn != null) {
                    conn.close();
                }

                return var4;
            }

            if (conn != null) {
                conn.close();
            }

            return var4;
        } catch (SQLException var10) {
            var10.printStackTrace();
            return -1L;
        }
    }

    public static void setLastUpdate(String uuid, long time) {
        try {
            PreparedStatement st = null;
            st = Main.getDb().getConnection().prepareStatement("UPDATE player_playtime SET lastUpdate = ? WHERE uuid = ?;");
            st.setLong(1, time);
            st.setString(2, uuid);
            st.execute();
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

    }

    public static long getLastUpdate(String uuid) {
        try {
            Connection conn = Main.getDb().getConnection();

            long var4;
            label79: {
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT lastUpdate FROM player_playtime WHERE uuid = ?;");

                    label81: {
                        try {
                            stmt.setString(1, uuid);
                            ResultSet resultSet = stmt.executeQuery();
                            if (!resultSet.next()) {
                                var4 = -1L;
                                break label81;
                            }

                            var4 = resultSet.getLong("lastUpdate");
                        } catch (Throwable var8) {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                }
                            }

                            throw var8;
                        }

                        if (stmt != null) {
                            stmt.close();
                        }
                        break label79;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var9) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (conn != null) {
                    conn.close();
                }

                return var4;
            }

            if (conn != null) {
                conn.close();
            }

            return var4;
        } catch (SQLException var10) {
            var10.printStackTrace();
            return -1L;
        }
    }

    public static void addPlaytime(String uuid) {
        long current = System.currentTimeMillis();
        long lastupdate = getLastUpdate(uuid);

        try {
            Connection conn = Main.getDb().getConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO player_playtime(uuid, playtimeTotal) VALUES(?, ?) ON DUPLICATE KEY UPDATE playtimeTotal = playtimeTotal + ?;");

                try {
                    stmt.setString(1, uuid);
                    stmt.setLong(2, current - lastupdate);
                    stmt.setLong(3, current - lastupdate);
                    tryToUpdateDay(uuid, lastupdate, current);
                    tryToUpdateWeek(uuid, lastupdate, current);
                    tryToUpdateMonth(uuid, lastupdate, current);
                    stmt.execute();
                } catch (Throwable var11) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var10) {
                            var11.addSuppressed(var10);
                        }
                    }

                    throw var11;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable var12) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                    }
                }

                throw var12;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var13) {
            var13.printStackTrace();
        }

    }

    public static void tryToUpdateDay(String uuid, long time1, long time2) {
        Connection conn;
        PreparedStatement stmt;
        if (isNewDay(time1, time2)) {
            try {
                conn = Main.getDb().getConnection();

                try {
                    stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?;");

                    try {
                        stmt.setLong(1, (long)(new Date(time2)).getMinutes() * 60L * 1000L);
                        stmt.setString(2, uuid);
                        stmt.execute();
                    } catch (Throwable var13) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable var12) {
                                var13.addSuppressed(var12);
                            }
                        }

                        throw var13;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var15) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var11) {
                            var15.addSuppressed(var11);
                        }
                    }

                    throw var15;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var16) {
                var16.printStackTrace();
                return;
            }
        } else {
            try {
                conn = Main.getDb().getConnection();

                try {
                    stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeDay = playtimeDay + ? WHERE uuid = ?;");

                    try {
                        stmt.setLong(1, time2 - time1);
                        stmt.setString(2, uuid);
                        stmt.execute();
                    } catch (Throwable var14) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable var10) {
                                var14.addSuppressed(var10);
                            }
                        }

                        throw var14;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var17) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var9) {
                            var17.addSuppressed(var9);
                        }
                    }

                    throw var17;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var18) {
                var18.printStackTrace();
                return;
            }
        }

    }

    public static void tryToUpdateWeek(String uuid, long time1, long time2) {
        Connection conn;
        PreparedStatement stmt;
        if (isNewWeek(time1, time2)) {
            try {
                conn = Main.getDb().getConnection();

                try {
                    stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeWeek = ? WHERE uuid = ?;");

                    try {
                        stmt.setLong(1, (long)(new Date(time2)).getMinutes() * 60L * 1000L);
                        stmt.setString(2, uuid);
                        stmt.execute();
                    } catch (Throwable var14) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable var12) {
                                var14.addSuppressed(var12);
                            }
                        }

                        throw var14;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var15) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var11) {
                            var15.addSuppressed(var11);
                        }
                    }

                    throw var15;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var16) {
                var16.printStackTrace();
                return;
            }
        } else {
            try {
                conn = Main.getDb().getConnection();

                try {
                    stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeWeek = playtimeWeek + ? WHERE uuid = ?;");

                    try {
                        stmt.setLong(1, time2 - time1);
                        stmt.setString(2, uuid);
                        stmt.execute();
                    } catch (Throwable var13) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable var10) {
                                var13.addSuppressed(var10);
                            }
                        }

                        throw var13;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var17) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var9) {
                            var17.addSuppressed(var9);
                        }
                    }

                    throw var17;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var18) {
                var18.printStackTrace();
                return;
            }
        }

    }

    public static void tryToUpdateMonth(String uuid, long time1, long time2) {
        Connection conn;
        PreparedStatement stmt;
        if (isNewMonth(time1, time2)) {
            try {
                conn = Main.getDb().getConnection();

                try {
                    stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeMonth = ?  WHERE uuid = ?;");

                    try {
                        stmt.setLong(1, (long)(new Date(time2)).getMinutes() * 60L * 1000L);
                        stmt.setString(2, uuid);
                        stmt.execute();
                    } catch (Throwable var13) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable var11) {
                                var13.addSuppressed(var11);
                            }
                        }

                        throw var13;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var15) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var10) {
                            var15.addSuppressed(var10);
                        }
                    }

                    throw var15;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var16) {
                var16.printStackTrace();
                return;
            }
        } else {
            try {
                conn = Main.getDb().getConnection();

                try {
                    stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeMonth = playtimeMonth + ?  WHERE uuid = ?;");

                    try {
                        stmt.setLong(1, time2 - time1);
                        stmt.setString(2, uuid);
                        stmt.execute();
                    } catch (Throwable var14) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable var12) {
                                var14.addSuppressed(var12);
                            }
                        }

                        throw var14;
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable var17) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var9) {
                            var17.addSuppressed(var9);
                        }
                    }

                    throw var17;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var18) {
                var18.printStackTrace();
                return;
            }
        }

    }

    public static void resetAndSetDayPlaytime(String uuid, long toset) {
        try {
            Connection conn = Main.getDb().getConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?");

                try {
                    stmt.setLong(1, toset);
                    stmt.setString(2, uuid);
                    stmt.execute();
                } catch (Throwable var9) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                    }

                    throw var9;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable var10) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }
                }

                throw var10;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var11) {
            var11.printStackTrace();
        }

    }

    public static void addPlaytimeDay(String uuid, long add) {
        try {
            Connection conn = Main.getDb().getConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement("UPDATE player_playtime SET playtimeDay = playtimeDay + ? WHERE uuid = ?;");

                try {
                    stmt.setLong(1, add);
                    stmt.setString(2, uuid);
                    stmt.execute();
                } catch (Throwable var9) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                    }

                    throw var9;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable var10) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }
                }

                throw var10;
            }

            if (conn != null) {
                conn.close();
            }

        } catch (SQLException var11) {
            var11.printStackTrace();
        }
    }

    public static void resetDayPlaytime(String uuid) {
        try {
            Connection conn = Main.getDb().getConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO player_playtime_day(uuid, time) VALUES(?, ?);");

                try {
                    stmt.setString(1, uuid);
                    stmt.setLong(2, 0L);
                    stmt.execute();
                } catch (Throwable var7) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable var8) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (conn != null) {
                conn.close();
            }

        } catch (SQLException var9) {
            var9.printStackTrace();
        }
    }

    public static void registerUser(String uuid) throws SQLException {
        PreparedStatement st = null;
        st = Main.getDb().getConnection().prepareStatement("INSERT INTO player_playtime (uuid, playtimeTotal, playtimeWeek, playtimeDay, playtimeMonth, lastUpdate) VALUES (?, ?, ?, ?, ?, ?)  ON DUPLICATE KEY UPDATE  uuid = ?;");
        st.setString(1, uuid);
        st.setLong(2, 0L);
        st.setLong(3, 0L);
        st.setLong(4, 0L);
        st.setLong(5, 0L);
        st.setLong(6, System.currentTimeMillis());
        st.setString(7, uuid);
        st.execute();
    }

    public static boolean isNewDay(long time1, long time2) {
        if (time1 > time2) {
            return false;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(time1));
            int year1 = cal.get(1);
            int month1 = cal.get(2);
            int day1 = cal.get(5);
            cal.setTime(new Date(time2));
            int year2 = cal.get(1);
            int month2 = cal.get(2);
            int day2 = cal.get(5);
            return year1 < year2 || month1 < month2 || day1 < day2;
        }
    }

    public static boolean isNewWeek(long time1, long time2) {
        if (time1 > time2) {
            return false;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(time1));
            int year1 = cal.get(1);
            int month1 = cal.get(2);
            int week1 = cal.get(4);
            cal.setTime(new Date(time2));
            int year2 = cal.get(1);
            int month2 = cal.get(2);
            int week2 = cal.get(4);
            return year1 < year2 || month1 < month2 || week1 < week2;
        }
    }

    public static boolean isNewMonth(long time1, long time2) {
        if (time1 > time2) {
            return false;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(time1));
            int year1 = cal.get(1);
            int month1 = cal.get(2);
            cal.setTime(new Date(time2));
            int year2 = cal.get(1);
            int month2 = cal.get(2);
            return year1 < year2 || month1 < month2;
        }
    }
}
