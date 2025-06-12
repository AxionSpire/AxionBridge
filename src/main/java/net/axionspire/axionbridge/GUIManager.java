package net.axionspire.axionbridge;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GUIManager {
    private AxionBridge plugin;
    public GUIManager(AxionBridge plugin) {
        this.plugin = plugin;
    }

    public Inventory mainMenu(Player player) {
        Inventory inv = plugin.getServer().createInventory(player, 9, BridgeTools.getInstance().getPrefix() + ChatColor.BLUE + "Main Menu");
        // TODO: Add buttons to do things
        return inv;
    }
}
