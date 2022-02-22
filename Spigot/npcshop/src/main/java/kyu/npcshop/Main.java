package kyu.npcshop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import Kyu.LangSupport.DB;
import Kyu.LangSupport.LanguageHelper;
import kyu.npcshop.Commands.NPCCommand;
import kyu.npcshop.CustomVillagers.CstmVillager;
import kyu.npcshop.Listeners.ClickListener;
import kyu.npcshop.Util.Util;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

    private static LanguageHelper helper;
    private YamlConfiguration config;
    private File configFile;
    private static Main instance;
    public static Economy econ = null;
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            System.out.println("Vault dependency not found! disabling!");
            return;
        }
        protocolManager = ProtocolLibrary.getProtocolManager();

        loadConfigValues();

        new NPCCommand(this);
        new ClickListener(this);
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
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

    private void loadConfigValues() {

        if (!getDataFolder().exists())
            getDataFolder().mkdirs();
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

        helper = new LanguageHelper(this, "de", getTextResource("de.yml"),
                Util.color(getConfig().getString("chatPrefix") + " "), true);

        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String user = config.getString("database.user");
        String password = config.getString("database.password");

        ClickListener.villagers.clear();

        if (getConfig().get("Villagers") != null) {
            for (String uuid : getConfig().getConfigurationSection("Villagers").getKeys(false)) {
                String name = getConfig().getString("Villagers." + uuid + ".name");
                ClickListener.villagers.put(UUID.fromString(uuid), new CstmVillager(UUID.fromString(uuid), name));
            }
        }

        helper.setDatabase(new DB(host, port, user, password, database));
    }

    @Override
    public void onDisable() {
    }

    public static LanguageHelper helper() {
        return helper;
    }

    public static Main getInstance() {
        return instance;
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
