package kyu.npcshop.CustomVillagers.GUI.Windows;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import kyu.npcshop.CustomVillagers.GUI.GUI;
import kyu.npcshop.CustomVillagers.GUI.Errors.AttemptToChangePlaceholderException;
import kyu.npcshop.CustomVillagers.GUI.Errors.NoMultiPageOnException;
import kyu.npcshop.CustomVillagers.GUI.Errors.NotEnoughRowsException;
import kyu.npcshop.CustomVillagers.GUI.Errors.PageOutOfBoundsException;
import kyu.npcshop.CustomVillagers.GUI.Errors.RowsOutOfBoundsException;
import kyu.npcshop.CustomVillagers.GUI.Errors.SlotOutOfBoundsException;
import kyu.npcshop.CustomVillagers.GUI.Item.GuiItem;
import kyu.npcshop.Util.Util;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.*;

public abstract class DefaultWindow implements Window {

    private GUI gui;
    private Inventory inv;

    private String title;
    private int rows;
    private Map<Integer, GuiItem[]> pages = new HashMap<>();
    private int currPage;
    private boolean isMultiPage = false, preventItemGrab = true, preventItemPlace = true, taskBarEnabled = false;

    private ItemStack nextPage, prevPage;
    private String[] pagePlaceholderNames = new String[]{"&aPrevious Page", "&aNext Page"};
    private ItemStack taskBarFiller;
    private TaskbarStyles taskbarStyle = TaskbarStyles.RIGHT;

    private List<GuiItem> relocatedItems = new ArrayList<>();

    private Consumer<InventoryCloseEvent> onClose = null;
    private Consumer<InventoryClickEvent> onPInvClick = null;

    protected DefaultWindow(@Nullable String title, int rows, GUI gui, JavaPlugin plugin) {
        generateDefaultPlaceholders();
        this.title = title;
        this.gui = gui;
        if (rows < 0 || rows > 6) {
            throw new RowsOutOfBoundsException();
        }
        if (title == null) {
            inv = Bukkit.createInventory(gui.getHolder(), rows * 9);
        } else {
            inv = Bukkit.createInventory(gui.getHolder(), rows * 9, Component.text(Util.color(title)));
        }
        this.rows = rows;
        pages.put(1, new GuiItem[rows * 9]);
        currPage = 1;
    }

    /*
    =============================================================================

                                User Accessible

    =============================================================================
     */


    /**
     * Opens the window
     */
    public void open() {
        refreshWindow();
        gui.getHolder().openInventory(inv);
    }

    /**
     * Closes the window
     */
    public void close() {

    }

    /*
    =============================================================================

                               Item Related

    =============================================================================
     */

    /**
     * Sets Item on Current Page at specified Slot
     * @param item the item appearing in the GUI
     * @param slot Slot the item will be placed in. Throws Exception when out of bounds
     * @return Added Item as GuiItem
     */
    public GuiItem setItem(ItemStack item, int slot) {
        checkSlotBounds(slot);
        doPlaceholderCheck(slot, getCurrentPage());
        GuiItem gItem = new GuiItem(item, slot, this);
        gItem.setPage(getCurrentPage());
        pages.get(currPage)[slot] = gItem;
        inv.setItem(slot, item);
        return gItem;
    }

    /**
     * Sets Item on Current Page at specified Slot
     * @param itemType Item Material
     * @param name Nullable - Name of the Item
     * @param slot Slot the item will be placed in. Throws Exception when out of bounds
     * @return Added Item as GuiItem
     */
    public GuiItem setItem(Material itemType, @Nullable String name, int slot) {
        ItemStack is = new ItemStack(itemType);
        if (name != null) {
            ItemMeta im = is.getItemMeta();
            im.displayName(Component.text(Util.color(name)));
            is.setItemMeta(im);
        }
        return setItem(is, slot);
    }

    /**
     * Adds item to the next free slot
     * Creates new Page of no other slot is free
     * @param item Item to add
     * @return Added Item as GuiItem
     */
    public GuiItem addItem(ItemStack item) {
        int slot = 0;
        int page = 1;
        while (getGuiItemFromPage(slot, page) != null) {
            slot++;
            if (slot > rows * 9 - 1) {
                slot = 0;
                page++;
                if (page > pages.size()) {
                    addPage();
                }
            }
        }
        return setItemAtPage(item, slot, page, false);
    }

    /**
     * Adds item to the next free slot
     * Creates new Page of no other slot is free
     * @param mat Material of the Item
     * @param name Nullable - Name of the Item
     * @return Added Item as GuiItem
     */
    public GuiItem addItem(Material mat, @Nullable String name) {
        ItemStack is = new ItemStack(mat);
        if (name != null) {
            ItemMeta im = is.getItemMeta();
            im.displayName(Component.text(Util.color(name)));
            is.setItemMeta(im);
        }
        return addItem(is);
    }

    /**
     * Sets Item on Specified Page at specified Slot
     * Requires multiPage to be true, will throw Exception otherwise
     * @param item Item appearing
     * @param slot Slot the Item will be placed in. Throws Exception if out of bounds
     * @param page Page the Item will be placed on. Throws Exception if out of bounds if forceCreatePage is false
     * @param forceCreatePage Whether a new Page should be created when page is out of bounds
     * @return Added Item as GuiItem
     */
    public GuiItem setItemAtPage(ItemStack item, int slot, int page, boolean forceCreatePage) {
        checkMultiPage();
        if (page == getCurrentPage()) {
            return setItem(item, slot);
        }
        if (forceCreatePage && page > pages.size()) {
            do {
                addPage();
            } while(page != pages.size());
        } else {
            checkPageBounds(page);
        }
        doPlaceholderCheck(slot, page);
        checkSlotBounds(slot);
        GuiItem gItem = new GuiItem(item, slot, this);
        gItem.setPage(page);
        pages.get(page)[slot] = gItem;
        return gItem;
    }

    /**
     * Sets Item on Specified Page at specified Slot
     * Requires multiPage to be true, will throw Exception otherwise
     * @param itemType Item Material
     * @param name Nullable - Name of the Item
     * @param slot Slot the Item will be placed in. Throws Exception if out of bounds
     * @param page Page the Item will be placed in. Throws Exception if out of bounds
     * @param forceCreatePage Whether a new Page should be created when page is out of bounds
     * @return Added Item as GuiItem
     */
    public GuiItem setItemAtPage(Material itemType, @Nullable String name, int slot, int page, boolean forceCreatePage) {
        ItemStack is = new ItemStack(itemType);
        if (name != null) {
            ItemMeta im = is.getItemMeta();
            im.displayName(Component.text(Util.color(name)));
            is.setItemMeta(im);
        }
        return setItemAtPage(is, slot, page, forceCreatePage);
    }

    /**
     * Removes Item from specified slot of the current Page
     * @param slot Slot that will be emptied. Throws Exception if out of bounds
     */
    public void removeItem(int slot) {
        checkSlotBounds(slot);
        doPlaceholderCheck(slot, getCurrentPage());
        pages.get(currPage)[slot] = null;
        inv.setItem(slot, new ItemStack(Material.AIR));
    }

    /**
     * Removes an item from a page
     * @param slot slot to be cleared. Throws Exception if out of bounds
     * @param page page of the slot. Throws Exception if out of bounds
     */
    public void removeItemFromPage(int slot, int page) {
        checkMultiPage();
        if (page == getCurrentPage()) {
            removeItem(slot);
            return;
        }
        doPlaceholderCheck(slot, page);
        checkPageBounds(page);
        checkSlotBounds(slot);
        pages.get(page)[slot] = null;
    }

    /**
     * Returns item of specified slot from the current page
     * @param slot Slot the item will be grabbed from. Throws Exception if out of bounds
     * @return ItemStack of Slot or null if slot is empty
     */
    public ItemStack getItem(int slot) {
        checkSlotBounds(slot);
        GuiItem item = pages.get(currPage)[slot];
        return item == null ? null : item.getItemStack();
    }

    /**
     * Returns GuiItem of specified slot from the current Page
     * @param slot Slot the GuiItem will be grabbed from. Throws Exception if out of bounds
     * @return GuiItem of Slot or null if slot is empty
     */
    public GuiItem getGuiItem(int slot) {
        checkSlotBounds(slot);
        return pages.get(currPage)[slot];
    }

    /**
     * Returns item of specified slot from specified page
     * @param slot Slot the item will be grabbed from. Throws Exception if out of bounds
     * @param page Page the item will be grabbed from. Throws Exception if out of bounds
     * @return ItemStack of Slot or null if slot is empty
     */
    public ItemStack getItemFromPage(int slot, int page) {
        checkMultiPage();
        checkPageBounds(page);
        checkSlotBounds(slot);
        GuiItem gItem = pages.get(page)[slot];
        return gItem == null ? null : gItem.getItemStack();
    }

    /**
     * Returns GuiItem of specified slot from specified page
     * @param slot Slot the item will be grabbed from. Throws Exception if out of bounds
     * @param page Page the item will be grabbed from. Throws Exception if out of bounds
     * @return GuiItem of Slot or null if slot is empty
     */
    public GuiItem getGuiItemFromPage(int slot, int page) {
        if (!isMultiPage && page > 1);
        checkPageBounds(page);
        checkSlotBounds(slot);
        return pages.get(page)[slot];
    }


    /*
    =============================================================================

                               Page Related

    =============================================================================
     */

    /**
     * Sets the Item the Taskbar is made out of
     * @param item
     */
    public void setTaskbarPlaceholder(ItemStack item) {
        taskBarFiller = item;
        for (int i : pages.keySet()) {
            setPlaceholders(i);
        }
    }

    /**
     * Sets the Item the Taskbar is made out of
     * @param mat Material of the item
     * @param name Nullable - Name of the item
     */
    public void setTaskbarPlaceholder(Material mat, @Nullable String name) {
        ItemStack is = new ItemStack(mat);
        if (name != null) {
            ItemMeta im = is.getItemMeta();
            im.displayName(Component.text(Util.color(name)));
            is.setItemMeta(im);
        }
        setTaskbarPlaceholder(is);
    }

    /**
     * Sets the style of the taskbar
     * @param taskbarStyle
     */
    public void setTaskbarStyle(TaskbarStyles taskbarStyle) {
        this.taskbarStyle = taskbarStyle;
    }

    /**
     * Sets whether the taskbar is enabled
     * @param taskBarEnabled
     */
    public void setTaskBarEnabled(boolean taskBarEnabled) {
        if (taskBarEnabled && rows == 1) {
            throw new NotEnoughRowsException();
        }
        this.taskBarEnabled = taskBarEnabled;
        if (taskBarEnabled) {
            for (int i : pages.keySet()) {
                setPlaceholders(i);
            }
        }
    }

    /**
     *
     * @return whether taskbar is enabled
     */
    public boolean isTaskBarEnabled() {
        return taskBarEnabled;
    }

    /**
     * Sets the Item for the page navigators
     * @param item
     * @param nextPageString Nullable - Name of the next page item
     * @param prevPageString Nullable - Name of the prev page item
     */
    public void setPagePlaceholders(ItemStack item, @Nullable String nextPageString, @Nullable String prevPageString) {
        nextPage = new ItemStack(item);
        ItemMeta im = nextPage.getItemMeta();
        if (nextPageString == null) {
            im.displayName(Component.text(Util.color(pagePlaceholderNames[1])));
        } else {
            im.displayName(Component.text(Util.color(nextPageString)));
        }
        nextPage.setItemMeta(im);

        prevPage = new ItemStack(item);
        im = prevPage.getItemMeta();
        if (prevPage == null) {
            im.displayName(Component.text(Util.color(pagePlaceholderNames[0])));
        } else {
            im.displayName(Component.text(Util.color(prevPageString)));
        }
        prevPage.setItemMeta(im);

        for (int i : pages.keySet()) {
            setPlaceholders(i);
        }
    }

    /**
     * Sets the item for the page navigators
     * @param mat Material of the item
     * @param nextPageString Nullable - Name of the next page item
     * @param prevPageString Nullable - Name of the prev page item
     */
    public void setPagePlaceholders(Material mat, @Nullable String nextPageString, @Nullable String prevPageString) {
        setPagePlaceholders(new ItemStack(mat), nextPageString, prevPageString);
    }

    /**
     * Adds a page
     * Requires multiPage to be true, throws Exception otherwise
     */
    public void addPage() {
        checkMultiPage();
        pages.put(pages.size() + 1, new GuiItem[rows * 9]);

        setPlaceholders(pages.size() - 1);
        setPlaceholders(pages.size());
    }

    /**
     * Removes a page
     * Requires multiPage to be true, throws Exception otherwise
     * Higher Page Numbers will be moved down to fill empty spot
     * @param page Page to be removed. Throws Exception if out of bounds
     */
    public void removePage(int page) {
        checkMultiPage();
        checkPageBounds(page);
        pages.remove(page);
        if (pages.size() == 0) {
            pages.put(1, new GuiItem[rows * 9]);
        }
        fillEmptyPageSpots();

        for (int i : pages.keySet()) {
            setPlaceholders(i);
        }

        //If last page is removed and user is on last, set current Page to *new* last page
        if (getCurrentPage() > pages.size()) {
            setPage(pages.size());
            refreshWindow();
        }
        //Also refresh window if page the user was on is removed
        //Say user is on page 5/6 and page 5 is removed, then user is on still page 5, but
        //It's not the same Page 5, so it needs to be refreshed
        else if (getCurrentPage() == page) {
            refreshWindow();
        }
    }

    /**
     * Moves later pages to the front to fill empty spots
     * Changes Page numbers obviously
     */
    public void fillEmptyPageSpots() {
        //Approach presupposes that only one page at a time is removed before this function is called
        int page = 1;
        while (true) {
            if (pages.get(page) == null && pages.get(page + 1) != null) {
                while (pages.get(page + 1) != null) {
                    pages.put(page, pages.get(page +1));
                    for (GuiItem item : pages.get(page)) {
                        if (item != null) item.setPage(page);
                    }
                    pages.remove(page + 1);
                    page++;
                }
                break;
            }
            page++;
            //Prevent overflow and handle edge case of last page being removed
            if (page > pages.size() + 1) break;
        }
    }

    /**
     * Changes Page to specified page
     * Requires multiPage to be true, will throw Exception otherwise
     * @param page Page to change to. Throws Exception if out of bounds
     */
    public void setPage(int page) {
        checkMultiPage();
        checkPageBounds(page);
        currPage = page;
        refreshWindow();
    }

    /**
     * Clears specified page, does <b>not remove the page</b>
     * @param page Page to be cleared. Requires multiPage to be true if greater than 1. Throws Exception if out of bounds
     */
    public void clearPage(int page) {
        if (page > 1) {
            checkMultiPage();
        }
        checkPageBounds(page);
        pages.remove(page);
        pages.put(page, new GuiItem[rows * 9]);
        if (currPage == page) {
            refreshWindow();
        }
    }

    /**
     * Clears all Pages <b>and removes them (except first)</b>
     */
    public void clearAllPages() {
        pages.clear();
        pages.put(1, new GuiItem[rows * 9]);
        currPage = 1;
        refreshWindow();
    }

    /**
     * Removes all empty pages. Pages with only Placeholders on them count as empty
     */
    public void removeEmptyPages() {
        List<Integer> pagesToRemove = new ArrayList<>();
        for (int page : pages.keySet()) {
            if (isPageEmpty(page)) pagesToRemove.add(page);
        }
        for (int i = 0; i < pagesToRemove.size(); i++) {
            removePage(pagesToRemove.get(i) - i);
        }
    }

    /**
     *
     * @param page The page to check. Throws Exception if out of bounds
     * @return Whether the page is empty. Ignores Placeholders
     */
    public boolean isPageEmpty(int page) {
        checkPageBounds(page);
        for (GuiItem item : pages.get(page)) {
            if (item == null) continue;
            if (item.getType().equals(GuiItem.GItemType.DEFAULT)) return false;
        }
        return true;
    }

    /**
     * Changes whether the inventory is allowed to have multiple pages
     * If set from true to false, the current Page will be set to 1
     * the other pages will not be deleted tho
     * Will move Items on Page 1 to make space for PageChanger Placeholders
     * @param value
     */
    public void setMultiPage(boolean value) {
        isMultiPage = value;
        if (!value) {
            currPage = 1;
            refreshWindow();
        } else {
            for (int i : pages.keySet()) {
                setPlaceholders(i);
            }
        }
    }

    /**
     *
     * @return Current Page Number
     */
    public int getCurrentPage() {
        return currPage;
    }

    /**
     *
     * @return Map(PageNumber, Array of GuiItems) of all Pages
     */
    public Map<Integer, GuiItem[]> getPages() {
        return pages;
    }

    /**
     *
     * @return Whether the inventory is allowed to have multiple pages
     */
    public boolean isMultiPage() {
        return isMultiPage;
    }

    /**
     * Reloads all Items on the current page
     */
    public void refreshWindow() {
        GuiItem[] items = pages.get(currPage);
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                inv.setItem(i, new ItemStack(Material.AIR));
                continue;
            }
            inv.setItem(i, items[i].getItemStack());
        }
    }

    /*
    =============================================================================

                                Getters/Setters

    =============================================================================
     */

    /**
     * Set whether taking items from the inventory should be prevented
     * @param value
     */
    public void setPreventItemGrab(boolean value) {
        preventItemGrab = value;
    }

    /**
     *
     * @return Whether taking items from the inventory is prevented
     */
    public boolean isPreventItemGrab() {
        return preventItemGrab;
    }

    /**
     * Sets whether Placing Items in the inventory should be prevented
     * Note: Placed items will <b>not</b> be added to the items list so far.
     * @param preventItemPlace
     */
    public void setPreventItemPlace(boolean preventItemPlace) {
        this.preventItemPlace = preventItemPlace;
    }

    /**
     *
     * @return Whether Placing Items in the inventory is prevented
     * Note: Placed items will <b>not</b> be added to the items list so far.
     */
    public boolean isPreventItemPlace() {
        return preventItemPlace;
    }

    /**
     *
     * @return Title of the Inventory
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return Inventory Holder
     */
    public Player getHolder() {
        return gui.getHolder();
    }

    /**
     *
     * @return GUI the Window is part of
     */
    public GUI getGUI() {
        return gui;
    }

    /**
     *
     * @return amount of rows of the inventory
     */
    public int getRows() {
        return rows;
    }

    /**
     *
     * @return the inventory of the window
     */
    public Inventory getInv() {
        return inv;
    }

    /**
     * Returns the next free slot and the page this slot is on
     * @param allowCreateNewPage if true, creates a new Page if there's no free slot
     * @return Key - Slot Value - Page | -1 for both if no free slot
     */
    public AbstractMap.SimpleEntry<Integer, Integer> getNextFreeSlot(boolean allowCreateNewPage) {
        int slot = 0;
        int page = 1;
        while (getGuiItemFromPage(slot, page) != null) {
            slot++;
            if (slot > rows * 9 - 1) {
                slot = 0;
                page++;
                if (page > pages.size()) {
                    if (allowCreateNewPage) {
                        addPage();
                    } else {
                        return new AbstractMap.SimpleEntry<>(-1, -1);
                    }
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(slot, page);
    }

    /**
     * Set function to execute on InvClose
     * @param onClose Function to execute once Inv is closed
     */
    public void setOnClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
    }

    /**
     * Set function to execute on Player Inventory Click
     * @param onClose Function to execute once Player Inv is Clicked
     */
    public void setOnPInvClick(Consumer<InventoryClickEvent> onPInvClick) {
        this.onPInvClick = onPInvClick;
    }


    /*
    =============================================================================

                           Package Private Methods

    =============================================================================
     */

    void removePlaceHolders(int page) {
        for (int i = 1; i != 3; i++) {
            GuiItem item = getGuiItemFromPage(rows * 9 - i, page);
            if (item == null || !item.getType().equals(GuiItem.GItemType.PLACEHOLDER)) {
                continue;
            }
            forceRemoveItem(rows * 9 - i, page);
        }
    }

    void setPlaceholders(int page) {
        GuiItem gNextPage = null, gPrevPage = null;
        removePlaceHolders(page);

        TaskbarStyles style = null;

        if (isTaskBarEnabled()) {
            style = taskbarStyle;
            for (int i = (rows - 1) * 9; i < rows * 9; i++) {
                forceRemoveItem(i, page);
                GuiItem placeholder = setItemAtPage(taskBarFiller, i, page, false);
                placeholder.setType(GuiItem.GItemType.PLACEHOLDER);
            }
        }

        if (style == null) {
            int placeSlot = 8;
            if (pages.get(page + 1) != null) {
                gNextPage = replaceItemAtPage(nextPage, (rows - 1) * 9 + placeSlot, page);
                placeSlot--;
            }
            if (page > 1) {
                gPrevPage = replaceItemAtPage(prevPage, (rows - 1) * 9 + placeSlot, page);
            }
        } else {
            if (pages.get(page + 1) != null) {
                gNextPage = replaceItemAtPage(nextPage, (rows - 1) * 9 + style.next(), page);
            }
            if (page > 1) {
                gPrevPage = replaceItemAtPage(prevPage, (rows - 1) * 9 + style.prev(), page);
            }
        }


        if (gNextPage != null) {
            gNextPage.setType(GuiItem.GItemType.PLACEHOLDER);
            gNextPage.setOnClick(e -> {
                setPage(page + 1);
            });
        }
        if (gPrevPage != null) {
            gPrevPage.setType(GuiItem.GItemType.PLACEHOLDER);
            gPrevPage.setOnClick(e -> {
                setPage(page - 1);
            });
        }
        System.out.println("Updated Page: " + page);
        if (page == getCurrentPage()) refreshWindow();
        List<GuiItem> move = new ArrayList<>(relocatedItems);
        relocatedItems.clear();
        for (GuiItem item : move) {
            AbstractMap.SimpleEntry<Integer, Integer> freeSlot = getNextFreeSlot(true);
            item.relocatePage(freeSlot.getKey(), freeSlot.getValue());
        }
    }

    void checkPageBounds(int page) {
        if (page < 1 || page > pages.size()) {
            throw new PageOutOfBoundsException(page, pages.size());
        }
    }

    void checkMultiPage() {
        if (!isMultiPage) {
            throw new NoMultiPageOnException();
        }
    }

    void checkSlotBounds(int slot) {
        if (slot < 0 || slot > rows * 9 - 1) {
            throw new SlotOutOfBoundsException(slot, rows * 9 -1);
        }
    }

    boolean isInvMove(InventoryClickEvent e) {
        switch (e.getAction()) {
            case MOVE_TO_OTHER_INVENTORY:
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case HOTBAR_SWAP:
                return true;
            default:
                return false;
        }
    }

    public void handleInvClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || !(e.getClickedInventory().equals(getInv()) || e.getClickedInventory().equals(getHolder().getInventory()))) {
            return;
        }
        if (!e.getWhoClicked().equals(getHolder())) {
            return;
        }
        System.out.println(e.getAction());
        if (isPreventItemPlace() && isInvMove(e)) {
            e.setCancelled(true);
        }
        if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) {
            return;
        }
        if (!e.getWhoClicked().getOpenInventory().getTopInventory().equals(getInv())) {
            return;
        }
        if (isPreventItemGrab()) {
            e.setCancelled(true);
        }
        if (e.getClickedInventory().equals(getHolder().getInventory())) {
            if (onPInvClick != null) onPInvClick.accept(e);
            return;
        }
        int slot = e.getSlot();
        GuiItem item = getPages().get(getCurrentPage())[slot];
        if (item == null) {
            return;
        }
        // getHolder().sendMessage(Component.text("Click triggered from Inv with size " + getInv().getSize()));
        item.executeOnClick(e);
    }

    void generateDefaultPlaceholders() {
        nextPage = new ItemStack(Material.PAPER);
        ItemMeta im = nextPage.getItemMeta();
        im.displayName(Component.text(Util.color(pagePlaceholderNames[1])));
        nextPage.setItemMeta(im);

        prevPage = new ItemStack(Material.PAPER);
        im = prevPage.getItemMeta();
        im.displayName(Component.text(Util.color(pagePlaceholderNames[0])));
        prevPage.setItemMeta(im);

        taskBarFiller = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        im = taskBarFiller.getItemMeta();
        im.displayName(Component.text(" "));
        taskBarFiller.setItemMeta(im);
    }

    void doPlaceholderCheck(int slot, int page) {
        GuiItem item = getGuiItemFromPage(slot, page);
        if (item == null) return;
        if (item.getType().equals(GuiItem.GItemType.PLACEHOLDER)) {
            throw new AttemptToChangePlaceholderException();
        }
    }

    //Ignores checks
    void forceRemoveItem(int slot, int page) {
        GuiItem itemAtSlot = getGuiItemFromPage(slot, page);
        if (itemAtSlot != null && itemAtSlot.getType().equals(GuiItem.GItemType.DEFAULT)) {
            relocatedItems.add(itemAtSlot);
        }
        pages.get(page)[slot] = null;
    }

    GuiItem replaceItemAtPage(ItemStack item, int slot, int page) {
        forceRemoveItem(slot, page);
        return setItemAtPage(item, slot, page, false);
    }

    public void handleClose(InventoryCloseEvent e) {
        // getHolder().sendMessage(Component.text("Start Handle Close of inv with size" + e.getInventory().getSize()));
        if (e.getInventory() == null || !e.getInventory().equals(getInv())) {
            return;
        }
        if (!e.getPlayer().equals(getHolder())) {
            return;
        }
        // getHolder().sendMessage(Component.text("Handling close Inv with size" + e.getInventory().getSize()));
        gui.setCurrentWindow(null);
        if (onClose != null) {
            onClose.accept(e);
        }
    }

}
