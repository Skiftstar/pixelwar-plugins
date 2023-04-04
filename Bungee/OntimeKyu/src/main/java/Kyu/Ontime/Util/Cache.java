package Kyu.Ontime.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Kyu.Ontime.Main;

public class Cache {
    
    private static Map<UUID, Ontime> data = new HashMap<>();

    public static long[] getPlaytimes(UUID uuid) {
        if (!data.containsKey(uuid)) {
            register(uuid, false);
        }
        return data.get(uuid).getPlaytimes();
    }

    public static void setLastUpdate(UUID uuid, long lastUpdate) {
        if (!data.containsKey(uuid)) {
            register(uuid, false);
        }
        data.get(uuid).setLastUpdate(lastUpdate);
    }

    public static void handlePlayerLeave(UUID uuid) {
        if (!data.containsKey(uuid)) {
            register(uuid, true);
        }
        data.get(uuid).updateDB();
        data.get(uuid).setIsOnline(false);
    }

    public static void register(UUID uuid, boolean setOnline) {
        Ontime ontime;
        if (!data.containsKey(uuid)) {
            final long[] dataFromDB = Ontime.loadDataFromDB(uuid);
            if (dataFromDB[0] == -1L) {
                Main.logger().warning("Error loading Data from " + uuid + " from Database! Not Registering!\nSee error above!");
                return;
            }
            ontime = new Ontime(uuid, dataFromDB[0], dataFromDB[1], dataFromDB[2], dataFromDB[3], dataFromDB[4], false);
            data.put(uuid, ontime);
        } else {
            ontime = data.get(uuid);
        }

        ontime.setIsOnline(setOnline);
    }
}
