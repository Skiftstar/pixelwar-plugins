package Derio.Ontime;

import Derio.Ontime.Database.DB;
import Derio.Ontime.commands.OntimeCommand;
import Derio.Ontime.listener.OntimeListener;
import Derio.Ontime.utils.Cache;
import Derio.Ontime.utils.LangFiles;
import Derio.Ontime.utils.PlayerData;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.logging.Logger;

public final class Main extends Plugin {

    private static Configuration config;
    private static File configFile;
    private static Logger logger;
    private static Main instance;
    private static DB db;


    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        try {
            Cache.lang = new LangFiles("de", "en");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Cache.data = new PlayerData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cache.lastLogin = new HashMap<>();
        Cache.playtimeDay = new HashMap<>();
        Cache.playtimeWeek = new HashMap<>();
        Cache.playtimeTotal = new HashMap<>();
        Cache.playtimeMonth = new HashMap<>();

        loadConfigValues();
        db = new DB();
        new OntimeCommand(this);
        new OntimeListener(this);
    }

    @Override
    public void onDisable() {
    }

    public void loadConfigValues() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

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
}
