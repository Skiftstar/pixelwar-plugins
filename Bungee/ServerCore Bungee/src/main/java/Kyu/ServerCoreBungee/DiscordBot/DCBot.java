package Kyu.ServerCoreBungee.DiscordBot;

import java.awt.Color;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import javax.security.auth.login.LoginException;

import Kyu.ServerCoreBungee.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class DCBot implements EventListener {

    private String logChannelID, guildId;
    private JDA jda;

    public void login(String token) {
        try {
            jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .setActivity(Activity.playing("on mc.pixelwar.eu"))
            .build();

            jda.upsertCommand("baninfo", "Show ban info of a player");
        } catch (LoginException e) {
            System.out.println("DISCORD BOT LOGIN EXCPETION");
            e.printStackTrace();
        }
        logChannelID = Main.getConfig().getString("DiscordLogChannel");
        guildId = Main.getConfig().getString("DiscordServerID");
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
        if (e.getName().equals("baninfo")) {
            showBanInfo(e);
        }
    }

    private void showBanInfo(SlashCommandEvent e) {
        if (!e.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            return;
        }
        e.reply("tolle Info!");
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