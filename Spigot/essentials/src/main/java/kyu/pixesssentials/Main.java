package kyu.pixesssentials;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import Kyu.SCommand;
import Kyu.LangSupport.DB;
import Kyu.LangSupport.LanguageHelper;
import kyu.pixesssentials.Listeners.EssentialsListener;
import kyu.pixesssentials.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class Main extends JavaPlugin{

    private YamlConfiguration config, joinedPlayersConfig, playerHomeConfig;
    private File configFile, joinedPlayersFile, playerHomeFile;
    public static Location spawnPos = null;
    public static LanguageHelper helper;
    private static Main instance;
    public static List<SCommand> commands = new ArrayList<>();
    public static int tpaTimeout = 0;



    @Override
    public void onEnable() {
		instance = this;

        loadConfigValues();

        SmallCommands.init(this);
        new EssentialsListener(this);
    }

    public void loadConfigValues() {

        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        configFile = new File(getDataFolder(), "config.yml");
        joinedPlayersFile = new File(getDataFolder(), "joinedPlayers.yml");
        playerHomeFile = new File(getDataFolder(), "playerHomes.yml");
        try {
            if (!configFile.exists()) {
                InputStream in = getResource("config.yml");
                Files.copy(in, configFile.toPath());
            }
            if (!joinedPlayersFile.exists()) {
                joinedPlayersFile.createNewFile();
            }
            if (!playerHomeFile.exists()) {
                playerHomeFile.createNewFile();
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            playerHomeConfig = YamlConfiguration.loadConfiguration(playerHomeFile);
            joinedPlayersConfig = YamlConfiguration.loadConfiguration(joinedPlayersFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        tpaTimeout = config.getInt("tpaTimeout");

        if (config.get("Essentials.Spawn") != null) {
            double x = config.getDouble("Essentials.Spawn.X");
            double y = config.getDouble("Essentials.Spawn.Y");
            double z = config.getDouble("Essentials.Spawn.Z");
            float pitch = (float) config.getDouble("Essentials.Spawn.Pitch");
            float yaw = (float) config.getDouble("Essentials.Spawn.Yaw");
            String world = config.getString("Essentials.Spawn.World");
            spawnPos = new Location(Bukkit.getWorld(UUID.fromString(world)), x, y, z, yaw, pitch);

        }

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

    public YamlConfiguration getJoinedPlayersConfig() {
        return joinedPlayersConfig;
    }

    public void saveJoinedPlayersConfig() {
        try {
            joinedPlayersConfig.save(joinedPlayersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getPlayerHomeConfig() {
        return playerHomeConfig;
    }

    public void save(YamlConfiguration config) {
        try {
            if (config.equals(playerHomeConfig)) {
                playerHomeConfig.save(playerHomeFile);
            }
            else if (config.equals(joinedPlayersConfig)) {
                joinedPlayersConfig.save(joinedPlayersFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

}
