package kyu.cities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import Kyu.SCommand;
import Kyu.LangSupport.DB;
import Kyu.LangSupport.LanguageHelper;
import kyu.cities.Commands.CityCommand;
import kyu.cities.Listeners.JoinLeaveListener;
import kyu.cities.Util.Util;

public class Main extends JavaPlugin {

  private YamlConfiguration config, playersConfig, citiesConfig;
  private File configFile, playersFile, citiesFile;
  public static LanguageHelper helper;
  private static Main instance;
  public static List<SCommand> commands = new ArrayList<>();
  public static int cityCost = 0;

    @Override
    public void onEnable() {
      instance = this;

      CityCommand.init();
      new JoinLeaveListener(this);
    }

    public void loadConfigValues() {

      if (!getDataFolder().exists()) getDataFolder().mkdirs();
      configFile = new File(getDataFolder(), "config.yml");
      playersFile = new File(getDataFolder(), "players.yml");
      citiesFile = new File(getDataFolder(), "cities.yml");
      try {
          if (!configFile.exists()) {
              InputStream in = getResource("config.yml");
              Files.copy(in, configFile.toPath());
          }
          if (!playersFile.exists()) {
            playersFile.createNewFile();
          }
          if (!citiesFile.exists()) {
            citiesFile.createNewFile();
          }
          config = YamlConfiguration.loadConfiguration(configFile);
          playersConfig = YamlConfiguration.loadConfiguration(playersFile);
          citiesConfig = YamlConfiguration.loadConfiguration(citiesFile);
      } catch (IOException e) {
          e.printStackTrace();
      }

      cityCost = config.getInt("CityCost");

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

  public YamlConfiguration getPlayersConfig() {
      return playersConfig;
  }

  public YamlConfiguration getCitiesConfig() {
      return citiesConfig;
  }

  public static Main getInstance() {
      return instance;
  }

  public static void saveConfig(YamlConfiguration c) {
    File f = null;
    if (c.equals(instance.config)) {
      f = instance.configFile;
    }
    else if (c.equals(instance.playersConfig)) {
      f = instance.playersFile;
    }
    else if (c.equals(instance.citiesConfig)) {
      f = instance.citiesFile;
    }
    try {
      c.save(f);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
