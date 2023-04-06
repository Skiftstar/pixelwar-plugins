package Kyu.Ontime;

import Kyu.Ontime.Commands.OntimeCommand;
import Kyu.Ontime.Database.DB;
import Kyu.Ontime.Listeners.OntimeListener;
import Kyu.WaterFallLanguageHelper.LanguageHelper;
import Kyu.WaterFallLanguageHelper.MariaDB;
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

public final class Main extends Plugin {

    public static LanguageHelper helper;

    private static Logger logger;
    private static Main instance;
    private static DB db;
    private static File configFile, uuidStorageFile;
    private static Configuration config, uuidStorage;
    

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        loadConfigValues();

        db = new DB();

        new OntimeListener(this);
        new OntimeCommand(this);
    }

    @Override
    public void onDisable() {
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
    }
    public static Main getInstance(){
        return instance;
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

    public static Logger logger() {
        return logger;
    }

    public static Main instance() {
        return instance;
    }

    public static DB getDb() {
        return db;
    }

    public static void saveUUIDStorage() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(uuidStorage, uuidStorageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration getUuidStorage() {
        return uuidStorage;
    }
}
