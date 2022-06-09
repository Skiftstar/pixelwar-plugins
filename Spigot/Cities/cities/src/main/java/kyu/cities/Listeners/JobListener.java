package kyu.cities.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import kyu.cities.Main;
import kyu.cities.Util.General.Pair;
import kyu.cities.Util.Player.CPlayer;
import kyu.cities.Util.Player.Job;

public class JobListener implements Listener {
    
    public JobListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    private void onBlockBreak(BlockBreakEvent e) {
        CPlayer p = CPlayer.getCPlayer(e.getPlayer());
        Material mat = e.getBlock().getType();
        for (Job job : p.getJobs().keySet()) {
            if (job.getBlockExp(mat) == null) continue;
            
            Pair<Double, Double> expVals = job.getBlockExp(mat);
            p.addJobExp(job, expVals.first);
            if (p.getCity() == null) continue;
            p.getCity().addExp(expVals.second);
        }
    }

    @EventHandler
    private void onEntityKill(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.getKiller() == null) return;
        if (!(dead.getKiller() instanceof Player)) return;
        Player killer = (Player) dead.getKiller();
        CPlayer p = CPlayer.getCPlayer(killer);

        for (Job job : p.getJobs().keySet()) {
            if (job.getMobExp(dead.getType()) == null) continue;
            Pair<Double, Double> expVals = job.getMobExp(dead.getType());
            p.addJobExp(job, expVals.first);
            if (p.getCity() == null) continue;
            p.getCity().addExp(expVals.second);
        }
    }

}
