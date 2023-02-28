package Derio.Ontime.utils;

import Derio.Ontime.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerData {

    private File file;
    private File dir;
    public static Configuration config;

    public PlayerData() throws IOException {
        dir = new File(Main.instance().getDataFolder().getPath());

        file = new File(dir, "uuids.yml");

        if (!file.exists()){
            file.createNewFile();
        }

        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Main.instance().getDataFolder(),  "uuids.yml"));

    }


    public boolean isInConfig(UUID uuid, String name){
        return config.getString(uuid.toString()).equalsIgnoreCase(name);
    }

    public void set(UUID uuid,String name){
        config.set(uuid.toString() , name);
        config.set(name , uuid.toString());
        save();
    }
    public String getUUID(String name){
        return config.getString(name);
    }
    public String getName(String uuid){
        return config.getString(uuid);
    }
    public Collection<String> getListKeys(){
        return config.getKeys();
    }
    private void save(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(Main.instance().getDataFolder(), "uuids.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







}
