package kyu.cities.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import kyu.cities.Main;
import kyu.cities.Util.City.City;
import kyu.cities.Util.City.CityRank;
import kyu.cities.Util.Player.CPlayer;

public class CityChunkListeners implements Listener {

    public CityChunkListeners(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent e) {
        CPlayer p = CPlayer.getCPlayer(e.getPlayer());

        City chunkOwner = City.isChunkOwned(p.getPlayer().getChunk());
        if (chunkOwner == null) {
            return;
        }

        if (!chunkOwner.equals(p.getCity())) {
            e.setCancelled(true);
            p.sendMessage(Main.helper.getMess(e.getPlayer(), "CantBreakBlocksInOtherCity", true));
            return;
        }

        if (!chunkOwner.canNewcommersBreakPlace() && p.getRank().equals(CityRank.NEW_MEMBER)) {
            e.setCancelled(true);
            p.sendMessage(Main.helper.getMess(e.getPlayer(), "CantBreakBlocksAsNewcommer", true));
            return;
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        CPlayer p = CPlayer.getCPlayer(e.getPlayer());

        City chunkOwner = City.isChunkOwned(p.getPlayer().getChunk());
        if (chunkOwner == null) {
            return;
        }

        if (!chunkOwner.equals(p.getCity())) {
            e.setCancelled(true);
            p.sendMessage(Main.helper.getMess(e.getPlayer(), "CantPlaceBlocksInOtherCity", true));
            return;
        }

        if (!chunkOwner.canNewcommersBreakPlace() && p.getRank().equals(CityRank.NEW_MEMBER)) {
            e.setCancelled(true);
            p.sendMessage(Main.helper.getMess(e.getPlayer(), "CantPlaceBlocksAsNewcommer", true));
            return;
        }
    }

    @EventHandler
    private void onOpenBlock(InventoryOpenEvent e) {
        CPlayer p = CPlayer.getCPlayer((Player) e.getPlayer());
        
        if (e.getInventory().equals(p.getPlayer().getInventory())) {
            return;
        }

        City chunkOwner = City.isChunkOwned(p.getPlayer().getChunk());
        if (chunkOwner == null) {
            return;
        }

        if (!p.getCity().equals(chunkOwner)) {
            e.setCancelled(true);
            p.sendMessage(Main.helper.getMess(p.getPlayer(), "CantOpenStuffInOtherCity", true));
        }        
    }

    @EventHandler
    private void PlayerHurtByPlayerEvent(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        City chunkOwner = City.isChunkOwned(((Player) e.getEntity()).getChunk());
        if (chunkOwner == null) {
            return;
        }
        if (!chunkOwner.isPvpEnabled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onCropTrample(EntityInteractEvent e) {
        if (!(e.getBlock().getType() == Material.FARMLAND && e.getEntity() instanceof Creature)) {
            return;
        } 
        City chunkOwner = City.isChunkOwned(e.getBlock().getLocation().getChunk());
        if (chunkOwner == null) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    private void onExplosionBlockDamage(BlockExplodeEvent e) {
        City chunkOwner = City.isChunkOwned(e.getBlock().getLocation().getChunk());
        if (chunkOwner == null) {
            return;
        }
        e.setCancelled(true);
    }
    
}
