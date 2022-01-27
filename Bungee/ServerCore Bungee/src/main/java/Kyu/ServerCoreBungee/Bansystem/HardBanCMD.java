package Kyu.ServerCoreBungee.Bansystem;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Ban;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanTime;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanType;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Pair;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Util;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HardBanCMD extends Command {

    public static boolean announceBan;

    public HardBanCMD(Main plugin) {
        super("hardban", "core.hardban");
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs", true)));
            return;
        }
        String player = args[0];
        if (Main.getUuidStorage().get(player.toLowerCase()) == null) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "PlayerWasNeverOnServer", true)));
            return;
        }
        String uuid = Main.getUuidStorage().getString(player.toLowerCase());
        String banTypeSt = args[1];
        BanType banType;
        try {
            banType = BanType.valueOf(banTypeSt);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NotAValidBanType", true)
                    .replace("%types", Arrays.toString(BanType.values()))));
            return;
        }
        boolean isKick = false;
        if (banType == BanType.KICK)
            isKick = true;
        if (!((isKick && args.length >= 2) || (!isKick && args.length >= 3))) {
            sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "NEArgs", true)));
            return;
        }
        int reasonIndex = isKick ? 2 : 3;
        String reason = "";
        for (int i = reasonIndex; i < args.length; i++) {
            reason += " " + args[i];
        }
        reason = reason.replaceFirst(" ", "");
        reason = "CUSTOM_" + reason;

        int[] times = new int[] { 0, 0, 0, 0, 0 };
        boolean permanent = false;
        if (!isKick) {
            String duration = args[2];
            if (duration.equalsIgnoreCase("permanent")) {
                permanent = true;
            } else {
                times = Util.stringToUnbanTime(duration);
            }
        }

        BanTime bantime = new BanTime(times[0], times[1], times[2], times[3], times[4], permanent, banType);
        long currTime = System.currentTimeMillis();

        String banUUID;
        do {
            banUUID = Util.generateUUID();
        } while (Util.exists(banUUID));

        Pair<Long, String> pair = Util.checkForActive(UUID.fromString(uuid), banType.toString(), bantime, banUUID);

        long unbanOn = pair.first;
        String reasonSt = reason;
        if (pair.second.length() > 0) {
            reasonSt = "CMB_" + reasonSt + pair.second;
        }
        String banner = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId().toString()
                : "CONSOLE";
        Util.putInDB(UUID.fromString(uuid), banner, reasonSt, bantime, unbanOn, banUUID, currTime);

        ProxiedPlayer p = Main.instance().getProxy().getPlayer(UUID.fromString(uuid));
        String kickMessage;
        if (p != null) {
            Ban ban;
            switch (banType) {
                case BAN:
                    ban = new Ban(reasonSt, new Date(unbanOn), banType, unbanOn == -1,
                            banUUID);
                    ban.setLanguage(LanguageHelper.getLanguage(p));
                    if (unbanOn == -1) {
                        kickMessage = LanguageHelper.getMess(p, "PermaBanMessage")
                                .replace("%reason", reason.split("CUSTOM_")[1]);
                    } else {
                        kickMessage = LanguageHelper.getMess(p, "TempbanMessage")
                                .replace("%reason", reason.split("CUSTOM_")[1])
                                .replace("%duration", Util.getRemainingTime(new Date(unbanOn),
                                        LanguageHelper.getLanguage(p)));
                    }
                    BansHandler.bans.put(p.getUniqueId(), ban);
                    p.disconnect(new TextComponent(kickMessage));
                    break;
                case KICK:
                    kickMessage = LanguageHelper.getMess(p, "KickMessage")
                            .replace("%reason", reason.split("CUSTOM_")[1]);
                    p.disconnect(new TextComponent(kickMessage));
                    break;
                case MUTE:
                    ban = new Ban(reasonSt, new Date(unbanOn), banType, unbanOn == -1,
                            banUUID);
                    if (unbanOn == -1) {
                        p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "PermaMuteMessage")
                                .replace("%reason", reason.split("CUSTOM_")[1])));
                        sendCustomData(p, p.getUniqueId().toString(), reasonSt, -1, banUUID);
                    } else {
                        p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "MuteMessage")
                                .replace("%duration",
                                        Util.getRemainingTime(new Date(unbanOn),
                                                LanguageHelper.getLanguage(p)))
                                .replace("%reason", reason.split("CUSTOM_")[1])));
                        sendCustomData(p, p.getUniqueId().toString(), reasonSt,
                                unbanOn, banUUID);
                    }
                    BansHandler.gMuteds.put(p.getUniqueId(), ban);
                    break;
                case GCHAT_MUTE:
                    ban = new Ban(reasonSt, new Date(unbanOn), banType, unbanOn == -1,
                            banUUID);
                    if (unbanOn == -1) {
                        p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatPermaMuteMessage")
                                .replace("%reason", reason.split("CUSTOM_")[1])));
                    } else {
                        p.sendMessage(new TextComponent(LanguageHelper.getMess(p, "GChatMuteMessage")
                                .replace("%duration",
                                        Util.getRemainingTime(new Date(unbanOn),
                                                LanguageHelper.getLanguage(p)))
                                .replace("%reason", reason.split("CUSTOM_")[1])));
                    }
                    BansHandler.gMuteds.put(p.getUniqueId(), ban);
                    break;
            }
        }
        sender.sendMessage(new TextComponent(LanguageHelper.getMess(sender, "PlayerBanned", true)
                .replace("%player", player)
                .replace("%reason", Util.getReason(reason, sender))));

        if (announceBan) {
            for (ProxiedPlayer pl : Main.instance().getProxy().getPlayers()) {
                pl.sendMessage(new TextComponent(LanguageHelper.getMess(pl, "Global_PlayerPunished", true)
                        .replace("%player", player)
                        .replace("%reason", Util.getReason(reason, pl))));
            }
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

}
