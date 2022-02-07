package kyu.npcshop.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import kyu.npcshop.Main;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.EntityPlayer;

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

        if (args.length < 1) {
            p.sendMessage(Component.text(Main.helper().getMess(p, "NEArgs", true)));
            return false;
        }

        if (args[0].equalsIgnoreCase("create")) {
            
        }

        return true;
    }
}
