package net.Indyuce.inventory.listener;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.gui.PlayerInventoryView;
import net.Indyuce.inventory.util.InventoryButton;
import net.Indyuce.inventory.util.Utils;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryButtonListener implements Listener {
    private final ItemStack icon;
    private final int slot;

    public InventoryButtonListener(ConfigurationSection config) {
        slot = config.getInt("slot");
        icon = new InventoryButton(config.getConfigurationSection("item")).getItem();
    }

    @EventHandler
    public void giveItemsOnJoin(PlayerJoinEvent event) {
        if (! setupInventoryIcon(event.getPlayer().getInventory())) {
            event.getPlayer().sendMessage(MMOInventory.plugin.getTranslation("full-inventory"));
        }
    }
    
    @EventHandler
    public void giveItemsOnRespawn(PlayerRespawnEvent event) {
        if (! setupInventoryIcon(event.getPlayer().getInventory())) {
            event.getPlayer().sendMessage(MMOInventory.plugin.getTranslation("full-inventory"));
        }
    }

    protected boolean setupInventoryIcon(Inventory inv) {

        // If slot already is icon
        // then leave it
        if (isInventoryButton(inv.getItem(slot))) {
            return true;
        }

        ItemStack holder = null;

        // Delete old button
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);

            if (i == slot || isEmpty(item)) continue;

            if (isInventoryButton(inv.getItem(i))) {
                inv.clear(i);
            }
        }

        // Inventory is full, cant setup
        if (inv.firstEmpty() == -1) return false;

        if (isEmpty(inv.getItem(slot)) == false && isInventoryButton(inv.getItem(slot)) == false) {
            holder = inv.getItem(slot);
            inv.addItem(holder);
        }

        inv.setItem(slot, icon);

        return true;
    }

    protected boolean isInventoryButton(ItemStack item) {
        if (isEmpty(item)) return false;

        return Utils.isButton(item);
    }

    protected boolean isEmpty(ItemStack item) {
        return item == null || Material.AIR.equals(item.getType());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockInteractions(InventoryClickEvent event) {
        if (Utils.isButton(event.getCurrentItem())) {
            new PlayerInventoryView((Player) event.getWhoClicked()).open();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockDrop(PlayerDeathEvent event) {
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack next = iterator.next();
            if (Utils.isButton(next))
                iterator.remove();
        }
    }
}
