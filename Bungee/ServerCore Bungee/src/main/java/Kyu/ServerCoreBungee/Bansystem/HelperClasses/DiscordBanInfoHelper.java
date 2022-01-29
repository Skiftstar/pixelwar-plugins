package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import java.util.List;
import java.util.Map;

public class DiscordBanInfoHelper {
    private int index; 
    private boolean activeOnly; 
    private Map<String, String> reasons;
    private Map<String, List<List<BanInfo>>> bansMap;
    private String playerName;

    public DiscordBanInfoHelper(int index, boolean activeOnly, Map<String, String> reasons,
    Map<String, List<List<BanInfo>>> bansMap, String playerName) {
        this.index = index;
        this.activeOnly = activeOnly;
        this.reasons = reasons;
        this.bansMap = bansMap;
        this.playerName = playerName;
    }

    public int getIndex() {
        return index;
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    public Map<String, String> getReasons() {
        return reasons;
    }

    public Map<String, List<List<BanInfo>>> getBansMap() {
        return bansMap;
    }
    
    public String getPlayerName() {
        return playerName;
    }

    public void setIndex(int index) {
        if (index < 0) index = bansMap.size() - 1;
        if (index > bansMap.size() - 1) index = 0;
        this.index = index;
    }
}
