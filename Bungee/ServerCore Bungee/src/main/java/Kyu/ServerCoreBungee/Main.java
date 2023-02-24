package Kyu.ServerCoreBungee;

import Kyu.ServerCoreBungee.Bansystem.BanCMD;
import Kyu.ServerCoreBungee.Bansystem.BanInfoCMD;
import Kyu.ServerCoreBungee.Bansystem.BansHandler;
import Kyu.ServerCoreBungee.Bansystem.HardBanCMD;
import Kyu.ServerCoreBungee.Bansystem.UnbanCMD;
import Kyu.ServerCoreBungee.Commands.ChangeLangCommand;
import Kyu.ServerCoreBungee.Commands.DMCommand;
import Kyu.ServerCoreBungee.Commands.GlobalChatCommand;
import Kyu.ServerCoreBungee.Commands.OnlineCommand;
import Kyu.ServerCoreBungee.Commands.ReloadCMD;
import Kyu.ServerCoreBungee.Commands.TeamchatCommand;
import Kyu.ServerCoreBungee.Database.DB;
import Kyu.ServerCoreBungee.Listeners.JoinListener;
import Kyu.ServerCoreBungee.Util.LuckPermsAPI;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import Kyu.WaterFallLanguageHelper.MariaDB;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import Kyu.ServerCoreBungee.DiscordBot.DCBot;

public final class Main extends Plugin {

    public static LuckPerms lp;
    public static LanguageHelper helper;
    public static int cacheTimeout, confirmTimeout;

    private static Configuration config, uuidStorage;
    private static File configFile, uuidStorageFile;
    private static Logger logger;
    private static Main instance;
    private static DB db;

    private BansHandler handler = null;
    private DCBot discordBot;


    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        logger = getLogger();

        discordBot = new DCBot();

        loadConfigValues();

        try {
            lp = LuckPermsProvider.get();
            LuckPermsAPI.setLuckAPI(lp);
        } catch (IllegalStateException e) {
            System.out.println("Luckperms not loaded!");
            return;
        }

        db = new DB();

        new JoinListener(this);

        new OnlineCommand(this);

        new TeamchatCommand(this);
        new DMCommand(this);
        new GlobalChatCommand(this);
        new ChangeLangCommand(this);

        new BanCMD(this);
        new HardBanCMD(this);
        new BanInfoCMD(this);
        new UnbanCMD(this);
        new ReloadCMD(this);
        handler = new BansHandler(this);
        handler.loadBanReasons();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void loadConfigValues() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        uuidStorageFile = new File(getDataFolder(), "uuids.yml");
        try {
            if (!uuidStorageFile.exists()) uuidStorageFile.createNewFile();
            uuidStorage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(uuidStorageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uuidStorage.set("CONSOLE", "CONSOLE");
        saveUUIDStorage();

        configFile = new File(getDataFolder(), "config.yml");
        try {
            if (!configFile.exists()) {
                InputStream in = getResourceAsStream("config.yml");
                Files.copy(in, configFile.toPath());
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String user = config.getString("database.user");
        String password = config.getString("database.password");

        MariaDB helperDb = new MariaDB(host, port, user, password, database, true);

        Map<String, Reader> langs = new HashMap<>();
        langs.put("de", new InputStreamReader(getResourceAsStream("de.yml")));
        langs.put("en", new InputStreamReader(getResourceAsStream("en.yml")));

        helper = new LanguageHelper(this, "de", langs, ChatColor.translateAlternateColorCodes('&', "&6[PixelCore] "), helperDb);


        confirmTimeout = getConfig().getInt("confirmTimeout");
        cacheTimeout = getConfig().getInt("cacheTimeout");
        if (handler != null) handler.loadBanReasons();
        BanCMD.announceBan = getConfig().getBoolean("announceBanGlobally");
        HardBanCMD.announceBan = BanCMD.announceBan;
        discordBot.login(getConfig().getString("DiscordToken"));
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void saveConfiguration() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveUUIDStorage() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(uuidStorage, uuidStorageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger logger() {
        return logger;
    }

    public static Main instance() {
        return instance;
    }

    public static Configuration getUuidStorage() {
        return uuidStorage;
    }

    public static DB getDb() {
        return db;
    }

    public DCBot getDiscordBot() {
        return discordBot;
    }
}
