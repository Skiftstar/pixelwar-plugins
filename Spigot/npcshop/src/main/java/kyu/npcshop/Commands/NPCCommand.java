package kyu.npcshop.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import kyu.npcshop.Main;
import kyu.npcshop.CustomVillagers.CstmVillager;
import kyu.npcshop.Listeners.ClickListener;
import net.kyori.adventure.text.Component;

public class NPCCommand implements CommandExecutor {
    
    public NPCCommand(Main plugin) {
        plugin.getCommand("npc").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String string,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.helper().getMess("PlayerOnly", true));
            return false;
        }

        Player p = (Player) sender;

        if (args.length < 2) {
            p.sendMessage(Component.text(Main.helper().getMess(p, "NEArgs", true)));
            return false;
        }

        if (args[0].equalsIgnoreCase("create")) {
            Villager villager = (Villager) p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setCollidable(false);
            villager.setPersistent(true);
            villager.setRemoveWhenFarAway(false);
            villager.setSilent(true);
            villager.setCustomName(args[1]);
            ClickListener.villagers.put(villager.getUniqueId(), new CstmVillager(villager.getUniqueId()));
            Main.getInstance().getConfig().set("Villagers." + villager.getUniqueId(), true);
            Main.getInstance().saveConfig();
        }

        return true;
    }
}
