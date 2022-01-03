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
import java.sql.PreparedStatement;
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
                // TODO: Log check instead of fixed index
                BanTime bantime = reason.getBantime(1);
                BanType banType = bantime.getBanType();

                String banUUID = Util.generateUUID();
                String banner = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId().toString() : "CONSOLE";
                putInDB(uuid, banner, reason.getReason(), bantime, banUUID);
                // TODO: Logging on Discord

                if (p != null) {
                    String kickMessage;
                    Ban ban;
                    switch (banType) {
                        case BAN:
                            ban = new Ban(reason.getReason(), bantime.getUnbanDate(), banType, bantime.isPermanent(), banUUID);
                            ban.setLanguage(LanguageHelper.getLanguage(p));
                            if (bantime.isPermanent()) {
                                kickMessage = LanguageHelper.getMess(p, "PermaBanMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()));
                            } else {
                                kickMessage = LanguageHelper.getMess(p, "TempbanMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))
                                        .replace("%duration", Util.getRemainingTime(bantime.getUnbanDate(),
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
                            if (bantime.isPermanent()) {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "PermaMuteMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                                sendCustomData(p, p.getUniqueId().toString(), reason.getReason(), -1, banUUID);
                            } else {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "MuteMessage")
                                        .replace("%duration",
                                                Util.getRemainingTime(bantime.getUnbanDate(),
                                                        LanguageHelper.getLanguage(p)))
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                                sendCustomData(p, p.getUniqueId().toString(), reason.getReason(),
                                        bantime.getUnbanDate().getTime(), banUUID);
                            }
                            break;
                        case GCHAT_MUTE:
                            ban = new Ban(reason.getReason(), bantime.getUnbanDate(), banType, bantime.isPermanent(), banUUID);
                            if (bantime.isPermanent()) {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatPermaMuteMessage")
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                            } else {
                                p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatMuteMessage")
                                        .replace("%duration",
                                                Util.getRemainingTime(bantime.getUnbanDate(),
                                                        LanguageHelper.getLanguage(p)))
                                        .replace("%reason", LanguageHelper.getMess(p, reason.getReason()))));
                            }
                            BansHandler.gMuteds.put(p.getUniqueId(), ban);
                            break;
                    }

                }
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

    private UUID getUUID(String playerName) {
        if (Main.getUuidStorage().get(playerName.toLowerCase()) == null) {
            return null;
        }
        return UUID.fromString(Main.getUuidStorage().getString(playerName.toLowerCase()));
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

    private void putInDB(UUID uuid, String banner, String reason, BanTime bantime, String banUUID) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO bans(uuid, banType, banReasonKey, bannedBy, bannedOn, unbanOn, banUUID) VALUES(?, ?, ?, ?, ?, ?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, bantime.getBanType().toString());
            stmt.setString(3, reason);
            stmt.setString(4, banner);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setLong(6, bantime.isPermanent() ? -1 : bantime.getUnbanDate().getTime());
            stmt.setString(7, banUUID);
            stmt.execute();
        } catch (SQLException e) {
            Main.logger().warning("Something went wrong.");
            e.printStackTrace();
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO banlogs(uuid, banType, banReasonKey, bannedBy, bannedOn, unbanOn, banUUID) VALUES(?, ?, ?, ?, ?, ?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, bantime.getBanType().toString());
            stmt.setString(3, reason);
            stmt.setString(4, banner);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setLong(6, bantime.getUnbanDate().getTime());
            stmt.setString(7, banUUID);
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