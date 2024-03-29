package kyu.npcshop.CustomVillagers.GUI.Item;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kyu.npcshop.CustomVillagers.GUI.Windows.DefaultWindow;
import kyu.npcshop.CustomVillagers.GUI.Windows.Window;
import kyu.npcshop.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class GuiItem {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("§x(?>§[0-9a-f]){6}", 2);

    private ItemStack item;
    private Window parentWindow;
    private int slot, page;
    private Consumer<InventoryClickEvent> function = null;
    private GItemType type;

    public GuiItem(ItemStack item, int slot, Window parentWindow) {
        this.item = item;
        this.parentWindow = parentWindow;
        this.slot = slot;
        type = GItemType.DEFAULT;
    }

    public GuiItem(ItemStack item, int slot, GItemType type, Window parentWindow) {
        this.item = item;
        this.parentWindow = parentWindow;
        this.slot = slot;
        this.type = type;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    /**
     * Not intended for public use!
     * @param slot
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @SuppressWarnings("deprecation")
    public void setBasicLore(List<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setLore(lore);
        item.setItemMeta(im);
        parentWindow.refreshWindow();
    }

    public Window getParentWindow() {
        return parentWindow;
    }

    public void setItemStack(ItemStack is) {
        this.item = is;
        parentWindow.refreshWindow();
    }

    public void setName(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(Util.color(name)));
        item.setItemMeta(meta);
        parentWindow.refreshWindow();
    }


    public void setOnClick(Consumer<InventoryClickEvent> consumer) {
        function = consumer;
    }

    public void executeOnClick(InventoryClickEvent e) {
        if (function == null) {
            return;
        }
        function.accept(e);
    }

    public GItemType getType() {
        return type;
    }

    public void setType(GItemType type) {
        this.type = type;
    }

    public int getPage() {
        return page;
    }

    //Not intended for public use
    public void setPage(int page) {
        this.page = page;
    }

    public enum GItemType {
        PLACEHOLDER, DEFAULT;
    }

    public void setLore(int max, String string) {
        //Max is for the maximum amount of characters you want to have on one line (Javadoc when done with this)
        long startTime = System.currentTimeMillis();
        String stringWithColor = Util.color(string);
        String[] paragraphs = stringWithColor.split("\n");
        ArrayList<Component> lore = new ArrayList<>(); //The ArrayList that we are going to use to set the metadata
        String lastUsedColor = "";

        for(String s : paragraphs){

            int holder = 0; //The index of the last character of the last line

            //Example: We just completed the first line, for example: 'Hello how are'.
            //The index of the last 'e' is 13, so we know that the space behind that e is 14 we need to start our next line
            //from 15 till ... because we don't the space.

            for(int i = 0; i < s.length() - max; i = holder){ //Iterate while i is smaller than the length - max because we know that's our last line.
                int endOfLineIndex = i + max; //Because i is the beginning index and max is the last index.
                boolean dashUse = true; //If it's not possible to create a new line because there is no space in less than max/4 indexes in front of us, it's true.

                for(int i1 = 0; i1 < max; i1++){
                    if(Character.isWhitespace(s.charAt(endOfLineIndex - i1))){ //Our index that we want is our end - i1.
                        endOfLineIndex = endOfLineIndex - i1; //Update the index.
                        dashUse = false; //We found a space, so let's just cut the word and use a '-' instead.
                        break;
                    }
                }

                String line = s.substring(holder, endOfLineIndex); //Cut our paragraph
                holder = endOfLineIndex + 1; //We don't want the space at our next line, so let's just delete it.
                if(dashUse){
                    line = line + "-"; //Add a '-' to the end of the line because we didn't find a space.
                    holder = endOfLineIndex; //We don't have a space now, so we don't need to remove our first character of our next line.
                }

                line = lastUsedColor + line;

                lore.add(Component.text(Util.color(line)));

                StringBuilder result = new StringBuilder();

                for(int index = line.length() - 1; index > -1; --index) { //Took this part from the ChatColor class and changed it a bit because it wouldn't work properly.
                    char section = line.charAt(index);
                    if (section == 167 && index < line.length() - 1) {
                        if (index > 11 && line.charAt(index - 12) == 167 && (line.charAt(index - 11) == 'x' || line.charAt(index - 11) == 'X')) {
                            String color = line.substring(index - 12, index + 2);
                            if (HEX_COLOR_PATTERN.matcher(color).matches()) {
                                result.insert(0, color);
                                break;
                            }
                        }

                        char c = line.charAt(index + 1);
                        result.append("&").append(c);
                    }
                }

                if(!result.toString().equals("")){
                    StringBuilder resultS = new StringBuilder();

                    for(int x = result.length(); x >= 2; x -= 2){
                        resultS.append(result.substring(x - 2, x));
                    }

                    lastUsedColor = resultS.toString();
                }

            }
            lore.add(Component.text(Util.color(lastUsedColor + s.substring(holder)))); //We don't need to do anything with our last line of the paragraph.
                                                                                          //So we just need to give our start index.
        }

        long endTime = System.currentTimeMillis();
        System.out.println(startTime);
        System.out.println(endTime);
        long usedTime = endTime - startTime;

        System.out.println(usedTime);

        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public void relocatePage(int slot, int page) {
        GuiItem itemOnOldSlot = ((DefaultWindow) parentWindow).getGuiItemFromPage(getSlot(), getPage());
        if (itemOnOldSlot != null && itemOnOldSlot.equals(this)) {
            ((DefaultWindow) parentWindow).removeItemFromPage(getSlot(), getPage());
        }
        ((DefaultWindow) parentWindow).getPages().get(page)[slot] = this;
        if (page == ((DefaultWindow) parentWindow).getCurrentPage()) {
            parentWindow.refreshWindow();
        }
    }
}
