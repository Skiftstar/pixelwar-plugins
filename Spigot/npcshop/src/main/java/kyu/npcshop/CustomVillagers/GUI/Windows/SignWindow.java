package kyu.npcshop.CustomVillagers.GUI.Windows;

import java.util.ArrayList;
import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import kyu.npcshop.Main;
import kyu.npcshop.CustomVillagers.GUI.GUI;
import kyu.npcshop.CustomVillagers.GUI.Item.GuiItem;
import net.kyori.adventure.text.Component;

public class SignWindow implements Window {

    private GUI gui;
    private String[] lines;
    private JavaPlugin plugin;
    private Block b;
    private Consumer<PacketEvent> consumer = null;
    private PacketAdapter listener;

    public SignWindow(String[] lines, GUI gui, JavaPlugin plugin) {
        this.gui = gui;
        this.plugin = plugin;
        this.lines = lines;
    }

    @Override
    public void open() {
        ProtocolManager manager = Main.getInstance().getProtocolManager();

        Location location = gui.getHolder().getLocation();
        location.setY(location.getY() - 4);
        b = location.getBlock();
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        gui.getHolder().sendBlockChange(pos.toLocation(location.getWorld()), Material.OAK_SIGN.createBlockData());


        Component components[] = new Component[]{Component.text(lines[0]), Component.text(lines[1]), Component.text(lines[2]), Component.text(lines[3])};
        gui.getHolder().sendSignChange(location, new ArrayList<Component>(Arrays.asList(components)), DyeColor.BLACK, false);
        
        PacketContainer signGUIPacket = manager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        signGUIPacket.getBlockPositionModifier().write(0, pos);

        try {
            manager.sendServerPacket(gui.getHolder(), signGUIPacket);
        } catch (Exception e) {

        }
        listener = new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.UPDATE_SIGN) {
                    if (!event.getPacket().getBlockPositionModifier().read(0).equals(pos)) return;
                    if (consumer != null) {
                        consumer.accept(event);
                    }
                    close();
                }
            }
        };
        manager.addPacketListener(listener);
        // https://wiki.vg/Protocol -- Update Sign Packet
        // https://github.com/dmulloy2/ProtocolLib
        // https://www.spigotmc.org/threads/signmenu-1-16-5-get-player-sign-input.249381/page-6
        
    }

    public void setOnFinish(Consumer<PacketEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void close() {
        gui.getHolder().sendBlockChange(b.getLocation(), b.getBlockData());
        Main.getInstance().getProtocolManager().removePacketListener(listener);
    }

    @Override
    public GuiItem setItem(ItemStack item, int slot) {
        return null;
    }

    @Override
    public GuiItem setItem(Material itemType, String name, int slot) {
        return null;
    }

    @Override
    public void removeItem(int slot) {

    }

    @Override
    public ItemStack getItem(int slot) {
        return null;
    }

    @Override
    public GuiItem getGuiItem(int slot) {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Player getHolder() {
        return gui.getHolder();
    }

    @Override
    public GUI getGUI() {
        return gui;
    }

    @Override
    public void refreshWindow() {

    }

    @Override
    public Inventory getInv() {
        return null;
    }

    @Override
    public void handleInvClick(InventoryClickEvent e) {

    }

    @Override
    public void handleClose(InventoryCloseEvent e) {

    }

}
