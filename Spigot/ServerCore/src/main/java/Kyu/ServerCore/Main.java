package Kyu.ServerCore;

import Kyu.LangSupport.DB;
import Kyu.LangSupport.LanguageHelper;
import Kyu.SCommand;
import Kyu.ServerCore.Bans.Mute;
import Kyu.ServerCore.Bans.MuteHandler;
import Kyu.ServerCore.Commands.GamemodeCMD;
import Kyu.ServerCore.Commands.SmallCommands;
import Kyu.ServerCore.Commands.TeleportCMD;
import Kyu.ServerCore.Listeners.ChatListener;
import Kyu.ServerCore.Listeners.JoinLeaveListener;
import Kyu.ServerCore.LuckPermsDenial.LuckPermsDenial;
import Kyu.ServerCore.TabAndScoreboard.ScoreboardManager;
import Kyu.ServerCore.Util.LuckPermsAPI;
import Kyu.ServerCore.Util.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public final class Main extends JavaPlugin implements PluginMessageListener{

    public static LuckPerms lp;
    public static LanguageHelper helper;
    public static String serverName;
    private static Main instance;
    public static int sidebarDelay;
    public static String toIgnore, discordLink;
    public static List<String> badWords = new ArrayList<>();
    public static List<SCommand> commands = new ArrayList<>();
    private YamlConfiguration config;
    private File configFile;
    public static long cacheDelay;
    public static Economy econ = null;
    public static Location spawnPos = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        try {
            lp = LuckPermsProvider.get();
            LuckPermsAPI.setLuckAPI(lp);
        } catch (IllegalStateException e) {
            System.out.println("Luckperms not loaded!");
            return;
        }

        if (!setupEconomy()) {
            System.out.println("Vault dependency not found! disabling!");
            return;
        }

        getServer().getMessenger().registerIncomingPluginChannel( this, "my:channel", this ); // we register the incoming channel

        loadConfigValues();

        new MuteHandler(this);

        ScoreboardManager.setup(this);

        GamemodeCMD.setup(this);
        TeleportCMD.setup(this);
        SmallCommands.init(this);

        new LuckPermsDenial(this);
        new JoinLeaveListener(this);
        new ChatListener(this);

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void loadConfigValues() {
        File badWordsFile = new File(this.getDataFolder(), "badWords.txt");
        if (badWordsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(badWordsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    badWords.add(line.toLowerCase());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        configFile = new File(getDataFolder(), "config.yml");
        try {
            if (!configFile.exists()) {
                InputStream in = getResource("config.yml");
                Files.copy(in, configFile.toPath());
            }
            config = YamlConfiguration.loadConfiguration(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        sidebarDelay = getConfig().getInt("ScoreboardRefreshDelay");
        serverName = Util.color(getConfig().getString("serverName"));
        toIgnore = Util.color(getConfig().getString("filterFromPrefixForScoreboard"));
        discordLink = getConfig().getString("discordLink");
        cacheDelay = getConfig().getInt("cacheTimeout") * 20L;

        helper = new LanguageHelper(this, "de", getTextResource("de.yml"), Util.color(getConfig().getString("chatPrefix") + " "), true);
        
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String user = config.getString("database.user");
        String password = config.getString("database.password");
        
        helper.setDatabase(new DB(host, port, user, password, database));

        for (SCommand command : commands) {
            command.setLangHelper(helper);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @NotNull
    @Override
    public YamlConfiguration getConfig() {
        return this.config;
    }

    @Override
    public void saveConfig() {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if ( !channel.equalsIgnoreCase( "my:channel" ) )
        {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput( bytes );
        String subChannel = in.readUTF();
        System.out.println(subChannel);
        if ( subChannel.equalsIgnoreCase( "MuteChannel" ) )
        {
            String string = in.readUTF();

            UUID uuid = UUID.fromString(string.split(";;;")[0]);
            System.out.println(Bukkit.getPlayer(uuid));
            if (Bukkit.getPlayer(uuid) == null) return;
            String reason = string.split(";;;")[1];
            String banUUID = string.split(";;;")[2];
            long unbanTime = in.readLong();
            boolean permanent = unbanTime == -1;
            System.out.println(unbanTime);
            System.out.println(System.currentTimeMillis());
            Mute mute = new Mute(reason, new Date(unbanTime < 0 ? 0 : unbanTime), permanent, banUUID);
            MuteHandler.mutedPlayers.put(uuid, mute);
        }
        if ( subChannel.equalsIgnoreCase("UnmuteChannel")) {
            String string = in.readUTF();
            UUID pUUID = UUID.fromString(string.split(";;;")[0]);
            MuteHandler.mutedPlayers.remove(pUUID);
            MuteHandler.mutedCache.remove(pUUID);
        }
    }
}
