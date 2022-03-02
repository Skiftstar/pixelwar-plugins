package kyu.pixesssentials.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kyu.pixesssentials.Main;
import net.kyori.adventure.text.Component;

public class EnchantCmd implements TabExecutor {

    public EnchantCmd(Main plugin) {
        plugin.getCommand("enchant").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String string,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.helper.getMess("PlayerOnly"));
            return false;
        }
        Player p = (Player) sender;

        if (!p.hasPermission("core.essentials.enchant")) {
            p.sendMessage(Component.text(Main.helper.getMess(p, "NEPerms", true)));
            return false;
        }

        if (args.length < 2) {
            p.sendMessage(Component.text(Main.helper.getMess(p, "NEArgs", true)));
            return false;
        }

        if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            p.sendMessage(Component.text(Main.helper.getMess(p, "NoItemInMainHand", true)));
            return false;
        }

        String enchKey = args[0];
        Enchantment ench = Enchantment.getByKey(NamespacedKey.fromString(enchKey));
        if (ench == null) {
            p.sendMessage(Component.text(Main.helper.getMess(p, "EnchantmentNotFound", true)));
            return false;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            p.sendMessage(Component.text(Main.helper.getMess(p, "NoNumberForLevel", true)));
            return false;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(ench, level, true);
        item.setItemMeta(meta);
        p.sendMessage(Component.text(Main.helper.getMess(p, "ItemEnchantSuccess", true)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
            @NotNull String string, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> argList = new ArrayList<>();

            for (Enchantment ench : Enchantment.values()) {
                if (ench.getKey().asString().contains(args[0]))
                    argList.add(ench.getKey().asString());
            }

            return argList;
        }
        return Collections.emptyList();
    }

}
