package PixelWar.Aurelia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import Kyu.SCommand;
import Kyu.LangSupport.LanguageHelper;
import Kyu.LangSupport.DB.MariaDB;
import PixelWar.Aurelia.Commands.GUICommands.MainMenuCommand;
import PixelWar.Aurelia.DB.DB;
import PixelWar.Aurelia.Listener.JoinLeaveListener;
import PixelWar.Aurelia.Util.Util;

public final class Main extends JavaPlugin {

    public static LanguageHelper helper;
    public static String serverName;
    public static long cacheDelay;
    public static List<SCommand> commands = new ArrayList<>();

    private static Main instance;
    
    private YamlConfiguration config;
    private File configFile;
    private DB db;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        configCheck();

        db = new DB();

        loadConfigValues();

        new JoinLeaveListener(this);
        new MainMenuCommand(this);
    }

    public void loadConfigValues() {
        if (!fetchConfigFromDB()) {
            //Load Config from Local
        }

        
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String user = config.getString("database.user");
        String password = config.getString("database.password");
        
        MariaDB helperDb = new MariaDB(host, port, user, password, database, true);

        Map<String, Reader> langs = new HashMap<>();
        langs.put("de", getTextResource("de.yml"));
        langs.put("en", getTextResource("en.yml"));

        helper = new LanguageHelper(this, "de", langs, Util.color(getConfig().getString("chatPrefix") + " "), helperDb);
        helper.setSendNoLangSetMess(false);


        for (SCommand command : commands) {
            command.setLangHelper(helper);
        }
    }

    // Check if Local Config file exists, if not, create
    private void configCheck() {
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

    // Would be used to transfer Local Config Values to Remote, unsure if it will be used
    // private void updateDBConfig() {
    // }

    private boolean fetchConfigFromDB() {
        // Commented until Core is setup

        // try (PreparedStatement stmt = db.getConnection().prepareStatement("SELECT * FROM aurelia_core_config;")) {
        //     ResultSet rs = stmt.executeQuery();
        //     while (rs.next()) {
        //         //Fetch from remote and save locally

        //         saveConfig();
        //         return true;
        //     }
        //     return false;
        // } catch (SQLException e) {
        //     e.printStackTrace();
        //     return false;
        // }
        return true;
    }
}
