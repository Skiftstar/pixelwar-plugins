package Derio.Ontime.utils;

import Derio.Ontime.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LangFiles {

    private File file;
    private File dir;
    public static Configuration[] configs;
    private String[] configNames;

    public LangFiles(String... locales) throws IOException {
        configNames = new String[locales.length];
        configs = new Configuration[locales.length];
        for (int i = 0; i < locales.length; i++) {


        dir = new File(Main.instance().getDataFolder().getPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        file = new File(dir, locales[i] + ".yml");
            if (!file.exists()) {
                FileOutputStream outputStream = new FileOutputStream(file);
                InputStream in = Main.getInstance().getResourceAsStream(locales[i]+".yml");
                in.transferTo(outputStream);
            }
        configs[i] = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(dir, locales[i] + ".yml"));
        configNames[i] = locales[i];
    }


    }

    public String getMessage(String locale, String path){

        for (int i = 0; i < configNames.length; i++) {

            if (configNames[i].equalsIgnoreCase(locale)){
                return configs[i].getString(path);
            }

        }

        return null;
    }



}
