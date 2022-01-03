package Kyu.LobbyCore;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private String guiName, serverName;
    private Material guiBlock;
    private List<Component> description;
    private int slot;

    public Server(String serverName, String guiName, Material guiBlock, List<String> description, int slot) {
        this.guiBlock = guiBlock;
        this.guiName = guiName;
        this.serverName = serverName;
        this.description = colorDescription(description);
        this.slot = slot;
    }

    public List<Component> colorDescription(List<String> description) {
        List<Component> tmp = new ArrayList<>();
        for (String s : description) {
            tmp.add(Component.text(Util.color(s)));
        }
        return tmp;
    }

    public List<Component> getDescription() {
        return description;
    }

    public String getGuiName() {
        return guiName;
    }

    public Material getGuiBlock() {
        return guiBlock;
    }

    public String getServerName() {
        return serverName;
    }

    public int getSlot() {
        return slot;
    }
}
