package PixelWar.Aurelia.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class NetworkPlayer {

    private static Map<Player, NetworkPlayer> players = new HashMap<>();

    private Player player;
    private List<UUID> playerDataUUIDs = new ArrayList<>();
    private UUID lastUsedProfile;

    public NetworkPlayer(Player p) {
        this.player = p;
        loadData();
    }

    public void setLastUsedProfile(UUID lastUsedProfile) {
        this.lastUsedProfile = lastUsedProfile;
    }

    public UUID getLastUsedProfile() {
        return lastUsedProfile;
    }

    public boolean hasProfiles() {
        return playerDataUUIDs.size() > 0;
    }

    public List<UUID> getProfiles() {
        return playerDataUUIDs;
    }

    private void loadData() {
        //TODO: this
    }

    public static NetworkPlayer get(Player player) {
        return players.getOrDefault(player, null);
    }
    
    public static NetworkPlayer addPlayer(Player player) {
        NetworkPlayer aureliaPlayer = new NetworkPlayer(player);
        players.put(player, aureliaPlayer);
        return aureliaPlayer;
    }

    public static void removePlayer(Player player) {
        players.remove(player);
    }
}
