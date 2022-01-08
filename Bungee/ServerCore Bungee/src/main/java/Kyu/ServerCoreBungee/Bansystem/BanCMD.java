package Kyu.ServerCoreBungee.Bansystem;

import Kyu.ServerCoreBungee.Bansystem.HelperClasses.*;
import Kyu.ServerCoreBungee.Main;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class BanCMD extends Command {

    private Main main;
    public static List<BanReason> banReasons = new ArrayList<>();
    private ConfirmCMD confirmCMD;

    public BanCMD(Main main) {
        super("ban", "bcore.ban", "punish");
        this.main = main;
        confirmCMD = new ConfirmCMD(main);
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs")));
            return;
        }
        if (args[0].equalsIgnoreCase("l")) {
            StringBuilder builder = new StringBuilder(LanguageHelper.getMess(sender, "BanReasonListTitle"));
            int index = 1;
            for (BanReason reason : banReasons) {
                builder.append("\n").append(LanguageHelper.getMess(sender, "BanReasonListEntry")
                        .replace("%index", "" + index)
                        .replace("%reason", LanguageHelper.getMess(sender, reason.getReason())));
                index++;
            }
            sender.sendMessage(new TextComponent(builder.toString()));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs", true)));
            return;
        }
        int reasonIndex;
        try {
            reasonIndex = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NaN", true)
                    .replace("%value", args[1])));
            return;
        }
        if (reasonIndex < 1 || reasonIndex > banReasons.size()) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "IndexOutOfBounds", true)));
            return;
        }
        BanReason reason = banReasons.get(reasonIndex - 1);

        String playerName = args[0];
        UUID uuid = getUUID(playerName);
        if (uuid == null) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "PlayerWasNeverOnServer", true)));
            return;
        }
        ProxiedPlayer p = main.getProxy().getPlayer(playerName);

        Consumer<Void> function = new Consumer<Void>() {
            @Override
            public void accept(Void unused) {
                long banTime = System.currentTimeMillis();
                int index = checkForPrevBans(uuid.toString(), reason.getReason());
                BanTime bantime = reason.getBantime(index);
                BanType banType = bantime.getBanType();

                String banUUID;
                do {
                    banUUID = Util.generateUUID();
                } while (exists(banUUID));

                Pair<Long, String> pair = checkForActive(uuid, banType.toString(), bantime, banUUID);

                long unbanOn = pair.first;
                System.out.println(unbanOn);
                String reasonSt = reason.getReason();
                if (pair.second.length() > 0) {
                    reasonSt = "CMB_" + reasonSt + pair.second;
                }
                String banner = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId().toString()
                        : "CONSOLE";
                putInDB(uuid, banner, reasonSt, bantime, unbanOn, banUUID, banTime);
                // TODO: Logging on Discord

                if (p != null) {
                    String kickMessage;
                    Ban ban;
                    switch (banType) {
                        case BAN:
                            ban = new Ban(reason.getReason(), new Date(unbanOn), banType, unbanOn == -1,
                                    banUUID);
                            ban.setLanguage(LanguageHelper.getLanguage(p));
                            if (unbanOn == -1) {
                                kickMessage = LanguageHelper.getMess(p, "PermaBanMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()));
                            } else {
                                kickMessage = LanguageHelper.getMess(p, "TempbanMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))
                                        .replace("%duration", Util.getRemainingTime(new Date(unbanOn),
                                                LanguageHelper.getLanguage(p)));
                            }
                            BansHandler.bans.put(p.getUniqueId(), ban);
                            p.disconnect(new TextComponent(kickMessage));
                            break;
                        case KICK:
                            kickMessage = LanguageHelper.getMess(p, "KickMessage")
                                    .replace("%reason", LanguageHelper.getMess(p, reason.getReason()));
                            p.disconnect(new TextComponent(kickMessage));
                            break;
                        case MUTE:
                            ban = new Ban(reason.getReason(), new Date(unbanOn), banType, unbanOn == -1,
                                    banUUID);
                            if (unbanOn == -1) {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "PermaMuteMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                                sendCustomData(p, p.getUniqueId().toString(), reason.getReason(), -1, banUUID);
                            } else {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "MuteMessage")
                                        .replace("%duration",
                                                Util.getRemainingTime(new Date(unbanOn),
                                                        LanguageHelper.getLanguage(p)))
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                                sendCustomData(p, p.getUniqueId().toString(), reason.getReason(),
                                        unbanOn, banUUID);
                            }
                            BansHandler.gMuteds.put(p.getUniqueId(), ban);
                            break;
                        case GCHAT_MUTE:
                            ban = new Ban(reason.getReason(), new Date(unbanOn), banType, unbanOn == -1,
                                    banUUID);
                            if (unbanOn == -1) {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatPermaMuteMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                            } else {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatMuteMessage")
                                        .replace("%duration",
                                                Util.getRemainingTime(new Date(unbanOn),
                                                        LanguageHelper.getLanguage(p)))
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                            }
                            BansHandler.gMuteds.put(p.getUniqueId(), ban);
                            break;
                    }

                }

                String banMess = LanguageHelper.getMess(sender, "PlayerBanned", true).replace("%player", playerName).replace("%reason", LanguageHelper.getMess(sender, reason.getReason()));
                
                sender.sendMessage(new TextComponent(banMess));
            }
        };

        if (p == null) {
            confirmCMD.addFunction(sender, function);
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "PleaseConfirmBan", true)
                    .replace("%player", playerName)
                    .replace("%reason", LanguageHelper.getMess(sender, reason.getReason()))));
        } else {
            function.accept(null);
        }

    }

    private boolean exists(String uuid) {
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

    private UUID getUUID(String playerName) {
        if (Main.getUuidStorage().get(playerName.toLowerCase()) == null) {
            return null;
        }
        return UUID.fromString(Main.getUuidStorage().getString(playerName.toLowerCase()));
    }

    private Pair<Long, String> checkForActive(UUID uuid, String banType, BanTime bantime, String newBanUUID) {
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

    public void sendCustomData(ProxiedPlayer player, String pUUID, String reason, long unbanLong, String banUUID) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        // perform a check to see if globally are no players
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("MuteChannel"); // the channel could be whatever you want
        out.writeUTF(pUUID + ";;;" + reason + ";;;" + banUUID); // this data could be whatever you want
        out.writeLong(unbanLong); // this data could be whatever you want
        System.out.println(unbanLong);

        player.getServer().getInfo().sendData("my:channel", out.toByteArray());
    }

    private int checkForPrevBans(String uuid, String reason) {

        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM banlogs WHERE uuid = ? AND banReasonKey = ?;")) {
            stmt.setString(1, uuid);
            stmt.setString(2, reason);
            ResultSet resultSet = stmt.executeQuery();

            int index = 0;
            while (resultSet.next()) {
                index++;
            }
            conn.close();
            return index;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void putInDB(UUID uuid, String banner, String reason, BanTime bantime, long unbanOn, String banUUID, long banTime) {
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
}