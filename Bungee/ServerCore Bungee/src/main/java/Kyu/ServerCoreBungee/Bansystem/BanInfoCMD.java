package Kyu.ServerCoreBungee.Bansystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanInfo;
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
            bans = Util.fetchActiveBans(uuid);
            onlyActive = true;
        } else {
            bans = Util.fetchAllBans(uuid);
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

            String reasonMess = LanguageHelper.getMess(sender, "BanInfoTemplateReasonHeader").replace("%reason", Util.getReason(reason, sender));
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

}
