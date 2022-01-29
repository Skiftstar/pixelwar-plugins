package Kyu.ServerCoreBungee.DiscordBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Kyu.ServerCoreBungee.Main;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanInfo;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.BanType;
import Kyu.ServerCoreBungee.Bansystem.HelperClasses.Util;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class DCBot implements EventListener {

    private String logChannelID, guildId;
    private JDA jda;

    public void login(String token) {
        try {
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    // .setActivity(Activity.playing("on mc.pixelwar.eu"))
                    .build();
            jda.awaitReady();
            // jda.upsertCommand("baninfo", "Show ban info of a player");
        } catch (Exception e) {
            System.out.println("DISCORD BOT STARTUP EXCPETION");
            e.printStackTrace();
        }
        logChannelID = Main.getConfig().getString("DiscordLogChannel");
        guildId = Main.getConfig().getString("DiscordServerID");
        jda.getGuildById(guildId).upsertCommand("baninfo", "Show ban info of a player")
                .addOption(OptionType.STRING, "name", "Name vom Spieler")
                .addOption(OptionType.BOOLEAN, "activeonly", "Falls true, zeigtn nur die aktiven Bans an").complete();
    }

    @Override
    public void onEvent(GenericEvent e) {
        if (e instanceof ReadyEvent) {
            System.out.println("Bot is ready!");
        }
        if (e instanceof SlashCommandEvent) {
            handleSlashCommands((SlashCommandEvent) e);
        }
    }

    private void handleSlashCommands(SlashCommandEvent e) {
        if (e.getChannelType().equals(ChannelType.PRIVATE))
            return;
        if (e.getName().equals("baninfo")) {
            showBanInfo(e);
        }
    }

    private void showBanInfo(SlashCommandEvent e) {
        if (!e.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            return;
        }
        String playerName = e.getOption("name").getAsString();
        if (playerName == null) {
            e.reply("Gib einen Spielernamen an!").setEphemeral(true).complete();
            return;
        }
        boolean activeOnly;
        if (e.getOption("activeonly") == null) {
            activeOnly = false;
        } else {
            activeOnly = e.getOption("activeonly").getAsBoolean();
        }

        if (Main.getUuidStorage().get(playerName.toLowerCase()) == null) {
            e.reply("Spieler war noch nie auf dem Server!").setEphemeral(true).complete();
            return;
        }
        String uuid = Main.getUuidStorage().getString(playerName.toLowerCase());

        Map<String, List<BanInfo>> bans;
        if (activeOnly) {
            bans = Util.fetchActiveBans(uuid);
        } else {
            bans = Util.fetchAllBans(uuid);
        }

        String language = "de";

        TextComponent banInfo;
        if (activeOnly) {
            banInfo = new TextComponent(LanguageHelper.getMess("de", "BanInfoTemplateActiveHeader")
                    .replace("%player", playerName));
        } else {
            banInfo = new TextComponent(LanguageHelper.getMess("de", "BanInfoTemplateHeader")
                    .replace("%player", playerName));
        }

        Map<String, List<BanInfo>> combinedBans = new HashMap<>();
        Map<BanInfo, String> bansThatAreCombined = new HashMap<>();

        // Key: Reason
        // Value: List of bans for this reason
        Map<String, List<List<BanInfo>>> bansMap = new HashMap<>();
        Map<String, String> reasons = new HashMap<>();

        for (String reason : bans.keySet()) {
            if (bans.get(reason).isEmpty())
                continue;

            reasons.put(reason, Util.getReason(reason, "de"));

            int index = 1;

            for (BanInfo info : bans.get(reason)) {
                String message;

                if (info.getCombinedInto() != null) {
                    message = LanguageHelper.getMess("de", "BanInfoTemplateCombineEntry");
                } else {
                    message = LanguageHelper.getMess("de", "BanInfoTemplateEntry");
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
                            .replace("%banTime", LanguageHelper.getMess("de", "Permanent"))
                            .replace("%unbanDate", LanguageHelper.getMess("de", "Permanent"))
                            .replace("%banType", info.getBantype().toString());
                }

                StringBuilder banInfoComponent = new StringBuilder(message);
                if (info.getCombinedInto() != null) {
                    bansThatAreCombined.put(info, info.getCombinedInto());
                    continue;
                }
                // if (!info.isEarlyUnban() && !info.getBantype().equals(BanType.KICK)
                // && (info.isPermanent() || info.getBanExpireOn().getTime() >
                // System.currentTimeMillis())) {
                // banInfoComponent
                // .setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND,
                // "/unban " + info.getBanUUID()));
                // banInfoComponent.setHoverEvent(new
                // HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                // new ComponentBuilder(LanguageHelper.getMess("de",
                // "BanInfoTemplateHoverText")).create()));
                // }

                List<List<BanInfo>> list = bansMap.getOrDefault(reason, new ArrayList<>());
                List<BanInfo> banInfoList = new ArrayList<>(Arrays.asList(info));
                list.add(new ArrayList<>(banInfoList));
                bansMap.put(reason, list);

                if (reason.startsWith("CMB_"))
                    combinedBans.put(info.getBanUUID(), banInfoList);

                if (info.isEarlyUnban()) {
                    banInfoComponent.append("\n").append(LanguageHelper.getMess("de", "BanInfoTemplateEarlyUnbanInfo")
                            .replace("%player", info.getEarlyUnbanBy())
                            .replace("%date", info.getEarlyUnbanOn().toString()));
                }

                index++;
            }
            // if (index > 1) {
            // banInfo.addExtra("\n");
            // banInfo.addExtra("\n");
            // banInfo.addExtra(reasonComponent);
            // }
        }
        for (BanInfo comp : bansThatAreCombined.keySet()) {
            String banToCombineInto = bansThatAreCombined.get(comp);
            List<BanInfo> ban = combinedBans.getOrDefault(banToCombineInto, null);
            if (ban == null)
                continue;
            ban.add(comp);
        }
        // StringBuilder message = new StringBuilder();
        // for (String reason : bansMap.keySet()) {
        // message.append(reasons.get(reason)).append("\n");
        // for (StringBuilder ban : bansMap.get(reason)) {
        // message.append(ban.toString()).append("\n");
        // }
        // message.append("\n");
        // }
        e.replyEmbeds(getBanInfoEmbed(0, activeOnly, reasons, bansMap, playerName)).complete();
    }

    private MessageEmbed getBanInfoEmbed(int index, boolean activeOnly, Map<String, String> reasons,
            Map<String, List<List<BanInfo>>> bansMap, String playerName) {
        List<String> bansList = new ArrayList<>(bansMap.keySet());
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle(activeOnly ? playerName + "'s laufende Bans" : playerName + "'s Bans");
        eb.setDescription("Grund: " + reasons.get(bansList.get(index)));
        int c = 0;
        for (List<BanInfo> bans : bansMap.get(bansList.get(index))) {
            System.out.println("Bans Size: " + bans.size());
            if (bans.size() < 1)
                continue;
            BanInfo mainBan = bans.get(0);
            eb.addField("BanID", mainBan.getBanUUID(), false);
            eb.addField("Gebannt von", mainBan.getBannedBy(), true);
            eb.addField("Gebannt am", mainBan.getBanOn().toString(), true);
            eb.addField("Ban Typ", mainBan.getBantype().toString(), true);
            if (!mainBan.getBantype().equals(BanType.KICK) && !mainBan.isPermanent()) {
                eb.addField("Unban am", mainBan.getBanExpireOn().toString(), true);
                eb.addField("Dauer", Util.getDateDiff(mainBan.getBanOn(), mainBan.getBanExpireOn(), "de"), true);
            }
            if (!mainBan.getBantype().equals(BanType.KICK)) {
                eb.addField("Permanent", mainBan.isPermanent() + "", true);
            }
            for (int i = 1; i < bans.size(); i++) {
                eb.addField("Zussamengesetzt aus Ban mit ID", bans.get(i).getBanUUID(), false);
                eb.addField("am", bans.get(i).getBanOn().toString(), true);
                eb.addField("von", bans.get(i).getBannedBy(), true);
                if (!bans.get(i).getBantype().equals(BanType.KICK) && !bans.get(i).isPermanent()) {
                    eb.addField("Dauer", Util.getDateDiff(bans.get(i).getBanOn(), bans.get(i).getBanExpireOn(), "de"),
                            true);
                }
            }
            if (c < bansMap.get(bansList.get(index)).size() - 1) {
                eb.addBlankField(false);
            }
            c++;
        }

        return eb.build();
    }

    public void logBan(String banner, String playerName, String reason) {
        TextChannel channel = jda.getGuildById(guildId).getTextChannelById(logChannelID);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Neuer Ban");
        embed.addField("Banned Player", playerName, false);
        embed.addField("Länge", "HARDCODED", false);
        embed.addField("Grund", reason, true);
        embed.addField("Banner", banner, true);
        embed.addField("Typ", "HARDCODED", true);
        embed.setTimestamp(new Date().toInstant());
        embed.setColor(Color.CYAN);
        channel.sendMessageEmbeds(embed.build()).complete();
    }

}