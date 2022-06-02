package kyu.cities.Util.Player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import kyu.cities.Main;
import kyu.cities.Util.General.Pair;
import kyu.cities.Util.General.RoundingType;
import kyu.cities.Util.General.Util;

public class Job {
    public static Map<String, Job> jobs = new HashMap<>();
    public static double expConversion = 0;
    public static RoundingType expConversionRounding = RoundingType.NONE;
    public static boolean allowBredAnimals = false;
    public static boolean allowSpawnerMobs = false;
    public static int swapCost = 0;

    private String name;
    private Map<Material, Pair<Double, Double>> blocks;
    private Map<EntityType, Pair<Double, Double>> mobs;

    public Job(String name, Map<Material, Pair<Double, Double>> blocks, Map<EntityType, Pair<Double, Double>> entities) {
        this.name = name;
        blocks.keySet().forEach(block -> {
            Pair<Double, Double> pair = blocks.get(block);
            Double cityExp;
            if (pair.second == null) {
                cityExp = Util.round(pair.first, expConversionRounding);
            } else {
                cityExp = pair.second;
            }
            this.blocks.put(block, new Pair<>(pair.first, cityExp));
        });
        entities.keySet().forEach(entity -> {
            Pair<Double, Double> pair = entities.get(entity);
            Double cityExp;
            if (pair.second == null) {
                cityExp = Util.round(pair.first, expConversionRounding);
            } else {
                cityExp = pair.second;
            }
            this.mobs.put(entity, new Pair<>(pair.first, cityExp));
        });
        jobs.put(name, this);
    }

    public String getName() {
        return name;
    }

    public Pair<Double, Double> getBlockExp(Material mat) {
        return blocks.get(mat);
    }

    public Pair<Double, Double> getMobExp(EntityType entity) {
        return mobs.get(entity);
    }

    public static void loadJobs() {
        YamlConfiguration config = Main.getInstance().getConfig();
        if (config.get("Jobs") == null) {
            return;
        }
        for (String jobName : config.getConfigurationSection("Jobs").getKeys(false)) {
            Map<Material, Pair<Double, Double>> blocks = new HashMap<>();
            Map<EntityType, Pair<Double, Double>> entities = new HashMap<>();
            if (config.get("Jobs." + jobName + ".BlocksExp") != null) {
                for (String block : config.getConfigurationSection("Jobs." + jobName + ".BlocksExp").getKeys(false)) {
                    try {
                        if (config.get("Jobs." + jobName + ".BlocksExp." + block + ".Job") == null) {
                            continue;
                        }
                        Material mat = Material.valueOf(block.toUpperCase());
                        Double jobExp = config.getDouble("Jobs." + jobName + ".BlocksExp." + block + ".Job");
                        Double cityExp = null;
                        if (config.get("Jobs." + jobName + ".BlocksExp." + block + ".City") != null) {
                            cityExp = config.getDouble("Jobs." + jobName + ".BlocksExp." + block + ".City");
                        }
                        blocks.put(mat, new Pair<>(jobExp, cityExp));
                    } catch (Exception e) {
                        System.out.println("Error while loading Block " + block + " from Job " + jobName + ": " + e.getMessage());
                    }
                }
            }
            if (config.get("Jobs." + jobName + ".EntitiesExp") != null) {
                for (String entity : config.getConfigurationSection("Jobs." + jobName + ".EntitiesExp").getKeys(false)) {
                    try {
                        if (config.get("Jobs." + jobName + ".EntitiesExp." + entity + ".Job") == null) {
                            continue;
                        }
                        EntityType type = EntityType.valueOf(entity.toUpperCase());
                        Double jobExp = config.getDouble("Jobs." + jobName + ".EntitiesExp." + entity + ".Job");
                        Double cityExp = null;
                        if (config.get("Jobs." + jobName + ".EntitiesExp." + entity + ".City") != null) {
                            cityExp = config.getDouble("Jobs." + jobName + ".EntitiesExp." + entity + ".City");
                        }
                        entities.put(type, new Pair<>(jobExp, cityExp));
                    } catch (Exception e) {
                        System.out.println("Error while loading Entity " + entity + " from Job " + jobName + ": " + e.getMessage());
                    }
                }
            }
            new Job(jobName, blocks, entities);
        }
    }

    public static Job getJob(String name) {
        return jobs.getOrDefault(name, null);
    }
}
