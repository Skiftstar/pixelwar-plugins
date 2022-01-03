package Kyu.GuiAPI.Windows;

import Kyu.GuiAPI.GUI;
import Kyu.GuiAPI.Item.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public interface Window {

    void open();

    void close();

    GuiItem setItem(ItemStack item, int slot);

    GuiItem setItem(Material itemType, @Nullable String name, int slot);

    void removeItem(int slot);

    ItemStack getItem(int slot);

    GuiItem getGuiItem(int slot);

    String getTitle();

    Player getHolder();

    GUI getGUI();

    void refreshWindow();

}
