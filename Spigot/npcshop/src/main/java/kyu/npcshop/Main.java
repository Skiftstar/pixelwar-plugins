package kyu.npcshop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import Kyu.LangSupport.DB;
import Kyu.LangSupport.LanguageHelper;
import kyu.npcshop.Commands.NPCCommand;
import kyu.npcshop.Util.Util;

public class Main extends JavaPlugin {

    private static LanguageHelper helper;
    private YamlConfiguration config;
    private File configFile;

    @Override
    public void onEnable() {
        loadConfigValues();

        new NPCCommand(this);
    }

    private void loadConfigValues() {

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

        helper = new LanguageHelper(this, "de", getTextResource("de.yml"), Util.color(getConfig().getString("chatPrefix") + " "), true);
        
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String user = config.getString("database.user");
        String password = config.getString("database.password");
        
        helper.setDatabase(new DB(host, port, user, password, database));
    }

    @Override
    public void onDisable() {
    }

    public static LanguageHelper helper() {
        return helper;
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
}
