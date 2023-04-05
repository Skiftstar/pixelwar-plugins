package Kyu.Ontime.Util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import Kyu.Ontime.Main;

public class Ontime {

    private final UUID uuid;
    private long dayTime, weekTime, monthTime, totalTime, lastUpdate;
    private boolean isOnline;

    Ontime(final UUID uuid, long dayTime, long weekTime, long monthTime, long totalTime, long lastUpdate, boolean isOnline) {
        this.uuid = uuid;
        this.dayTime = dayTime;
        this.weekTime = weekTime;
        this.monthTime = monthTime;
        this.totalTime = totalTime;
        this.lastUpdate = lastUpdate;
        this.isOnline = isOnline;
    }

    void setIsOnline(final boolean isOnline) {
        this.isOnline = isOnline;
    }

    void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Get Playtime of a player
     * @return a long[] with {dayTime, weekTime, monthTime, totalTime}
     */
    long[] getPlaytimes() {
        final long current = System.currentTimeMillis();
        final boolean[] dateComparisons = Util.dateComparison(lastUpdate, current);
        final boolean isNewDay = dateComparisons[0];
        final boolean isNewWeek = dateComparisons[1];
        final boolean isNewMonth = dateComparisons[2];

        if (isOnline) {
            updatePlaytime(isNewDay, isNewWeek, isNewMonth, current);
            return new long[]{
                dayTime,
                weekTime,
                monthTime,
                totalTime
            };
        }
        return new long[]{
            isNewDay ? 0 : dayTime,
            isNewWeek ? 0 : weekTime,
            isNewMonth ? 0 : monthTime,
            totalTime
        };
    }

    void updatePlaytime(final boolean isNewDay, final boolean isNewWeek, final boolean isNewMonth, final long currentMillis) {
        final long msCurrentDay = Util.getMillisFromCurrentDay(currentMillis);

        //FIXME: Theoretically, if a player Joins on Sunday, plays all the way until Tuesday and never has his ontime updated on Monday
        // it will only display the minutes he played on Tuesday on "Playtime Week"
        // Month has a similiar problem
        // I decided to ignore this, as the scenario is pretty unlikely unless someone is a die hard minecraft gamer lmfao
        dayTime = isNewDay ? msCurrentDay : currentMillis - lastUpdate + dayTime;
        weekTime = isNewWeek ? msCurrentDay : currentMillis - lastUpdate + weekTime;
        monthTime = isNewWeek ? msCurrentDay : currentMillis - lastUpdate + monthTime;
        totalTime = currentMillis - lastUpdate + totalTime;
        lastUpdate = currentMillis;
    }

    void updateDB() {
        final long current = System.currentTimeMillis();
        final boolean[] dateComparisons = Util.dateComparison(lastUpdate, current);
        final boolean isNewDay = dateComparisons[0];
        final boolean isNewWeek = dateComparisons[1];
        final boolean isNewMonth = dateComparisons[2];

        updatePlaytime(isNewDay, isNewWeek, isNewMonth, current);
        saveToDB(uuid, dayTime, weekTime, monthTime, totalTime, lastUpdate);
    }

    static void saveToDB(final UUID uuid, final long dayTime, final long weekTime, final long monthTime, final long totalTime, final long lastUpdate) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("INSERT INTO player_ontimes (uuid, playtimeDay, playtimeWeek, playtimeMonth, playtimeTotal, lastUpdate) VALUES (?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE playtimeDay = ?, playtimeWeek = ?, playtimeMonth = ?, playtimeTotal = ?, lastUpdate = ?;")) {
                stmt.setString(1, uuid.toString());
                stmt.setLong(2, dayTime);
                stmt.setLong(3, weekTime);
                stmt.setLong(4, monthTime);
                stmt.setLong(5, totalTime);
                stmt.setLong(6, lastUpdate);

                stmt.setLong(7, dayTime);
                stmt.setLong(8, weekTime);
                stmt.setLong(9, monthTime);
                stmt.setLong(10, totalTime);
                stmt.setLong(11, lastUpdate);

                stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load Data of a Player From DB
     * @param uuid UUID of the player
     * @return a long[] with {dayTime, weekTime, monthTime, totalTime, lastUpdate}
     * or a long[] with {-1} if an error occured 
     */
    static long[] loadDataFromDB(UUID uuid) {
        try (PreparedStatement stmt = Main.getDb().getConnection().prepareStatement("SELECT * FROM player_ontimes WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                final long current = System.currentTimeMillis();
                saveToDB(uuid, 0, 0, 0, 0, current); 
                return new long[]{0, 0, 0, 0, current};
            }

            final long dayTime = rs.getLong("playtimeDay");
            final long weekTime = rs.getLong("playtimeWeek");
            final long monthTime = rs.getLong("playtimeMonth");
            final long totalTime = rs.getLong("playtimeTotal");
            final long lastUpdate = rs.getLong("lastUpdate");

            return new long[]{dayTime, weekTime, monthTime, totalTime, lastUpdate};
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{-1};
        }
    }

}
