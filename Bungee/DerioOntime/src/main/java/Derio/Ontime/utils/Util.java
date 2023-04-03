package Derio.Ontime.utils;

import Derio.Ontime.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

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

    public static void tryReset(String uuid){

        long current = System.currentTimeMillis();
        long lastupdate = getLastUpdate(uuid);

        boolean[] dateChecks = dateComparison(lastupdate, current);
        boolean isNewDay = dateChecks[0];
        boolean isNewWeek = dateChecks[1];
        boolean isNewMonth = dateChecks[2];

        String query = "";
        String query1 = "";
        String query2 = "";
        if (isNewMonth) {
            query = "UPDATE player_playtime SET playtimeMonth = ? WHERE uuid = ?;";
            query1 = "UPDATE player_playtime SET playtimeWeek = ? WHERE uuid = ?;";
            query2 = "UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?;";
            execute(uuid, query,query1, query2);

        }else if (isNewWeek){
            query = "UPDATE player_playtime SET playtimeWeek = ? WHERE uuid = ?;";
            query1 = "UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?;";
            execute(uuid, query,query1);

        } else if (isNewDay){
            query = "UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?;";
            execute(uuid, query);
        }
    }

    private static void execute(String uuid,String... querys){
        for (int i = 0; i < querys.length; i++) {
            try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement(querys[i]);) {
                stmt.setLong(1, 0);
                stmt.setString(2, uuid);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
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
            setLastUpdate(uuid, current);


            stmt.executeUpdate();
    try {
        Cache.playtimeTotal.put(uuid, Cache.playtimeTotal.get(uuid)+ current-lastupdate);
    }catch (NullPointerException ex){
        Cache.playtimeTotal.put(uuid, current-lastupdate);
    }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void tryToUpdateDay(String uuid, long time1, long time2, boolean isNewDay) {
        String query;
        long time;
        if (isNewDay) {
            query = "UPDATE player_playtime SET playtimeDay = ? WHERE uuid = ?;";
            time =  getMinutesFromCurrentDay(time2);
            Cache.playtimeDay.put(uuid,   getMinutesFromCurrentDay(time2));
        } else {
            query = "UPDATE player_playtime SET playtimeDay = playtimeDay + ? WHERE uuid = ?;";
            time = time2 - time1;
            Cache.playtimeDay.put(uuid, Cache.playtimeDay.getOrDefault(uuid, 0L) + time);
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
            time =  getMinutesFromCurrentDay(time2);
            Cache.playtimeWeek.put(uuid,  getMinutesFromCurrentDay(time2));
        } else {
            query = "UPDATE player_playtime SET playtimeWeek = playtimeWeek + ? WHERE uuid = ?;";
            time = time2 - time1;
            Cache.playtimeWeek.put(uuid, Cache.playtimeDay.getOrDefault(uuid, 0L) + time);
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
            time = getMinutesFromCurrentDay(time2);
            Cache.playtimeMonth.put(uuid,  getMinutesFromCurrentDay(time2));
        } else {
            query = "UPDATE player_playtime SET playtimeMonth = playtimeMonth + ? WHERE uuid = ?;";
            time = time2 - time1;
            Cache.playtimeMonth.put(uuid, Cache.playtimeDay.getOrDefault(uuid, 0L) + time);
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

    /**
     * Returns whether time1 is on a new day/week/month compared to time2
     * @param time1 older time
     * @param time2 newer time
     * @return a boolean array [isNewDay, isNewWeek, isNewMonth]
     */
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
    public static long getMinutesFromCurrentDay(long currentMs) {
        long minutes = (int) (currentMs / (1000 * 60));

        long minutesSinceMidnight = minutes % (24 * 60);

        return minutesSinceMidnight;
    }
    public static String getLocale(String uuid){

        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("SELECT lang FROM userLangs WHERE uuid = ?;");) {
            stmt.setString(1, uuid);

            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            return resultSet.getString("lang");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }


    }
}
