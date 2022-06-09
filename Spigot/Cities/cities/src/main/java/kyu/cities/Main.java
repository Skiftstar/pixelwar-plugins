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
import kyu.cities.Commands.JobCommand;
import kyu.cities.Listeners.CityChunkListeners;
import kyu.cities.Listeners.JobListener;
import kyu.cities.Listeners.JoinLeaveListener;
import kyu.cities.Util.City.City;
import kyu.cities.Util.General.Constants;
import kyu.cities.Util.General.EXPCurveType;
import kyu.cities.Util.General.RoundingType;
import kyu.cities.Util.General.Util;
import kyu.cities.Util.Player.Job;

public class Main extends JavaPlugin {

  private YamlConfiguration config, playersConfig, citiesConfig, nameMapperConfig;
  private File configFile, playersFile, citiesFile, nameMapperFile;
  public static LanguageHelper helper;
  private static Main instance;
  public static List<SCommand> commands = new ArrayList<>();
  // public static int cityCost = 0, confirmTimeout = 5;

    @Override
    public void onEnable() {
      instance = this;

      loadConfigValues();

      CityCommand.init();
      JobCommand.init();
      new JoinLeaveListener(this);
      new CityChunkListeners(this);
      new JobListener(this);
    }

    public void loadConfigValues() {

      if (!getDataFolder().exists()) getDataFolder().mkdirs();
      configFile = new File(getDataFolder(), "config.yml");
      playersFile = new File(getDataFolder(), "players.yml");
      citiesFile = new File(getDataFolder(), "cities.yml");
      nameMapperFile = new File(getDataFolder(), "nameToUUID.yml");
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
          if (!nameMapperFile.exists()) {
            nameMapperFile.createNewFile();
          }
          config = YamlConfiguration.loadConfiguration(configFile);
          playersConfig = YamlConfiguration.loadConfiguration(playersFile);
          citiesConfig = YamlConfiguration.loadConfiguration(citiesFile);
          nameMapperConfig = YamlConfiguration.loadConfiguration(nameMapperFile);
      } catch (IOException e) {
          e.printStackTrace();
      }

      Constants.confirmTimeout = config.getInt("ConfirmTimeout");

      Job.expConversion = config.getDouble("JobToCityEXPConversion");
      Job.expConversionRounding = RoundingType.valueOf(config.getString("JobToCityEXPConversionRounding"));
      Job.allowBredAnimals = config.getBoolean("AllowBredAnimalsForEXP");
      Job.allowSpawnerMobs = config.getBoolean("AllowSpawnerMobsForEXP");
      Job.swapCost = config.getInt("JobSwapCost");
      Job.expCurveType = EXPCurveType.valueOf(config.getString("JobEXPCurveType").toUpperCase());
      if (Job.expCurveType.equals(EXPCurveType.LINEAR)) {
        Job.base = config.getDouble("JobEXPCurveLinear.ExpPerLevel");
      } else {
        Job.exponent = config.getDouble("JobEXPCurveExponential.exp");
        Job.base = config.getDouble("JobEXPCurveExponential.Base");
        Job.multiplier = config.getDouble("JobEXPCurveExponential.multiplier");
      }
      Job.loadJobs();

      City.cost = config.getInt("CityCost");
      City.expCurveType = EXPCurveType.valueOf(config.getString("CityEXPCurveType").toUpperCase());
      if (City.expCurveType.equals(EXPCurveType.LINEAR)) {
        City.base = config.getDouble("CityEXPCurveLinear.ExpPerLevel");
      } else {
        City.exponent = config.getDouble("CityEXPCurveExponential.exp");
        City.base = config.getDouble("CityEXPCurveExponential.Base");
        City.multiplier = config.getDouble("CityEXPCurveExponential.multiplier");
      }
      City.defaultClaimableChunks = config.getInt("CityStartChunks");
      City.levelsPerNewChunk = config.getInt("CityLevelsPerChunk");

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

  public YamlConfiguration getNameMapperConfig() {
      return nameMapperConfig;
  }

  public static Main getInstance() {
      return instance;
  }

  public YamlConfiguration getConfig() {
    return config;
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
    else if (c.equals(instance.nameMapperConfig)) {
      f = instance.nameMapperFile;
    }
    try {
      c.save(f);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
