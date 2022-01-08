package Kyu.ServerCoreBungee.Bansystem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanInfo;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanReason;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanType;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Util;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BanInfoCMD extends Command {

    public BanInfoCMD(Main plugin) {
        super("baninfo", "bcore.ban", "binfo");
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs", true)));
            return;
        }
        String playerName = args[0];
        if (Main.getUuidStorage().get(playerName.toLowerCase()) == null) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "PlayerWasNeverOnServer", true)));
            return;
        }
        String uuid = Main.getUuidStorage().getString(playerName.toLowerCase());

        Map<String, List<BanInfo>> bans;
        boolean onlyActive = false;
        if (args.length > 1 && args[1].equals("active")) {
            bans = fetchActiveBans(uuid);
            onlyActive = true;
        } else {
            bans = fetchAllBans(uuid);
        }

        String language = sender instanceof ProxiedPlayer ? LanguageHelper.getLanguage((ProxiedPlayer) sender)
                : LanguageHelper.getDefaultLang();

        TextComponent banInfo;
        if (onlyActive) {
            banInfo = new TextComponent(LanguageHelper.getMess(sender, "BanInfoTemplateActiveHeader")
                    .replace("%player", playerName));
        } else {
            banInfo = new TextComponent(LanguageHelper.getMess(sender, "BanInfoTemplateHeader")
                    .replace("%player", playerName));
        }

        Map<String, TextComponent> combinedBans = new HashMap<>();
        Map<TextComponent, String> bansThatAreCombined = new HashMap<>();

        for (String reason : bans.keySet()) {
            if (bans.get(reason).isEmpty())
                continue;

            String reasonMess = LanguageHelper.getMess(sender, "BanInfoTemplateReasonHeader");
            if (reason.startsWith("CUSTOM_")) {
                reasonMess = reasonMess.replace("%reason", reason.split("CUSTOM_")[1]);
            } else if (reason.startsWith("CMB_")) {
                String reasons = "";
                for (String string : reason.split("CMB_")[1].split("\\+")) {
                    if (string.startsWith("CUSTOM_")) reasons += " + " + string.split("CUSTOM_")[1];
                    else reasons += " + " + LanguageHelper.getMess(sender, string);
                }
                reasons = reasons.replaceFirst(" \\+ ", "");
                reasonMess = reasonMess.replace("%reason", reasons);
            } else {
                reasonMess = reasonMess.replace("%reason", LanguageHelper.getMess(sender, reason));
            }
            TextComponent reasonComponent = new TextComponent(reasonMess);
            

            int index = 1;

            for (BanInfo info : bans.get(reason)) {
                String message;

                if (info.getCombinedInto() != null) {
                    message = LanguageHelper.getMess(sender, "BanInfoTemplateCombineEntry");
                } else {
                    message = LanguageHelper.getMess(sender, "BanInfoTemplateEntry");
                }

                if (info.getBantype().equals(BanType.KICK)) {
                    message = message.replace("%banTime", "Kick").replace("%unbanDate", "Kick");
                }

                if (!info.isPermanent()) {
                    message = message.replace("%index", "" + index)
                            .replace("%banID", info.getBanUUID())
                            .replace("%bannedBy", info.getBannedBy())
                            .replace("%banDate", info.getBanOn().toString())
                            .replace("%banTime", Util.getDateDiff(info.getBanOn(), info.getBanExpireOn(), language))
                            .replace("%unbanDate", info.getBanExpireOn().toString())
                            .replace("%banType", info.getBantype().toString());
                } else {
                    message = message.replace("%index", "" + index)
                            .replace("%banID", info.getBanUUID())
                            .replace("%bannedBy", info.getBannedBy())
                            .replace("%banDate", info.getBanOn().toString())
                            .replace("%banTime", LanguageHelper.getMess(sender, "Permanent"))
                            .replace("%unbanDate", LanguageHelper.getMess(sender, "Permanent"))
                            .replace("%banType", info.getBantype().toString());
                }

                TextComponent banInfoComponent = new TextComponent(message);
                if (info.getCombinedInto() != null) {
                    bansThatAreCombined.put(banInfoComponent, info.getCombinedInto());
                    continue;
                }

                if (!info.isEarlyUnban() && !info.getBantype().equals(BanType.KICK)
                        && (info.isPermanent() || info.getBanExpireOn().getTime() > System.currentTimeMillis())) {
                    banInfoComponent
                            .setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND,
                                    "/unban " + info.getBanUUID()));
                    banInfoComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(LanguageHelper.getMess(sender, "BanInfoTemplateHoverText")).create()));
                }

                reasonComponent.addExtra("\n");
                reasonComponent.addExtra(banInfoComponent);

                if (reason.startsWith("CMB_"))
                    combinedBans.put(info.getBanUUID(), banInfoComponent);

                if (info.isEarlyUnban()) {
                    reasonComponent.addExtra("\n");
                    reasonComponent.addExtra(LanguageHelper.getMess(sender, "BanInfoTemplateEarlyUnbanInfo")
                            .replace("%player", info.getEarlyUnbanBy())
                            .replace("%date", info.getEarlyUnbanOn().toString()));
                }

                index++;
            }
            if (index > 1) {
                banInfo.addExtra("\n");
                banInfo.addExtra("\n");
                banInfo.addExtra(reasonComponent);
            }
        }
        for (TextComponent comp : bansThatAreCombined.keySet()) {
            String banToCombineInto = bansThatAreCombined.get(comp);
            TextComponent ban = combinedBans.getOrDefault(banToCombineInto, null);
            if (ban == null)
                continue;
            ban.addExtra("\n");
            String combineMess = LanguageHelper.getMess(sender, "BanInfoTemplateCombined");
            String[] split = combineMess.split("%ban");
            ban.addExtra(split[0]);
            ban.addExtra(comp);
            if (split.length > 1) {
                ban.addExtra(split[1]);
            }
        }
        sender.sendMessage(banInfo);
    }

    private Map<String, List<BanInfo>> fetchAllBans(String uuid) {
        Map<String, List<BanInfo>> bans = new HashMap<>();
        for (BanReason banreason : BanCMD.banReasons) {
            bans.put(banreason.getReason(), new ArrayList<>());
        }

        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM banlogs WHERE uuid = ? ORDER BY bannedOn ASC;")) {
            stmt.setString(1, uuid);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                BanInfo info;

                BanType banType = BanType.valueOf(resultSet.getString("banType"));
                String reasonKey = resultSet.getString("banReasonKey");
                String banUUID = resultSet.getString("banUUID");
                String bannedBy = uuidToName(resultSet.getString("bannedBy"));
                Date bannedOn = new Date(resultSet.getLong("bannedOn"));
                String playerUUID = resultSet.getString("uuid");
                String combinedInto = resultSet.getString("combinedIntoNew");

                long unbanLong = resultSet.getLong("unbanOn");
                boolean permanent = unbanLong == -1;
                unbanLong = permanent ? 0 : unbanLong;
                if (permanent) {
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn);
                } else {
                    Date unbanOn = new Date(unbanLong);
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn, unbanOn);
                }

                boolean earlyUnban = resultSet.getBoolean("earlyUnban");
                if (earlyUnban) {
                    String unbanBy = uuidToName(resultSet.getString("earlyUnbanByUUID"));
                    Date earlyUnbanOn = new Date(resultSet.getLong("earlyUnbanOn"));
                    info.setEarlyUnban(unbanBy, earlyUnbanOn);
                }

                if (combinedInto != null) {
                    info.setCombinedInto(combinedInto);
                }

                if (!bans.containsKey(reasonKey)) {
                    List<BanInfo> list = new ArrayList<>(Arrays.asList(info));
                    bans.put(reasonKey, list);
                } else {
                    bans.get(reasonKey).add(info);
                }
            }
            conn.close();
            return bans;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, List<BanInfo>> fetchActiveBans(String uuid) {
        Map<String, List<BanInfo>> bans = new HashMap<>();
        for (BanReason banreason : BanCMD.banReasons) {
            bans.put(banreason.getReason(), new ArrayList<>());
        }

        Connection conn = Main.getDb().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM bans WHERE uuid = ? ORDER BY bannedOn ASC;")) {
            stmt.setString(1, uuid);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                BanInfo info;

                BanType banType = BanType.valueOf(resultSet.getString("banType"));
                String reasonKey = resultSet.getString("banReasonKey");
                String banUUID = resultSet.getString("banUUID");
                String bannedBy = uuidToName(resultSet.getString("bannedBy"));
                Date bannedOn = new Date(resultSet.getLong("bannedOn"));
                String playerUUID = resultSet.getString("uuid");

                long unbanLong = resultSet.getLong("unbanOn");
                boolean permanent = unbanLong == -1;
                unbanLong = permanent ? 0 : unbanLong;
                if (!permanent && unbanLong < System.currentTimeMillis())
                    continue;
                if (permanent) {
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn);
                } else {
                    Date unbanOn = new Date(unbanLong);
                    info = new BanInfo(banUUID, playerUUID, banType, bannedBy, bannedOn, unbanOn);
                }

                if (!bans.containsKey(reasonKey)) {
                    List<BanInfo> list = new ArrayList<>(Arrays.asList(info));
                    bans.put(reasonKey, list);
                } else {
                    bans.get(reasonKey).add(info);
                }
            }
            conn.close();
            return bans;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String uuidToName(String uuid) {
        if (Main.getUuidStorage().get(uuid) == null) {
            return "UUID NOT FOUND!";
        } else {
            return Main.getUuidStorage().getString(uuid);
        }
    }

}
