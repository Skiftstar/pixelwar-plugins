package Kyu.ServerCoreBungee.Ontime.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import Kyu.ServerCoreBungee.Main;

public class Util {
    public static long[] getPlaytime(String uuid) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("SELECT * FROM player_playtime WHERE uuid = ?;");) {
            stmt.setString(1, uuid);

            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return new long[]{-1, -1, -1, -1};
            }

            long[] result = new long[]{0, 0, 0, 0};

            result[0] = resultSet.getLong("playtimeDay");
            result[1] = resultSet.getLong("playtimeWeek");
            result[2] = resultSet.getLong("playtimeMonth");
            result[3] = resultSet.getLong("playtimeTotal");

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{-1, -1, -1, -1};
        }
    }

    public static void setLastUpdate(String uuid, long time) {
        try (PreparedStatement st = Main.getDb().getConnection().prepareStatement("UPDATE player_playtime SET lastUpdate = ? WHERE uuid = ?;");) {
            st.setLong(1, time);
            st.setString(2, uuid);
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static long getLastUpdate(String uuid) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("SELECT lastUpdate FROM player_playtime WHERE uuid = ?;");) {
            stmt.setString(1, uuid);

            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return -1L;
            }

            return resultSet.getLong("lastUpdate");
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public static void addPlaytime(String uuid) {
        long current = System.currentTimeMillis();
        long lastupdate = getLastUpdate(uuid);

        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("INSERT INTO player_playtime(uuid, playtimeTotal) VALUES(?, ?) ON DUPLICATE KEY UPDATE playtimeTotal = playtimeTotal + ?;");) {
            stmt.setString(1, uuid);
            stmt.setLong(2, current - lastupdate);
            stmt.setLong(3, current - lastupdate);

            boolean[] dateChecks = dateComparison(lastupdate, current);
            tryToUpdateDay(uuid, lastupdate, current, dateChecks[0]);
            tryToUpdateWeek(uuid, lastupdate, current, dateChecks[1]);
            tryToUpdateMonth(uuid, lastupdate, current, dateChecks[2]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void tryToUpdateDay(String uuid, long time1, long time2, boolean isNewDay) {
        String query;
        long time;
        if (isNewDay) {
            query = "UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?;";
            time = (long)(new Date(time2)).getMinutes() * 60L * 1000L;
        } else {
            query = "UPDATE player_playtime SET playtimeDay = playtimeDay + ? WHERE uuid = ?;";
            time = time2 - time1;
        }
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement(query);) {
            stmt.setLong(1, time);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void tryToUpdateWeek(String uuid, long time1, long time2, boolean isNewWeek) {
        String query;
        long time;
        if (isNewWeek) {
            query = "UPDATE player_playtime SET playtimeWeek = ? WHERE uuid = ?;";
            time = (long)(new Date(time2)).getMinutes() * 60L * 1000L;
        } else {
            query = "UPDATE player_playtime SET playtimeWeek = playtimeWeek + ? WHERE uuid = ?;";
            time = time2 - time1;
        }
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement(query);) {
            stmt.setLong(1, time);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void tryToUpdateMonth(String uuid, long time1, long time2, boolean isNewMonth) {
        String query;
        long time;
        if (isNewMonth) {
            query = "UPDATE player_playtime SET playtimeMonth = ? WHERE uuid = ?;";
            time = (long)(new Date(time2)).getMinutes() * 60L * 1000L;
        } else {
            query = "UPDATE player_playtime SET playtimeMonth = playtimeMonth + ? WHERE uuid = ?;";
            time = time2 - time1;
        }
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement(query);) {
            stmt.setLong(1, time);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void resetAndSetDayPlaytime(String uuid, long toset) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?");) {
            stmt.setLong(1, toset);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void addPlaytimeDay(String uuid, long add) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("UPDATE player_playtime SET playtimeDay = playtimeDay + ? WHERE uuid = ?;");) {
            stmt.setLong(1, add);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException var11) {
            var11.printStackTrace();
        }
    }

    public static void resetDayPlaytime(String uuid) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("INSERT INTO player_playtime_day(uuid, time) VALUES(?, ?);");) {
            stmt.setString(1, uuid);
            stmt.setLong(2, 0L);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void registerUser(String uuid) {
        try (PreparedStatement st = Main.getDb().getConnection().prepareStatement("INSERT INTO player_playtime (uuid, playtimeTotal, playtimeWeek, playtimeDay, playtimeMonth, lastUpdate) VALUES (?, ?, ?, ?, ?, ?)  ON DUPLICATE KEY UPDATE  uuid = ?;");) {
            st.setString(1, uuid);
            st.setLong(2, 0L);
            st.setLong(3, 0L);
            st.setLong(4, 0L);
            st.setLong(5, 0L);
            st.setLong(6, System.currentTimeMillis());
            st.setString(7, uuid);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
}
