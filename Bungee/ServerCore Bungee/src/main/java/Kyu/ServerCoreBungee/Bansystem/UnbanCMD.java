package Kyu.ServerCoreBungee.Bansystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanType;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnbanCMD extends Command {

    public UnbanCMD(Main main) {
        super("unban", "bcore.unban");
        main.getProxy().getPluginManager().registerCommand(main, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs", true)));
            return;
        }
        String banUUID = args[0];
        boolean removeLog = false;
        if (args.length > 1 && args[1].equalsIgnoreCase("removelog"))
            removeLog = true;
        removeBan(banUUID, sender);
        if (removeLog)
            removeLog(banUUID);
        else updateLog(banUUID, sender);
        sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "UnbanSuccess", true)));
        
    }

    private void removeBan(String uuid, CommandSender sender) {
        Connection conn = Main.getDb().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM bans WHERE banUUID = ?;")) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID pUUID = UUID.fromString(rs.getString("uuid"));
                BanType banType = BanType.valueOf(rs.getString("banType"));
                String banUUID = rs.getString("banUUID");
                switch (banType) {
                    case BAN:
                        BansHandler.bans.remove(pUUID);
                        BansHandler.bansCache.remove(pUUID);
                        break;
                    case GCHAT_MUTE:
                        BansHandler.gMuteds.remove(pUUID);
                        BansHandler.gMutedsCache.remove(pUUID);
                        break;
                    case MUTE:
                        if (Main.instance().getProxy().getPlayers().size() == 0) {
                            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "CouldNotUnbanNoPlayer", true)));
                        } else {
                            sendCustomData(Main.instance().getProxy().getPlayers().iterator().next(), pUUID.toString(), banUUID);
                        }
                        BansHandler.gMuteds.remove(pUUID);
                        BansHandler.gMutedsCache.remove(pUUID);
                        break;
                    case KICK:
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM bans WHERE banUUID = ?;")) {
            stmt.setString(1, uuid);
            stmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLog(String banUUID, CommandSender sender) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE banlogs SET earlyUnban = ?, earlyUnbanByUUID = ?, earlyUnbanOn = ? WHERE banUUID = ?;")) {
            stmt.setBoolean(1, true);
            stmt.setString(2, sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId().toString() : "CONSOLE");
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setString(4, banUUID);
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeLog(String uuid) {
        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM banlogs WHERE banUUID = ?;")) {
            stmt.setString(1, uuid);
            stmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void sendCustomData(ProxiedPlayer player, String pUUID, String banUUID) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        // perform a check to see if globally are no players
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("UnmuteChannel"); // the channel could be whatever you want
        out.writeUTF(pUUID + ";;;" + banUUID); // this data could be whatever you want

        player.getServer().getInfo().sendData("my:channel", out.toByteArray());
    }

}
