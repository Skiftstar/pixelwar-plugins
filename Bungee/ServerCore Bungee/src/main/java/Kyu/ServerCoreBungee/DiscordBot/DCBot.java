package Kyu.ServerCoreBungee.DiscordBot;

import javax.security.auth.login.LoginException;

import Kyu.ServerCoreBungee.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class DCBot implements EventListener {

    private String logChannelID, guildId;
    private JDA jda;

    public void login(String token) {
        try {
            jda = JDABuilder.createDefault(token).addEventListeners(this).build();
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
    }

    public void logSmth(String banner, String playerName, String reason) {
        TextChannel channel = jda.getGuildById(guildId).getTextChannelById(logChannelID);
        channel.sendMessage(banner + " hat " + playerName + " für " + reason + " gebannt").complete();
    }

}