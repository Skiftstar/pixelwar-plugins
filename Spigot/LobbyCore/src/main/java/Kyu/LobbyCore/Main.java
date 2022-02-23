package Kyu.LobbyCore;

import Kyu.LangSupport.LanguageHelper;
import Kyu.LobbyCore.Commands.Commands;
import Kyu.LobbyCore.Listeners.LobbyListeners;
import Kyu.LobbyCore.Listeners.ItemListener;
import Kyu.LobbyCore.Listeners.JoinListener;
import Kyu.LobbyCore.Listeners.VoidListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Logger logger;
    public static List<Server> servers = new ArrayList<>();
    public static Location spawnLoc = null;
    public static LanguageHelper helper;
    private YamlConfiguration config;
    private File configFile;
    public static ItemListener itemListener;
    public static int navigatorRows = 6;
    public static String navigatorTitle;


    @Override
    public void onEnable() {
        logger = getLogger();

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

        loadConfigValues();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        new JoinListener(this);
        new VoidListener(this);
        itemListener = new ItemListener(this);
        new LobbyListeners(this);

        Commands.initCommands(this);
    }

    private void loadServers() {
        servers.clear();
        for (String serverSt : getConfig().getConfigurationSection("servers").getKeys(false)) {
            String guiName = getConfig().getString("servers." + serverSt + ".name");
            List<String> description = getConfig().getStringList("servers." + serverSt + ".description");
            int slot = getConfig().getInt("servers." + serverSt + ".slot");
            Material mat;
            try {
                mat = Material.valueOf(getConfig().getString("servers." + serverSt + ".block"));
            } catch (IllegalArgumentException e) {
                logger.severe(helper.getMess("InvalidMaterial")
                        .replace("%server", serverSt));
                continue;
            }
            Server server = new Server(serverSt, guiName, mat, description, slot);
            servers.add(server);
        }
    }

    public void loadConfigValues() {
        System.out.println("Called");
        helper = new LanguageHelper(this, "de", getTextResource("de.yml"), Util.color("&6[Lobby] "));
        Commands.replaceLanguageHelper(helper);

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

        GameMode gamemode;
        try {
            gamemode = GameMode.valueOf(config.getString("defaultGamemode"));
        } catch (IllegalArgumentException e) {
            logger.severe(helper.getMess("InvalidGamemode"));
            gamemode = GameMode.ADVENTURE;
        }
        JoinListener.defaultMode = gamemode;

        navigatorRows = getConfig().getInt("navigatorRows");
        navigatorTitle = getConfig().getString("serverInterfaceTitle");
        VoidListener.voidReset = getConfig().getBoolean("resetOnVoid");

        if (config.get("spawnLoc") != null) {
            try {
                double x = config.getDouble("spawnLoc.X");
                double y = config.getDouble("spawnLoc.Y");
                double z = config.getDouble("spawnLoc.Z");
                float pitch = (float) config.getDouble("spawnLoc.Pitch");
                float yaw = (float) config.getDouble("spawnLoc.Yaw");
                String world = config.getString("spawnLoc.World");
                spawnLoc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
            } catch (Exception e) {
                logger.severe(helper.getMess("SpawnLocationError"));
            }
        }

        loadServers();
        System.out.println(JoinListener.defaultMode);
        for (Server server : servers) {
            System.out.println(server.getGuiName());
        }
    }

    @Override
    public void onDisable() {

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
