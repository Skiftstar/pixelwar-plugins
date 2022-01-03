package Kyu.ServerCoreBungee;

import Kyu.ServerCoreBungee.Bansystem.BanCMD;
import Kyu.ServerCoreBungee.Bansystem.BansHandler;
import Kyu.ServerCoreBungee.Commands.DMCommand;
import Kyu.ServerCoreBungee.Commands.GlobalChatCommand;
import Kyu.ServerCoreBungee.Commands.ReloadCMD;
import Kyu.ServerCoreBungee.Commands.TeamchatCommand;
import Kyu.ServerCoreBungee.Database.DB;
import Kyu.ServerCoreBungee.Listeners.JoinListener;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
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
import java.nio.file.Files;
import java.util.logging.Logger;

public final class Main extends Plugin {

    public static LuckPerms lp;
    private static Configuration config, uuidStorage;
    private static File configFile, uuidStorageFile;
    private static Logger logger;
    public static int cacheTimeout, confirmTimeout;
    private static Main instance;
    private BansHandler handler = null;
    private static DB db;


    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        logger = getLogger();

        loadConfigValues();

        try {
            lp = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            System.out.println("Luckperms not loaded!");
            return;
        }

        db = new DB();

        new JoinListener(this);

        new TeamchatCommand(this);
        new DMCommand(this);
        new GlobalChatCommand(this);


        new BanCMD(this);
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

        LanguageHelper.setup(this, "de", getResourceAsStream("de.yml"), ChatColor.translateAlternateColorCodes('&', "&6[PixelCore] "), true);

        uuidStorageFile = new File(getDataFolder(), "uuids.yml");
        try {
            if (!uuidStorageFile.exists()) uuidStorageFile.createNewFile();
            uuidStorage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(uuidStorageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        
        LanguageHelper.setDatabase(new Kyu.WaterFallLanguageHelper.DB(host, port, user, password, database));

        confirmTimeout = getConfig().getInt("confirmTimeout");
        cacheTimeout = getConfig().getInt("cacheTimeout");
        if (handler != null) handler.loadBanReasons();
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
}
