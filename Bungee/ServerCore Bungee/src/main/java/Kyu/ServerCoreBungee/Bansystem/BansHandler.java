package Kyu.ServerCoreBungee.Bansystem;

import Kyu.ServerCoreBungee.Bansystem.HelperClasses.*;
import Kyu.ServerCoreBungee.Main;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BansHandler implements Listener {

    public static Map<UUID, ScheduledTask> tasks = new HashMap<>();
    public static Map<UUID, Ban> gMuteds = new HashMap<>();
    public static Map<UUID, Ban> gMutedsCache = new HashMap<>();
    public static Map<UUID, Ban> bans = new HashMap<>();
    public static Map<UUID, Ban> bansCache = new HashMap<>();
    public static Map<UUID, List<Ban>> cache = new HashMap<>();

    private Main main;

    public BansHandler(Main main) {
        this.main = main;
        main.getProxy().getPluginManager().registerListener(main, this);
    }

    @EventHandler
    public void preJoin(LoginEvent e) {
        UUID uid = e.getConnection().getUniqueId();
        ScheduledTask task = tasks.getOrDefault(uid, null);
        if (task != null) {
            task.cancel();
        }
        tasks.remove(uid);

        List<Ban> bans = cache.getOrDefault(uid, new ArrayList<>());
        if (bans.size() > 0) {
            for (Ban ban : bans) {
                if (ban.getType().equals(BanType.BAN)) {
                    BansHandler.bans.put(uid, ban);
                } else if (ban.getType().equals(BanType.GCHAT_MUTE)) {
                    gMuteds.put(uid, ban);
                }
            }
        } else {
            System.out.println("Checking for bans in DB");
            checkForBans(uid);
        }

        if (BansHandler.bans.containsKey(uid)) {
            System.out.println("Kicking!");
            kickForBan(e, uid);
        }
    }

    private void kickForBan(LoginEvent e, UUID uuid) {
        Ban ban = bans.get(uuid);
        if (ban.isPermanent()) {
            e.setCancelReason(new TextComponent(Main.helper.getMess(ban.getLanguage(), "PermaBanMessage")
            .replace("%reason", Util.getReason(ban.getReason(), ban.getLanguage()))));
        } else {
        e.setCancelReason(new TextComponent(Main.helper.getMess(ban.getLanguage(), "TempbanMessage")
        .replace("%reason", Util.getReason(ban.getReason(), ban.getLanguage()))
        .replace("%duration", Util.getRemainingTime(ban.getUnbanDate(),
                ban.getLanguage()))));
        }
        e.setCancelled(true);
        cacheBans(uuid);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        System.out.println("Event fired");
        ProxiedPlayer p = e.getPlayer();
        cacheBans(p.getUniqueId());

    }

    private void cacheBans(UUID uuid) {
        List<Ban> bans = new ArrayList<>();
        bans.add(gMuteds.getOrDefault(uuid, null));
        gMuteds.remove(uuid);

        bans.add(BansHandler.bans.getOrDefault(uuid, null));
        BansHandler.bans.remove(uuid);

        ScheduledTask clearTask = main.getProxy().getScheduler().schedule(main, () -> {
            System.out.println("Removing!");
            cache.remove(uuid);
        }, Main.cacheTimeout, TimeUnit.SECONDS);

        BansHandler.tasks.remove(uuid);
        BansHandler.tasks.put(uuid, clearTask);
    }

    private void checkForBans(UUID uuid) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM bans WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                BanType banType = BanType.valueOf(resultSet.getString("banType"));
                long unbanLong = resultSet.getLong("unbanOn");
                String reasonKey = resultSet.getString("banReasonKey");
                boolean permanent = unbanLong == -1;
                unbanLong = unbanLong == -1 ? 0 : unbanLong;
                String banUUID = resultSet.getString("banUUID");
                Ban ban = new Ban(reasonKey, new Date(unbanLong), banType, permanent, banUUID);
                String language = fetchLangFromDB(uuid.toString());
                ban.setLanguage(language);

                System.out.println("Found!");

                if (ban.getType().equals(BanType.BAN)) {
                    BansHandler.bans.put(uuid, ban);
                } else if (ban.getType().equals(BanType.GCHAT_MUTE) || ban.getType().equals(BanType.MUTE)) {
                    gMuteds.put(uuid, ban);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String fetchLangFromDB(String uuid) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM langusers WHERE uuid = ?;")) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            conn.close();
            if (rs.next()) {
                return rs.getString("lang");
            }
            return Main.helper.getDefaultLang();
        } catch (SQLException e) {
            e.printStackTrace();
            return Main.helper.getDefaultLang();
        }
    }

    public void loadBanReasons() {
        BanCMD.banReasons.clear();
        Configuration config = Main.getConfig();
        for (String key : config.getSection("BanReasons").getKeys()) {
            String path = "BanReasons." + key + ".";

            String reason = config.getString(path + "reasonKey");

            // Load BanTimes
            List<BanTime> banTimes = new ArrayList<>();
            for (String bantimeKey : config.getSection(path + "BanTimes").getKeys()) {
                String bantimePath = path + "BanTimes." + bantimeKey + ".";
                BanType type;
                try {
                    type = BanType.valueOf(config.getString(bantimePath + "type"));
                } catch (IllegalArgumentException e) {
                    Main.logger().severe("Invalid BanType for banTimeKey " + bantimePath + "! Not loading BanTime!");
                    continue;
                }
                if (type == BanType.KICK) {
                    BanTime banTime = new BanTime(0, 0, 0, 0, 0, false, type);
                    banTimes.add(banTime);
                    continue;
                }
                boolean permanent = config.getBoolean(bantimePath + "permanent");
                if (permanent) {
                    BanTime banTime = new BanTime(0, 0, 0, 0, 0, true, type);
                    banTimes.add(banTime);
                } else {
                    int months = config.getInt(bantimePath + "months");
                    int days = config.getInt(bantimePath + "days");
                    int hours = config.getInt(bantimePath + "hours");
                    int minutes = config.getInt(bantimePath + "minutes");
                    int seconds = config.getInt(bantimePath + "seconds");
                    BanTime banTime = new BanTime(months, days, hours, minutes, seconds, false, type);
                    banTimes.add(banTime);
                }
            }

            BanReason banReason = new BanReason(reason, banTimes);
            BanCMD.banReasons.add(banReason);
        }
    }

    public static void remove(UUID uuid, Map<UUID, Ban> map) {
        Ban ban = map.get(uuid);
        map.remove(uuid);
        String banUUID = ban.getBanUUID();
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM bans WHERE banUUID = ? AND uuid = ?;")) {
            stmt.setString(1, banUUID);
            stmt.setString(2, uuid.toString());
            stmt.execute();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
